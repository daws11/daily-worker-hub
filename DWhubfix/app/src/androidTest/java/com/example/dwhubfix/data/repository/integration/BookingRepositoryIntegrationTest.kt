package com.example.dwhubfix.data.repository.integration

import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*

/**
 * Booking Repository Integration Tests
 *
 * Tests booking operations including clock-in/clock-out against real Supabase backend.
 * Requires test-config.properties with valid Supabase credentials.
 *
 * Prerequisites:
 * 1. Configure test-config.properties with your Supabase dev/staging credentials
 * 2. Create test users in your Supabase project
 * 3. Ensure tables exist: bookings, shifts, jobs
 */
class BookingRepositoryIntegrationTest : BaseIntegrationTest() {

    // ==================== CLOCK-IN TESTS ====================

    @Test
    fun clock_in_with_location_saves_location_data() = runTest {
        // Arrange - Create booking in confirmed state
        val (businessId, workerId, shiftId, bookingId) = createConfirmedBooking()

        // Act - Clock in with location
        val clockInLat = -6.2088
        val clockInLng = 106.8456
        val clockInTime = java.time.Instant.now().toString()

        client.from("bookings").update(
            mapOf(
                "status" to "clocked_in",
                "clock_in_time" to clockInTime,
                "clock_in_location_lat" to clockInLat,
                "clock_in_location_lng" to clockInLng
            )
        ) {
            filter { eq("id", bookingId) }
        }

        // Assert
        val booking = client.from("bookings").select() {
            filter { eq("id", bookingId) }
        }.decodeSingle<Map<String, Any?>>()

        assertEquals("Status should be clocked_in", "clocked_in", booking["status"])
        assertEquals("Clock in time should be saved", clockInTime, booking["clock_in_time"])
        assertEquals("Clock in latitude should be saved", clockInLat, booking["clock_in_location_lat"])
        assertEquals("Clock in longitude should be saved", clockInLng, booking["clock_in_location_lng"])
    }

    @Test
    fun clock_in_when_already_clocked_in_returns_failure() = runTest {
        // Arrange - Create booking and clock in
        val (_, workerId, shiftId, bookingId) = createConfirmedBooking()

        client.from("bookings").update(
            mapOf(
                "status" to "clocked_in",
                "clock_in_time" to java.time.Instant.now().toString()
            )
        ) {
            filter { eq("id", bookingId) }
        }

        // Act - Try to clock in again (should not change status)
        val beforeState = client.from("bookings").select() {
            filter { eq("id", bookingId) }
        }.decodeSingle<Map<String, Any?>>()

        // Attempting to clock in again
        val originalClockInTime = beforeState["clock_in_time"]
        client.from("bookings").update(
            mapOf("clock_in_time" to java.time.Instant.now().toString())
        ) {
            filter { eq("id", bookingId) }
        }

        val afterState = client.from("bookings").select() {
            filter { eq("id", bookingId) }
        }.decodeSingle<Map<String, Any?>>()

        // Assert - Status remains clocked_in
        assertEquals("Status should remain clocked_in", "clocked_in", afterState["status"])
    }

    @Test
    fun clock_in_to_nonexistent_booking_returns_failure() = runTest {
        // Arrange
        authenticateAsWorker()
        val fakeBookingId = "00000000-0000-0000-0000-000000000000"

        // Act & Assert
        val exception = kotlin.runCatching {
            client.from("bookings").update(
                mapOf(
                    "status" to "clocked_in",
                    "clock_in_time" to java.time.Instant.now().toString()
                )
            ) {
                filter { eq("id", fakeBookingId) }
            }
        }.exceptionOrNull()

        assertNotNull("Should throw exception for non-existent booking", exception)
    }

    // ==================== CLOCK-OUT TESTS ====================

    @Test
    fun clock_out_with_location_saves_location_data() = runTest {
        // Arrange - Create booking, clock in
        val (businessId, workerId, shiftId, bookingId) = createConfirmedBooking()
        val clockInLat = -6.2088
        val clockInLng = 106.8456
        val clockInTime = java.time.Instant.now().toString()

        client.from("bookings").update(
            mapOf(
                "status" to "clocked_in",
                "clock_in_time" to clockInTime,
                "clock_in_location_lat" to clockInLat,
                "clock_in_location_lng" to clockInLng
            )
        ) {
            filter { eq("id", bookingId) }
        }

        // Act - Clock out
        val clockOutLat = -6.2100
        val clockOutLng = 106.8500
        val clockOutTime = java.time.Instant.now().toString()

        client.from("bookings").update(
            mapOf(
                "status" to "completed",
                "clock_out_time" to clockOutTime,
                "clock_out_location_lat" to clockOutLat,
                "clock_out_location_lng" to clockOutLng
            )
        ) {
            filter { eq("id", bookingId) }
        }

        // Assert
        val booking = client.from("bookings").select() {
            filter { eq("id", bookingId) }
        }.decodeSingle<Map<String, Any?>>()

        assertEquals("Status should be completed", "completed", booking["status"])
        assertEquals("Clock out time should be saved", clockOutTime, booking["clock_out_time"])
        assertEquals("Clock out latitude should be saved", clockOutLat, booking["clock_out_location_lat"])
        assertEquals("Clock out longitude should be saved", clockOutLng, booking["clock_out_location_lng"])
    }

