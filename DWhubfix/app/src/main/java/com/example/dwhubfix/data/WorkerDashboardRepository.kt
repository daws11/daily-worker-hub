// =====================================================
// UPDATE SUPABASE REPOSITORY
// Adding worker dashboard API integration methods
// =====================================================

// Add these methods to SupabaseRepository object:

/**
 * Get worker statistics (shifts, earnings, rating, reliability)
 * @param userId Worker user ID
 * @return WorkerStats object
 */
suspend fun getWorkerStats(userId: String): WorkerStats {
    // Get worker profile
    val workerProfile = client.postgrest["workers"]
        .select("*, users(email)")
        .eq("user_id", userId)
        .singleOrNull<WorkerProfile>()

    // Get wallet balance
    val wallet = client.postgrest["wallets"]
        .select("*")
        .eq("user_id", userId)
        .singleOrNull<Wallet>()

    // Calculate stats
    return WorkerStats(
        totalShiftsCompleted = workerProfile?.total_shifts_completed ?: 0,
        totalEarnings = wallet?.total_earned ?: 0L,
        walletBalance = wallet?.balance ?: 0L,
        frozenAmount = wallet?.frozen_amount ?: 0L,
        ratingAvg = workerProfile?.rating_avg ?: 0.0,
        ratingCount = workerProfile?.rating_count ?: 0,
        reliabilityScore = workerProfile?.reliability_score ?: 100.0,
        tier = workerProfile?.tier ?: "bronze"
    )
}

/**
 * Get worker bookings history
 * @param userId Worker user ID
 * @param limit Max number of bookings to return
 * @return List of Booking objects
 */
suspend fun getWorkerBookings(userId: String, limit: Int = 20): List<Booking> {
    return client.postgrest["bookings"]
        .select("""
            *,
            shifts (
                *,
                businesses (
                    business_name,
                    business_type,
                    location_address
                )
            )
        """)
        .eq("worker_id", userId)
        .order("created_at", descending = true)
        .limit(limit)
        .execute()
        .map { it.toBooking() }
        .toList()
}

/**
 * Get upcoming jobs (confirmed, pending shifts not yet started)
 * @param userId Worker user ID
 * @param limit Max number of bookings to return
 * @return List of upcoming Shift objects
 */
suspend fun getUpcomingShifts(userId: String, limit: Int = 10): List<Shift> {
    val currentDate = java.time.LocalDate.now().toString()

    return client.postgrest["bookings"]
        .select("""
            shifts (*)
        """)
        .eq("worker_id", userId)
        .in("status", listOf("confirmed", "pending"))
        .gte("shifts.date", currentDate)
        .order("shifts.date", ascending = true)
        .order("shifts.start_time", ascending = true)
        .limit(limit)
        .execute()
        .map { it.toShift() }
        .toList()
}

// =====================================================
// HELPER EXTENSION FUNCTIONS
// Add these to SupabaseRepository.kt (outside the object)
// =====================================================

/**
 * Extension function to convert Postgrest response to Booking
 */
private fun Map<*, *>.toBooking(): Booking {
    val id = this["id"] as? String ?: ""
    val shiftId = this["shift_id"] as? String ?: ""
    val workerId = this["worker_id"] as? String ?: ""
    val businessId = this["business_id"] as? String ?: ""
    val status = this["status"] as? String ?: "pending"
    val clockInTime = this["clock_in_time"] as? String ?: ""
    val clockOutTime = this["clock_out_time"] as? String ?: ""
    val workerRating = this["worker_rating"] as? Double ?: 0.0
    val createdAt = this["created_at"] as? String ?: ""

    // Get shift details
    val shift = this["shifts"] as? Map<*, *>
    val jobTitle = shift?.get("job_title") as? String ?: ""
    val date = shift?.get("date") as? String ?: ""
    val startTime = shift?.get("start_time") as? String ?: ""
    val endTime = shift?.get("end_time") as? String ?: ""
    val ratePerHour = shift?.get("rate_per_hour") as? Long ?: 0L

    // Get business details
    val businesses = shift?.get("businesses") as? Map<*, *>
    val businessName = businesses?.get("business_name") as? String ?: ""

    val totalEarnings = if (status == "completed") {
        val hoursWorked = calculateHours(startTime, endTime)
        hoursWorked * ratePerHour
    } else 0L

    return Booking(
        id = id,
        shiftId = shiftId,
        workerId = workerId,
        businessId = businessId,
        status = status,
        clockInTime = clockInTime,
        clockOutTime = clockOutTime,
        workerRating = workerRating,
        shift = Shift(
            id = shiftId,
            jobTitle = jobTitle,
            date = date,
            startTime = startTime,
            endTime = endTime,
            ratePerHour = ratePerHour,
            business = Business(
                businessName = businessName
            )
        ),
        totalEarnings = totalEarnings,
        createdAt = createdAt
    )
}

