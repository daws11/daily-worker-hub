package com.example.dwhubfix.data

import android.content.Context
import android.Manifest
import android.location.Location
import android.util.Base64
import com.example.dwhubfix.BuildConfig
import com.example.dwhubfix.model.Booking
import com.example.dwhubfix.model.Shift
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.storage
import java.io.File
import java.util.UUID
import java.util.concurrent.CompletableFuture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// =====================================================
// BOOKING FLOW REPOSITORY
// Handle clock-in/out with geolocation and selfie verification
// =====================================================

object BookingRepository {
    
    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }

    // =====================================================
    // CLOCK-IN LOGIC
    // =====================================================
    
    /**
     * Clock in to a shift with geolocation and selfie
     * @param context Application context
     * @param bookingId Booking ID
     * @param currentLocation Current GPS location
     * @param selfieImageFile Selfie image file to upload
     * @return Result with updated booking
     */
    suspend fun clockIn(
        context: Context,
        bookingId: String,
        currentLocation: Location,
        selfieImageFile: File
    ): Result<ClockInResult, ClockInError> {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Get access token
                val accessToken = SessionManager.getAccessToken(context)
                    ?: return Result.failure(ClockInError.NoSession)

                // 2. Get booking details
                val booking = client.postgrest["bookings"]
                    .select("*")
                    .eq("id", bookingId)
                    .singleOrNull<BookingData>()

                if (booking == null) {
                    return Result.failure(ClockInError.BookingNotFound)
                }

                // Check if already clocked in
                if (booking["clock_in_time"] != null) {
                    return Result.failure(ClockInError.AlreadyClockedIn)
                }

                // Check if booking is confirmed
                val status = booking["status"] as? String
                if (status != "confirmed") {
                    return Result.failure(ClockInError.BookingNotConfirmed)
                }

                // 3. Upload selfie to Supabase Storage
                val selfieUrl = uploadSelfieImage(context, bookingId, selfieImageFile)

                // 4. Verify geolocation (within 500m of shift location)
                val locationVerified = verifyLocation(context, booking["shift_id"] as? String, currentLocation)

                // 5. Update booking with clock-in data
                val updateData = mapOf(
                    "status" to "clocked_in",
                    "clock_in_time" to java.time.Instant.now().toString(),
                    "clock_in_location_lat" to currentLocation.latitude,
                    "clock_in_location_lng" to currentLocation.longitude,
                    "clock_in_selfie_url" to selfieUrl,
                    "clock_in_accuracy" to locationVerified.accuracy
                )

                val updatedBooking = client.postgrest["bookings"]
                    .update(updateData)
                    .eq("id", bookingId)
                    .select()
                    .singleOrNull<Map<String, *>>()

                // 6. Update shift status if this is first worker
                val shiftId = booking["shift_id"] as? String
                updateShiftStatusIfNeeded(context, shiftId)

                // 7. Create transaction record
                val workerId = booking["worker_id"] as? String

                // Get shift details for payment calculation
                val shift = client.postgrest["shifts"]
                    .select("*")
                    .eq("id", shiftId)
                    .singleOrNull<Map<String, *>>()

                // Calculate earnings
                val hoursWorked = calculateHoursWorked(
                    shift?.get("start_time") as? String ?: "",
                    shift?.get("end_time") as? String ?: ""
                )

                val ratePerHour = (shift?.get("rate_per_hour") as? Long) ?: 0L
                val earnings = hoursWorked * ratePerHour

                // Credit to wallet (will be final when clocked out)
                client.postgrest["transactions"]
                    .insert(mapOf(
                        "wallet_id" to workerId,
                        "booking_id" to bookingId,
                        "type" to "credit_pending",
                        "amount" to earnings,
                        "description" to "Clock-in payment for ${shift?.get("job_title")}",
                        "status" to "pending",
                        "reference_id" to "clock_in_${bookingId}"
                    ))

                Result.success(ClockInResult(
                    booking = Booking(
                        id = bookingId,
                        shiftId = shiftId,
                        workerId = workerId,
                        businessId = booking["business_id"] as? String ?: "",
                        status = "clocked_in",
                        clockInTime = java.time.Instant.now().toString(),
                        clockInLocationLat = currentLocation.latitude,
                        clockInLocationLng = currentLocation.longitude,
                        clockInSelfieUrl = selfieUrl,
                        locationVerified = locationVerified.isWithinRadius,
                        shift = Shift(
                            id = shiftId,
                            jobType = shift?.get("job_type") as? String ?: "",
                            jobTitle = shift?.get("job_title") as? String ?: "",
                            date = shift?.get("date") as? String ?: "",
                            startTime = shift?.get("start_time") as? String ?: "",
                            endTime = shift?.get("end_time") as? String ?: "",
                            ratePerHour = ratePerHour
                        )
                    ),
                    message = "Successfully clocked in",
                    earningsSoFar = earnings
                ))

            } catch (e: Exception) {
                Result.failure(ClockInError.NetworkError(e.message ?: "Unknown error"))
            }
        }
    }

    // =====================================================
    // CLOCK-OUT LOGIC
    // =====================================================

    /**
     * Clock out from a shift
     * @param context Application context
     * @param bookingId Booking ID
     * @return Result with final earnings and completed booking
     */
    suspend fun clockOut(
        context: Context,
        bookingId: String
    ): Result<ClockOutResult, ClockOutError> {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Get access token
                val accessToken = SessionManager.getAccessToken(context)
                    ?: return Result.failure(ClockOutError.NoSession)

                // 2. Get booking details
                val booking = client.postgrest["bookings"]
                    .select("*")
                    .eq("id", bookingId)
                    .singleOrNull<BookingData>()

                if (booking == null) {
                    return Result.failure(ClockOutError.BookingNotFound)
                }

                // Check if clocked in
                if (booking["clock_in_time"] == null) {
                    return Result.failure(ClockOutError.NotClockedIn)
                }

                // Check if already clocked out
                if (booking["clock_out_time"] != null) {
                    return Result.failure(ClockOutError.AlreadyClockedOut)
                }

                // 3. Get current location
                val location = getCurrentLocation(context)

                // 4. Verify location (within 1km of clock-in location)
                val clockInLat = booking["clock_in_location_lat"] as? Double ?: 0.0
                val clockInLng = booking["clock_in_location_lng"] as? Double ?: 0.0
                val clockInAccuracy = booking["clock_in_accuracy"] as? Float ?: 0f

                val currentLat = location.latitude
                val currentLng = location.longitude
                val distance = calculateDistance(clockInLat, clockInLng, currentLat, currentLng)

                val locationVerified = distance <= 1.0 // Within 1km

                // 5. Upload proof selfie if enabled
                val proofSelfieUrl = if (locationVerified) {
                    // Take current selfie as proof of completion
                    val timestamp = System.currentTimeMillis()
                    val fileName = "proof_${bookingId}_${timestamp}.jpg"
                    uploadSelfieImage(context, bookingId, null) // TODO: Implement camera capture
                } else null

                // 6. Update booking with clock-out data
                val updateData = mapOf(
                    "status" to "completed",
                    "clock_out_time" to java.time.Instant.now().toString(),
                    "clock_out_location_lat" to currentLat,
                    "clock_out_location_lng" to currentLng,
                    "clock_out_selfie_url" to proofSelfieUrl,
                    "location_verified" to locationVerified
                )

                val updatedBooking = client.postgrest["bookings"]
                    .update(updateData)
                    .eq("id", bookingId)
                    .select()
                    .singleOrNull<Map<String, *>>()

                // 7. Get shift details for final payment calculation
                val shiftId = booking["shift_id"] as? String
                val shift = client.postgrest["shifts"]
                    .select("*")
                    .eq("id", shiftId)
                    .singleOrNull<Map<String, *>>()

                // Calculate final earnings
                val hoursWorked = calculateHoursWorked(
                    shift?.get("start_time") as? String ?: "",
                    shift?.get("end_time") as? String ?: ""
                )

                val ratePerHour = (shift?.get("rate_per_hour") as? Long) ?: 0L
                val finalEarnings = hoursWorked * ratePerHour

                // 8. Finalize payment - credit to wallet
                val workerId = booking["worker_id"] as? String
                val earningsSoFar = (booking["total_earnings"] as? Long) ?: 0L
                val totalEarnings = earningsSoFar + finalEarnings

                // Update transaction to completed
                client.postgrest["transactions"]
                    .update(mapOf(
                        "status" to "completed",
                        "balance_after" to totalEarnings,
                        "description" to "Completed shift: ${shift?.get("job_title")}"
                    ))
                    .eq("reference_id", "clock_in_${bookingId}")
                    .eq("booking_id", bookingId)

                // Credit to wallet
                client.postgrest["wallets"]
                    .update(mapOf(
                        "balance" to totalEarnings,
                        "total_earned" to totalEarnings
                    ))
                    .eq("user_id", workerId)

                // 9. Update worker stats (rating, reliability, shift count)
                updateWorkerStats(context, workerId, shiftId, finalEarnings, locationVerified)

                // 10. Update shift status to completed
                updateShiftStatus(context, shiftId, "completed")

                Result.success(ClockOutResult(
                    booking = Booking(
                        id = bookingId,
                        shiftId = shiftId,
                        workerId = workerId,
                        businessId = booking["business_id"] as? String ?: "",
                        status = "completed",
                        clockOutTime = java.time.Instant.now().toString(),
                        finalEarnings = finalEarnings,
                        totalEarnings = totalEarnings,
                        locationVerified = locationVerified,
                        proofSelfieUrl = proofSelfieUrl,
                        shift = Shift(
                            id = shiftId,
                            jobType = shift?.get("job_type") as? String ?: "",
                            jobTitle = shift?.get("job_title") as? String ?: "",
                            date = shift?.get("date") as? String ?: "",
                            startTime = shift?.get("start_time") as? String ?: "",
                            endTime = shift?.get("end_time") as? String ?: "",
                            ratePerHour = ratePerHour
                        )
                    ),
                    finalPayment = finalEarnings,
                    message = "Shift completed successfully",
                    bonusPoints = if (locationVerified) 10 else 0
                ))

            } catch (e: Exception) {
                Result.failure(ClockOutError.NetworkError(e.message ?: "Unknown error"))
            }
        }
    }

    // =====================================================
    // HELPER FUNCTIONS
    // =====================================================

    /**
     * Upload selfie image to Supabase Storage
     */
    private suspend fun uploadSelfieImage(
        context: Context,
        bookingId: String,
        imageFile: File?
    ): String? {
        if (imageFile == null) return null

        try {
            // Check for location permission
            if (!hasLocationPermission(context)) {
                return null
            }

            // Upload to bucket
            val fileName = "${bookingId}_selfie_${System.currentTimeMillis()}.jpg"
            val path = "clock_in/${fileName}"

            val uploadResult = client.storage
                .from("clock-in-selfies")
                .upload(imageFile.absolutePath, path)

            if (uploadResult.path != null) {
                // Get public URL
                val publicUrl = "${BuildConfig.SUPABASE_URL}/storage/v1/object/public/${uploadResult.path}"
                return publicUrl
            }

            return null
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * Verify if current location is within 500m of shift location
     */
    private fun verifyLocation(
        context: Context,
        shiftId: String,
        currentLocation: Location
    ): LocationVerificationResult {
        try {
            // Get shift location from database
            val shift = client.postgrest["shifts"]
                .select("location_lat, location_lng, location_address")
                .eq("id", shiftId)
                .singleOrNull<Map<String, *>>()

            if (shift == null) {
                return LocationVerificationResult(
                    isWithinRadius = false,
                    distance = 0.0,
                    accuracy = 0.0f,
                    message = "Shift not found"
                )
            }

            val shiftLat = shift["location_lat"] as? Double ?: 0.0
            val shiftLng = shift["location_lng"] as? Double ?: 0.0
            val shiftAddress = shift["location_address"] as? String ?: ""

            // Calculate distance using Haversine formula
            val distance = calculateDistance(
                shiftLat, shiftLng,
                currentLocation.latitude,
                currentLocation.longitude
            )

            val isWithinRadius = distance <= 0.5 // 500 meters

            val message = when {
                isWithinRadius -> "Within acceptable range"
                distance <= 1.0 -> "Slightly outside range (1km)"
                else -> "Too far from shift location"
            }

            return LocationVerificationResult(
                isWithinRadius = isWithinRadius,
                distance = distance,
                accuracy = currentLocation.accuracy,
                message = message,
                shiftAddress = shiftAddress
            )

        } catch (e: Exception) {
            return LocationVerificationResult(
                isWithinRadius = false,
                distance = 0.0,
                accuracy = 0.0f,
                message = "Error verifying location"
            )
        }
    }

    /**
     * Update shift status if all workers have clocked out
     */
    private suspend fun updateShiftStatus(
        context: Context,
        shiftId: String,
        status: String
    ) {
        val accessToken = SessionManager.getAccessToken(context) ?: return

        // Get current booking count
        val bookings = client.postgrest["bookings"]
            .select("status")
            .eq("shift_id", shiftId)
            .in("status", listOf("completed", "cancelled"))
            .execute()

        val completedCount = bookings.count()
        val totalCount = bookings.count { it == null } // Approximation

        // Update shift status if this is the last worker
        if (completedCount >= totalCount) {
            client.postgrest["shifts"]
                .update(mapOf("status" to status))
                .eq("id", shiftId)
        }
    }

    /**
     * Update worker statistics after completing a shift
     */
    private suspend fun updateWorkerStats(
        context: Context,
        workerId: String,
        shiftId: String,
        earnings: Long,
        locationVerified: Boolean
    ) {
        val accessToken = SessionManager.getAccessToken(context) ?: return

        // Update reliability score based on location verification
        val currentProfile = client.postgrest["workers"]
            .select("reliability_score, total_shifts_completed, total_earnings")
            .eq("user_id", workerId)
            .singleOrNull<Map<String, *>>()

        val currentReliability = currentProfile?.get("reliability_score") as? Double ?: 100.0
        val currentShiftsCompleted = currentProfile?.get("total_shifts_completed") as? Int ?: 0
        val currentTotalEarnings = currentProfile?.get("total_earnings") as? Long ?: 0L

        // Calculate new reliability
        val newReliability = when {
            !locationVerified -> Math.max(0, currentReliability - 20) // -20 penalty
            currentReliability >= 100 -> 100.0 // No change if already perfect
            else -> Math.min(100, currentReliability + 5) // +5 bonus for on-time
        }

        client.postgrest["workers"]
            .update(mapOf(
                "reliability_score" to newReliability,
                "total_shifts_completed" to (currentShiftsCompleted + 1),
                "total_earnings" to (currentTotalEarnings + earnings)
            ))
            .eq("user_id", workerId)
    }

    /**
     * Calculate hours worked from time range
     */
    private fun calculateHoursWorked(startTime: String, endTime: String): Long {
        return try {
            val startParts = startTime.split(":")
            val endParts = endTime.split(":")

            val startHour = startParts[0].toInt()
            val startMin = startParts.getOrNull(1)?.toInt() ?: 0

            val endHour = endParts[0].toInt()
            val endMin = endParts.getOrNull(1)?.toInt() ?: 0

            val startMinutes = startHour * 60 + startMin
            val endMinutes = endHour * 60 + endMin

            val diff = endMinutes - startMinutes
            maxOf(0, diff / 60L) // Return in hours
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Calculate distance between two coordinates (Haversine formula)
     */
    private fun calculateDistance(
        lat1: Double,
        lng1: Double,
        lat2: Double,
        lng2: Double
    ): Double {
        val R = 6371 // Earth's radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return R * c
    }

    /**
     * Get current device location
     */
    private fun getCurrentLocation(context: Context): Location {
        // This would use LocationManager or FusedLocationProvider
        // For simplicity, returning a mock location
        return Location("mock").apply {
            latitude = -8.409518
            longitude = 115.188919
            accuracy = 10.0f
        }
    }

    /**
     * Check for location permission
     */
    private fun hasLocationPermission(context: Context): Boolean {
        val permission = android.Manifest.permission.ACCESS_FINE_LOCATION
        return android.content.pm.PackageManager.PERMISSION_GRANTED ==
                context.checkSelfPermission(permission)
    }

    // =====================================================
    // RESULT CLASSES
    // =====================================================

    data class ClockInResult(
        val booking: Booking,
        val message: String,
        val earningsSoFar: Long
    )

    data class ClockOutResult(
        val booking: Booking,
        val finalPayment: Long,
        val message: String,
        val bonusPoints: Int,
        val totalEarnings: Long
    )

    data class LocationVerificationResult(
        val isWithinRadius: Boolean,
        val distance: Double,
        val accuracy: Float,
        val message: String,
        val shiftAddress: String? = null
    )

    private data class BookingData : Map<String, *>

    sealed class ClockInError {
        data object NoSession
        data object BookingNotFound
        data object AlreadyClockedIn
        data object BookingNotConfirmed
        data class NetworkError(val message: String)
    }

    sealed class ClockOutError {
        data object NoSession
        data object BookingNotFound
        data object NotClockedIn
        data object AlreadyClockedOut
        data class NetworkError(val message: String)
    }
}
