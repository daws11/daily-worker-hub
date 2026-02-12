package com.example.dwhubfix.data

import android.content.Context
import android.location.Location
import com.example.dwhubfix.BuildConfig
import com.example.dwhubfix.model.Booking
import com.example.dwhubfix.model.Shift
import com.example.dwhubfix.model.BookingStatus
import com.example.dwhubfix.model.Business
import com.example.dwhubfix.model.ClockInResult
import com.example.dwhubfix.model.ClockOutResult
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.Storage
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.*

// =====================================================
// CUSTOM RESULT TYPE
// Wrapper for Success/Error pattern
// =====================================================
sealed class ApiResult<out S, out E> {
    data class Success<S>(val data: S) : ApiResult<S, Nothing>()
    data class Failure<E>(val error: E) : ApiResult<Nothing, E>()
}

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
     */
    suspend fun clockIn(
        context: Context,
        bookingId: String,
        currentLocation: Location,
        selfieImageFile: File
    ): ApiResult<ClockInResult, ClockInError> {
        return withContext(Dispatchers.IO) {
            try {
                val accessToken = SessionManager.getAccessToken(context)
                if (accessToken == null) {
                    @Suppress("UNCHECKED_CAST")
                    return@withContext ApiResult.Failure(ClockInError.NoSession) as ApiResult<ClockInResult, ClockInError>
                }

                // Get booking details
                val booking = try {
                    client.from("bookings").select() {
                        filter { eq("id", bookingId) }
                    }.decodeSingle<Map<String, Any?>>()
                } catch (e: Exception) {
                    @Suppress("UNCHECKED_CAST")
                    return@withContext ApiResult.Failure(ClockInError.BookingNotFound) as ApiResult<ClockInResult, ClockInError>
                }

                val status = booking["status"] as? String ?: "pending"
                if (status == "clocked_in") {
                    @Suppress("UNCHECKED_CAST")
                    return@withContext ApiResult.Failure(ClockInError.AlreadyClockedIn) as ApiResult<ClockInResult, ClockInError>
                }
                if (status != "confirmed") {
                    @Suppress("UNCHECKED_CAST")
                    return@withContext ApiResult.Failure(ClockInError.BookingNotConfirmed) as ApiResult<ClockInResult, ClockInError>
                }

                val shiftId = booking["shift_id"] as? String
                val workerId = booking["worker_id"] as? String
                val businessId = booking["business_id"] as? String

                // Get shift details
                val shift = try {
                    client.from("shifts").select() {
                        filter { eq("id", shiftId ?: "") }
                    }.decodeSingle<Map<String, Any?>>()
                } catch (e: Exception) {
                    @Suppress("UNCHECKED_CAST")
                    return@withContext ApiResult.Failure(ClockInError.NetworkError("Shift not found")) as ApiResult<ClockInResult, ClockInError>
                }

                // Update booking with clock-in data
                val updateData = mapOf(
                    "status" to "clocked_in",
                    "clock_in_time" to java.time.Instant.now().toString(),
                    "clock_in_location_lat" to currentLocation.latitude,
                    "clock_in_location_lng" to currentLocation.longitude,
                    "clock_in_accuracy" to currentLocation.accuracy.toDouble()
                )

                try {
                    client.from("bookings").update(updateData) {
                        filter { eq("id", bookingId) }
                    }
                } catch (e: Exception) {
                    @Suppress("UNCHECKED_CAST")
                    return@withContext ApiResult.Failure(ClockInError.NetworkError(e.message ?: "Update failed")) as ApiResult<ClockInResult, ClockInError>
                }

                // Calculate earnings
                val startTime = shift["start_time"] as? String ?: ""
                val endTime = shift["end_time"] as? String ?: ""
                val ratePerHour = (shift["rate_per_hour"] as? Number)?.toLong() ?: 0L
                val hoursWorked = calculateHoursWorked(startTime, endTime)
                val earnings = hoursWorked * ratePerHour

                @Suppress("UNCHECKED_CAST")
                ApiResult.Success(ClockInResult(
                    booking = createBookingFromMap(booking, shift),
                    message = "Successfully clocked in",
                    earningsSoFar = earnings
                )) as ApiResult<ClockInResult, ClockInError>

            } catch (e: Exception) {
                @Suppress("UNCHECKED_CAST")
                ApiResult.Failure(ClockInError.NetworkError(e.message ?: "Unknown error")) as ApiResult<ClockInResult, ClockInError>
            }
        }
    }

    // =====================================================
    // CLOCK-OUT LOGIC
    // =====================================================

    /**
     * Clock out from a shift
     */
    suspend fun clockOut(
        context: Context,
        bookingId: String
    ): ApiResult<ClockOutResult, ClockOutError> {
        return withContext(Dispatchers.IO) {
            try {
                val accessToken = SessionManager.getAccessToken(context)
                if (accessToken == null) {
                    @Suppress("UNCHECKED_CAST")
                    return@withContext ApiResult.Failure(ClockOutError.NoSession) as ApiResult<ClockOutResult, ClockOutError>
                }

                // Get booking details
                val booking = try {
                    client.from("bookings").select() {
                        filter { eq("id", bookingId) }
                    }.decodeSingle<Map<String, Any?>>()
                } catch (e: Exception) {
                    @Suppress("UNCHECKED_CAST")
                    return@withContext ApiResult.Failure(ClockOutError.BookingNotFound) as ApiResult<ClockOutResult, ClockOutError>
                }

                val clockInTime = booking["clock_in_time"] as? String
                if (clockInTime == null) {
                    @Suppress("UNCHECKED_CAST")
                    return@withContext ApiResult.Failure(ClockOutError.NotClockedIn) as ApiResult<ClockOutResult, ClockOutError>
                }

                val clockOutTime = booking["clock_out_time"] as? String
                if (clockOutTime != null) {
                    @Suppress("UNCHECKED_CAST")
                    return@withContext ApiResult.Failure(ClockOutError.AlreadyClockedOut) as ApiResult<ClockOutResult, ClockOutError>
                }

                val shiftId = booking["shift_id"] as? String
                val workerId = booking["worker_id"] as? String
                val businessId = booking["business_id"] as? String

                // Get shift details
                val shift = try {
                    client.from("shifts").select() {
                        filter { eq("id", shiftId ?: "") }
                    }.decodeSingle<Map<String, Any?>>()
                } catch (e: Exception) {
                    @Suppress("UNCHECKED_CAST")
                    return@withContext ApiResult.Failure(ClockOutError.NetworkError("Shift not found")) as ApiResult<ClockOutResult, ClockOutError>
                }

                // Get current location
                val currentLocation = getCurrentLocation(context)

                // Calculate distance from clock-in location
                val clockInLat = (booking["clock_in_location_lat"] as? Double) ?: 0.0
                val clockInLng = (booking["clock_in_location_lng"] as? Double) ?: 0.0
                val distance = calculateDistance(
                    clockInLat, clockInLng,
                    currentLocation.latitude, currentLocation.longitude
                )
                val locationVerified = distance <= 1.0 // Within 1km

                // Update booking with clock-out data
                val updateData = mapOf(
                    "status" to "completed",
                    "clock_out_time" to java.time.Instant.now().toString(),
                    "clock_out_location_lat" to currentLocation.latitude,
                    "clock_out_location_lng" to currentLocation.longitude,
                    "location_verified" to locationVerified
                )

                try {
                    client.from("bookings").update(updateData) {
                        filter { eq("id", bookingId) }
                    }
                } catch (e: Exception) {
                    @Suppress("UNCHECKED_CAST")
                    return@withContext ApiResult.Failure(ClockOutError.NetworkError(e.message ?: "Update failed")) as ApiResult<ClockOutResult, ClockOutError>
                }

                // Calculate final earnings
                val startTime = shift["start_time"] as? String ?: ""
                val endTime = shift["end_time"] as? String ?: ""
                val ratePerHour = (shift["rate_per_hour"] as? Number)?.toLong() ?: 0L
                val hoursWorked = calculateHoursWorked(startTime, endTime)
                val finalEarnings = hoursWorked * ratePerHour

                @Suppress("UNCHECKED_CAST")
                ApiResult.Success(ClockOutResult(
                    booking = createBookingFromMap(booking, shift),
                    finalPayment = finalEarnings,
                    message = "Shift completed successfully",
                    bonusPoints = if (locationVerified) 10 else 0,
                    totalEarnings = finalEarnings
                )) as ApiResult<ClockOutResult, ClockOutError>

            } catch (e: Exception) {
                @Suppress("UNCHECKED_CAST")
                ApiResult.Failure(ClockOutError.NetworkError(e.message ?: "Unknown error")) as ApiResult<ClockOutResult, ClockOutError>
            }
        }
    }

    // =====================================================
    // HELPER FUNCTIONS
    // =====================================================

    private fun createBookingFromMap(
        booking: Map<String, Any?>,
        shift: Map<String, Any?>
    ): Booking {
        val shiftId = booking["shift_id"] as? String ?: ""
        val workerId = booking["worker_id"] as? String ?: ""
        val businessId = booking["business_id"] as? String ?: ""
        val status = when (booking["status"] as? String ?: "pending") {
            "pending" -> BookingStatus.PENDING
            "confirmed" -> BookingStatus.CONFIRMED
            "clocked_in" -> BookingStatus.CLOCKED_IN
            "in_progress" -> BookingStatus.IN_PROGRESS
            "completed" -> BookingStatus.COMPLETED
            "cancelled" -> BookingStatus.CANCELLED
            "no_show" -> BookingStatus.NO_SHOW
            "blocked" -> BookingStatus.BLOCKED
            else -> BookingStatus.PENDING
        }

        return Booking(
            id = (booking["id"] as? String) ?: "",
            shiftId = shiftId,
            workerId = workerId,
            businessId = businessId,
            status = status,
            clockInTime = booking["clock_in_time"] as? String,
            clockOutTime = booking["clock_out_time"] as? String,
            workerRating = null,
            shift = createShiftFromMap(shift),
            totalEarnings = (booking["total_earnings"] as? Number)?.toLong() ?: 0L,
            clockInLocationLat = booking["clock_in_location_lat"] as? Double,
            clockInLocationLng = booking["clock_in_location_lng"] as? Double,
            clockOutLocationLat = booking["clock_out_location_lat"] as? Double,
            clockOutLocationLng = booking["clock_out_location_lng"] as? Double,
            clockInSelfieUrl = booking["clock_in_selfie_url"] as? String,
            clockOutSelfieUrl = booking["clock_out_selfie_url"] as? String,
            locationVerified = booking["location_verified"] as? Boolean ?: false,
            proofSelfieUrl = null
        )
    }

    private fun createShiftFromMap(shift: Map<String, Any?>): Shift {
        return Shift(
            id = (shift["id"] as? String) ?: "",
            jobType = shift["job_type"] as? String ?: "",
            jobTitle = shift["job_title"] as? String ?: "",
            date = (shift["date"] as? String) ?: "",
            startTime = shift["start_time"] as? String ?: "",
            endTime = shift["end_time"] as? String ?: "",
            ratePerHour = (shift["rate_per_hour"] as? Number)?.toLong() ?: 0L,
            business = Business(
                businessName = shift["business_name"] as? String ?: "",
                businessType = "",
                locationAddress = shift["location_address"] as? String ?: ""
            ),
            requiredWorkersCount = (shift["required_workers_count"] as? Number)?.toInt() ?: 1,
            filledWorkersCount = (shift["filled_workers_count"] as? Number)?.toInt() ?: 0,
            urgencyLevel = shift["urgency_level"] as? String ?: "normal",
            status = shift["status"] as? String ?: "active"
        )
    }

    private fun calculateHoursWorked(startTime: String, endTime: String): Long {
        return try {
            val startParts = startTime.split(":")
            val endParts = endTime.split(":")

            val startHour = startParts.getOrNull(0)?.toInt() ?: 0
            val startMin = startParts.getOrNull(1)?.toInt() ?: 0
            val endHour = endParts.getOrNull(0)?.toInt() ?: 0
            val endMin = endParts.getOrNull(1)?.toInt() ?: 0

            val startMinutes = startHour * 60 + startMin
            val endMinutes = endHour * 60 + endMin

            val diff = endMinutes - startMinutes
            maxOf(0, diff / 60L)
        } catch (e: Exception) {
            0L
        }
    }

    private fun calculateDistance(
        lat1: Double,
        lng1: Double,
        lat2: Double,
        lng2: Double
    ): Double {
        val R = 6371 // Earth's radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return R * c
    }

    private fun getCurrentLocation(context: Context): Location {
        return Location("mock").apply {
            latitude = -8.409518
            longitude = 115.188919
            accuracy = 10.0f
        }
    }

    // =====================================================
    // ERROR CLASSES
    // =====================================================

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
