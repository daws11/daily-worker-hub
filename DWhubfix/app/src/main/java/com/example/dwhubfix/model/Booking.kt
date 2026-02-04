package com.example.dwhubfix.model

/**
 * Booking model used in Worker Dashboard and Booking Flow
 * Contains shift details and booking status
 */
data class Booking(
    val id: String,
    val shiftId: String,
    val workerId: String,
    val businessId: String,
    val status: BookingStatus,
    val clockInTime: String?,
    val clockOutTime: String?,
    val workerRating: Double?,
    val shift: Shift,
    val totalEarnings: Long,
    val clockInLocationLat: Double?,
    val clockInLocationLng: Double?,
    val clockOutLocationLat: Double?,
    val clockOutLocationLng: Double?,
    val clockInSelfieUrl: String?,
    val clockOutSelfieUrl: String?,
    val locationVerified: Boolean,
    val proofSelfieUrl: String?
) {
    val formattedEarnings: String
        get() = "Rp ${String.format("%,d", totalEarnings)}"
}

/**
 * Shift model with full details for booking display
 */
data class Shift(
    val id: String,
    val jobType: String,
    val jobTitle: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val ratePerHour: Long,
    val business: Business,
    val requiredWorkersCount: Int,
    val filledWorkersCount: Int,
    val urgencyLevel: String,
    val status: String
)

/**
 * Business model
 */
data class Business(
    val businessName: String,
    val businessType: String,
    val locationAddress: String
)

/**
 * Booking Status Enum
 */
enum class BookingStatus {
    PENDING,
    CONFIRMED,
    CLOCKED_IN,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    NO_SHOW,
    BLOCKED
}

/**
 * Clock In Result for UI
 */
data class ClockInResult(
    val booking: Booking,
    val message: String,
    val earningsSoFar: Long
)

/**
 * Clock Out Result for UI
 */
data class ClockOutResult(
    val booking: Booking,
    val finalPayment: Long,
    val message: String,
    val bonusPoints: Int,
    val totalEarnings: Long
)

/**
 * Wallet model for worker dashboard
 */
data class Wallet(
    val balance: Long = 0L,
    val frozenAmount: Long = 0L,
    val currency: String = "IDR"
) {
    val availableBalance: Long
        get() = balance - frozenAmount

    val formattedBalance: String
        get() = "Rp ${String.format("%,d", balance)}"

    val formattedAvailableBalance: String
        get() = "Rp ${String.format("%,d", availableBalance)}"
}
