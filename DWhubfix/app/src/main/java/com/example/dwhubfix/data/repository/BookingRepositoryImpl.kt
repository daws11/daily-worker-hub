package com.example.dwhubfix.data.repository

import android.content.Context
import android.location.Location
import com.example.dwhubfix.core.location.LocationManager
import com.example.dwhubfix.core.location.LocationUnavailableException
import com.example.dwhubfix.core.network.SupabaseClient
import com.example.dwhubfix.data.SessionManager
import com.example.dwhubfix.model.Booking
import com.example.dwhubfix.model.BookingStatus
import com.example.dwhubfix.model.Business
import com.example.dwhubfix.model.ClockInResult
import com.example.dwhubfix.model.ClockOutResult
import com.example.dwhubfix.model.Shift
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.sqrt
import kotlin.math.atan2
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.pow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Booking Repository
 *
 * Handles booking operations including clock-in/clock-out with geolocation verification.
 * Uses injected dependencies for better testability.
 */
@Singleton
class BookingRepository @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val locationManager: LocationManager,
    @ApplicationContext private val context: Context
) {
    private val client get() = supabaseClient.client

    // =====================================================
    // CLOCK-IN LOGIC
    // =====================================================

    /**
     * Clock in to a shift with geolocation and selfie
     *
     * @param bookingId The booking ID
     * @param currentLocation Current location of the worker
     * @param selfieImageFile Selfie image file for verification
     * @return ApiResult containing ClockInResult on success
     */
    suspend fun clockIn(
        bookingId: String,
        currentLocation: Location,
        selfieImageFile: File
    ): ApiResult<ClockInResult, ClockInError> {
        return withContext(Dispatchers.IO) {
            try {
                // Verify authentication
                if (!isAuthenticated()) {
                    return@withContext ApiResult.Failure(ClockInError.NoSession)
                }

                // Get booking details
                val booking = getBookingById(bookingId)
                    ?: return@withContext ApiResult.Failure(ClockInError.BookingNotFound)

                // Validate booking status
                when (booking["status"] as? String ?: "pending") {
                    "clocked_in" -> return@withContext ApiResult.Failure(ClockInError.AlreadyClockedIn)
                    "confirmed" -> { /* OK to proceed */ }
                    else -> return@withContext ApiResult.Failure(ClockInError.BookingNotConfirmed)
                }

                val shiftId = booking["shift_id"] as? String

                // Get shift details
                val shift = getShiftById(shiftId ?: "")
                    ?: return@withContext ApiResult.Failure(
                        ClockInError.NetworkError("Shift not found")
                    )

                // Update booking with clock-in data
                val updateData = mapOf(
                    "status" to "clocked_in",
                    "clock_in_time" to java.time.Instant.now().toString(),
                    "clock_in_location_lat" to currentLocation.latitude,
                    "clock_in_location_lng" to currentLocation.longitude,
                    "clock_in_accuracy" to currentLocation.accuracy.toDouble()
                )

                updateBooking(bookingId, updateData)

                // Calculate earnings
                val earnings = calculateEarnings(shift)

                ApiResult.Success(
                    ClockInResult(
                        booking = createBookingFromMap(booking, shift),
                        message = "Successfully clocked in",
                        earningsSoFar = earnings
                    )
                )

            } catch (e: Exception) {
                ApiResult.Failure(ClockInError.NetworkError(e.message ?: "Unknown error"))
            }
        }
    }

    // =====================================================
    // CLOCK-OUT LOGIC
    // =====================================================

    /**
     * Clock out from a shift
     *
     * @param bookingId The booking ID
     * @return ApiResult containing ClockOutResult on success
     */
    suspend fun clockOut(
        bookingId: String
    ): ApiResult<ClockOutResult, ClockOutError> {
        return withContext(Dispatchers.IO) {
            try {
                // Verify authentication
                if (!isAuthenticated()) {
                    return@withContext ApiResult.Failure(ClockOutError.NoSession)
                }

                // Get booking details
                val booking = getBookingById(bookingId)
                    ?: return@withContext ApiResult.Failure(ClockOutError.BookingNotFound)

                // Validate clock-in state
                val clockInTime = booking["clock_in_time"] as? String
                if (clockInTime == null) {
                    return@withContext ApiResult.Failure(ClockOutError.NotClockedIn)
                }

                val clockOutTime = booking["clock_out_time"] as? String
                if (clockOutTime != null) {
                    return@withContext ApiResult.Failure(ClockOutError.AlreadyClockedOut)
                }

                val shiftId = booking["shift_id"] as? String

                // Get shift details
                val shift = getShiftById(shiftId ?: "")
                    ?: return@withContext ApiResult.Failure(
                        ClockOutError.NetworkError("Shift not found")
                    )

                // Get current location using LocationManager
                val currentLocationResult = locationManager.getCurrentLocation()
                val currentLocation = when {
                    currentLocationResult.isSuccess -> currentLocationResult.getOrNull()!!
                    else -> locationManager.getDefaultLocation()
                }

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

                updateBooking(bookingId, updateData)

                // Calculate final earnings
                val finalEarnings = calculateEarnings(shift)

                ApiResult.Success(
                    ClockOutResult(
                        booking = createBookingFromMap(booking, shift),
                        finalPayment = finalEarnings,
                        message = "Shift completed successfully",
                        bonusPoints = if (locationVerified) 10 else 0,
                        totalEarnings = finalEarnings
                    )
                )

            } catch (e: Exception) {
                ApiResult.Failure(ClockOutError.NetworkError(e.message ?: "Unknown error"))
            }
        }
    }

    // =====================================================
    // HELPER FUNCTIONS
    // =====================================================

    private suspend fun getBookingById(bookingId: String): Map<String, Any?>? {
        return try {
            client.from("bookings").select() {
                filter { eq("id", bookingId) }
            }.decodeSingle<Map<String, Any?>>()
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun getShiftById(shiftId: String): Map<String, Any?>? {
        return if (shiftId.isBlank()) null
        else try {
            client.from("shifts").select() {
                filter { eq("id", shiftId) }
            }.decodeSingle<Map<String, Any?>>()
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun updateBooking(
        bookingId: String,
        data: Map<String, Any?>
    ) {
        client.from("bookings").update(data) {
            filter { eq("id", bookingId) }
        }
    }

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

    private fun calculateEarnings(shift: Map<String, Any?>): Long {
        val startTime = shift["start_time"] as? String ?: ""
        val endTime = shift["end_time"] as? String ?: ""
        val ratePerHour = (shift["rate_per_hour"] as? Number)?.toLong() ?: 0L
        val hoursWorked = calculateHoursWorked(startTime, endTime)
        return hoursWorked * ratePerHour
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

        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))

        return R * c
    }

    private fun isAuthenticated(): Boolean {
        return SessionManager.getAccessToken(context) != null
    }
}