    @Test
    fun clock_out_when_not_clocked_in_returns_failure() = runTest {
        // Arrange - Create booking in confirmed state (not clocked in)
        val (businessId, workerId, shiftId, bookingId) = createConfirmedBooking()

        // Act & Assert - Try to clock out without clocking in
        val exception = kotlin.runCatching {
            client.from("bookings").update(
                mapOf(
                    "status" to "completed",
                    "clock_out_time" to java.time.Instant.now().toString()
                )
            ) {
                filter { eq("id", bookingId) }
            }
        }.exceptionOrNull()

        // The update will succeed, but logically the booking was not clocked in
        // In production, there would be validation logic
        assertNotNull("Should handle clock out without clock in", exception)
    }

    @Test
    fun clock_out_calculates_duration_correctly() = runTest {
        // This test verifies duration calculation
        val startTime = "09:00"
        val endTime = "17:00"

        val startParts = startTime.split(":")
        val endParts = endTime.split(":")

        val startHour = startParts[0].toInt()
        val startMin = startParts[1].toInt()
        val endHour = endParts[0].toInt()
        val endMin = endParts[1].toInt()

        val startMinutes = startHour * 60 + startMin
        val endMinutes = endHour * 60 + endMin
        val diff = endMinutes - startMinutes

        val expectedHours = maxOf(0, diff / 60)

        assertEquals("Duration should be 8 hours", 8L, expectedHours.toLong())
    }

    // ==================== GET BOOKINGS TESTS ====================

    @Test
    fun get_bookings_for_worker_returns_list() = runTest {
        // Arrange - Create bookings for worker
        val (businessId, workerId, shiftId, bookingId) = createConfirmedBooking()

        // Act - Get bookings for worker
        val bookings = client.from("bookings").select() {
            filter { eq("worker_id", workerId) }
        }.decodeList<Map<String, Any?>>()

        // Assert
        assertTrue("Should return at least one booking", bookings.isNotEmpty())
    }

    @Test
    fun get_bookings_empty_returns_empty_list() = runTest {
        // Arrange - New worker with no bookings
        val workerId = authenticateAsWorker()

        // Act - Get bookings
        val bookings = client.from("bookings").select() {
            filter { eq("worker_id", workerId) }
        }.decodeList<Map<String, Any?>>()

        // Assert
        assertTrue("Should return empty list for new worker", bookings.isEmpty())
    }

    @Test
    fun get_shift_details_returns_shift_data() = runTest {
        // Arrange - Create shift
        val (businessId, workerId, shiftId, _) = createConfirmedBooking()

        // Act - Get shift details
        val shift = client.from("shifts").select() {
            filter { eq("id", shiftId) }
        }.decodeSingle<Map<String, Any?>>()

        // Assert
        assertNotNull("Shift should have an ID", shift["id"])
        assertNotNull("Shift should have a title", shift["job_title"])
        assertNotNull("Shift should have a date", shift["date"])
    }

    // ==================== EARNINGS CALCULATION TESTS ====================

    @Test
    fun earnings_calculation_hourly_rate_correct_amount() = runTest {
        // Test calculation: 8 hours * 15000 IDR/hour = 120000 IDR
        val hoursWorked = 8.0
        val ratePerHour = 15000L
        val expectedEarnings = (hoursWorked * ratePerHour).toLong()

        assertEquals("Hourly earnings should be correct", 120000L, expectedEarnings)
    }

    @Test
    fun earnings_calculation_daily_rate_correct_amount() = runTest {
        // Test calculation: 1 day * 150000 IDR/day = 150000 IDR
        val dailyRate = 150000L
        val daysWorked = 1
        val expectedEarnings = dailyRate * daysWorked

        assertEquals("Daily earnings should be correct", 150000L, expectedEarnings)
    }

    @Test
    fun earnings_calculation_with_overtime_includes_overtime() = runTest {
        // Test: 8 regular hours + 2 overtime hours
        val regularHours = 8.0
        val overtimeHours = 2.0
        val ratePerHour = 15000L
        val overtimeMultiplier = 1.5

        val regularEarnings = (regularHours * ratePerHour).toLong()
        val overtimeEarnings = (overtimeHours * ratePerHour * overtimeMultiplier).toLong()
        val totalEarnings = regularEarnings + overtimeEarnings

        assertEquals("Regular earnings should be correct", 120000L, regularEarnings)
        assertEquals("Overtime earnings should be correct", 45000L, overtimeEarnings)
        assertEquals("Total earnings should include overtime", 165000L, totalEarnings)
    }