/**
 * Extension function to convert Postgrest response to Shift
 */
private fun Map<*, *>.toShift(): Shift {
    val id = this["id"] as? String ?: ""
    val jobType = this["job_type"] as? String ?: ""
    val jobTitle = this["job_title"] as? String ?: ""
    val date = this["date"] as? String ?: ""
    val startTime = this["start_time"] as? String ?: ""
    val endTime = this["end_time"] as? String ?: ""
    val ratePerHour = this["rate_per_hour"] as? Long ?: 0L

    val businesses = this["businesses"] as? Map<*, *>
    val businessName = businesses?.get("business_name") as? String ?: ""

    return Shift(
        id = id,
        jobType = jobType,
        jobTitle = jobTitle,
        date = date,
        startTime = startTime,
        endTime = endTime,
        ratePerHour = ratePerHour,
        business = Business(
            businessName = businessName
        )
    )
}

/**
 * Calculate hours worked from start and end time
 */
private fun calculateHours(startTime: String, endTime: String): Long {
    return try {
        val startParts = startTime.split(":")
        val endParts = endTime.split(":")
        
        val startHour = startParts[0].toInt()
        val startMin = startParts.getOrNull(1)?.toInt() ?: 0
        val startSec = startParts.getOrNull(2)?.toInt() ?: 0
        
        val endHour = endParts[0].toInt()
        val endMin = endParts.getOrNull(1)?.toInt() ?: 0
        val endSec = endParts.getOrNull(2)?.toInt() ?: 0
        
        val startTotalSeconds = startHour * 3600L + startMin * 60L + startSec
        val endTotalSeconds = endHour * 3600L + endMin * 60L + endSec
        
        val diff = endTotalSeconds - startTotalSeconds
        
        diff / 3600L // Convert to hours
    } catch (e: Exception) {
        0L
    }
}

// =====================================================
// MODEL CLASSES (if not already created)
// =====================================================

/**
 * Booking data class
 */
data class Booking(
    val id: String,
    val shiftId: String,
    val workerId: String,
    val businessId: String,
    val status: String,
    val clockInTime: String,
    val clockOutTime: String,
    val workerRating: Double,
    val shift: Shift,
    val totalEarnings: Long,
    val createdAt: String
)

/**
 * Shift data class
 */
data class Shift(
    val id: String,
    val jobType: String,
    val jobTitle: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val ratePerHour: Long,
    val business: Business
)

/**
 * Business data class
 */
data class Business(
    val businessName: String
)

/**
 * Worker profile data class
 */
private data class WorkerProfile(
    val user_id: String,
    val full_name: String,
    val display_name: String?,
    val total_shifts_completed: Int,
    val total_earnings: Long,
    val rating_avg: Double,
    val rating_count: Int,
    val reliability_score: Double,
    val tier: String
)

/**
 * Wallet data class
 */
private data class Wallet(
    val balance: Long,
    val frozen_amount: Long,
    val total_earned: Long
)

/**
 * Shifts data class (for type checking)
 */
private interface Shifts {
    val job_title: String,
    val start_time: String,
    val end_time: String,
    val rate_per_hour: Long,
    val business_name: String
}