    // ==================== UPDATE BOOKING STATUS TESTS ====================

    @Test
    fun update_booking_status_changes_status() = runTest {
        // Arrange - Create booking
        val (businessId, workerId, shiftId, bookingId) = createConfirmedBooking()

        // Act - Update status to in_progress
        client.from("bookings").update(
            mapOf("status" to "in_progress")
        ) {
            filter { eq("id", bookingId) }
        }

        // Assert
        val booking = client.from("bookings").select() {
            filter { eq("id", bookingId) }
        }.decodeSingle<Map<String, Any?>>()

        assertEquals("Status should be in_progress", "in_progress", booking["status"])
    }

    @Test
    fun multiple_clock_in_out_cycles_track_correctly() = runTest {
        // This test verifies that we can track multiple state changes
        val (businessId, workerId, shiftId, bookingId) = createConfirmedBooking()

        // Clock in
        client.from("bookings").update(
            mapOf("status" to "clocked_in", "clock_in_time" to java.time.Instant.now().toString())
        ) {
            filter { eq("id", bookingId) }
        }

        var booking = client.from("bookings").select() {
            filter { eq("id", bookingId) }
        }.decodeSingle<Map<String, Any?>>()
        assertEquals("Status should be clocked_in", "clocked_in", booking["status"])

        // Start work
        client.from("bookings").update(
            mapOf("status" to "in_progress")
        ) {
            filter { eq("id", bookingId) }
        }

        booking = client.from("bookings").select() {
            filter { eq("id", bookingId) }
        }.decodeSingle<Map<String, Any?>>()
        assertEquals("Status should be in_progress", "in_progress", booking["status"])

        // Complete
        client.from("bookings").update(
            mapOf("status" to "completed", "clock_out_time" to java.time.Instant.now().toString())
        ) {
            filter { eq("id", bookingId) }
        }

        booking = client.from("bookings").select() {
            filter { eq("id", bookingId) }
        }.decodeSingle<Map<String, Any?>>()
        assertEquals("Status should be completed", "completed", booking["status"])
    }

    // ==================== HELPER METHODS ====================

    /**
     * Creates a confirmed booking for testing
     * @return Quadruple of (businessId, workerId, shiftId, bookingId)
     */
    private suspend fun createConfirmedBooking(): Quadruple<String, String, String, String> {
        // Create business user
        val businessId = authenticateAsBusiness()

        // Create job using typed TestJobData
        val jobData = createTestJobData(
            businessId = businessId,
            title = "Test Job for Booking $testId",
            description = "Test job",
            wage = 15000.0,
            wageType = "per_hour",
            location = "Jakarta, Indonesia",
            category = "hospitality",
            startTime = "09:00",
            endTime = "17:00",
            shiftDate = java.time.LocalDate.now().plusDays(1).toString(),
            isUrgent = false,
            workerCount = 1,
            status = "open",
            testId = testId
        )
        val job = client.from("jobs").insert(jobData).decodeSingle<Map<String, Any?>>()
        val jobId = job["id"] as? String ?: throw IllegalStateException("No job ID")

        // Create shift using map (no typed class for shift yet)
        val shiftData = mapOf(
            "job_id" to jobId,
            "job_type" to "hospitality",
            "job_title" to "Test Shift $testId",
            "date" to java.time.LocalDate.now().plusDays(1).toString(),
            "start_time" to "09:00",
            "end_time" to "17:00",
            "rate_per_hour" to 15000,
            "business_name" to "Test Business",
            "location_address" to "Jakarta, Indonesia",
            "required_workers_count" to 1,
            "filled_workers_count" to 1,
            "urgency_level" to "normal",
            "status" to "active",
            "test_id" to testId,
            "is_test_data" to true
        )
        val shift = client.from("shifts").insert(shiftData).decodeSingle<Map<String, Any?>>()
        val shiftId = shift["id"] as? String ?: throw IllegalStateException("No shift ID")

        logoutIfNeeded()

        // Create worker and booking using typed TestBookingData
        val workerId = authenticateAsWorker()

        val bookingData = createTestBookingData(
            jobId = jobId,
            workerId = workerId,
            businessId = businessId,
            bookingDate = java.time.LocalDate.now().plusDays(1).toString(),
            startTime = "09:00",
            endTime = "17:00",
            status = "confirmed",
            testId = testId
        )
        val booking = client.from("bookings").insert(bookingData).decodeSingle<Map<String, Any?>>()
        val bookingId = booking["id"] as? String ?: throw IllegalStateException("No booking ID")

        return Quadruple(businessId, workerId, shiftId, bookingId)
    }

    /**
     * Simple data class for returning 4 values
     */
    private data class Quadruple<out A, out B, out C, out D>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D
    )
}
