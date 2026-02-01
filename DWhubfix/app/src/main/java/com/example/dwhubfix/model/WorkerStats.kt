package com.example.dwhubfix.model

/**
 * Worker Statistics Data Class
 * Used in Worker Dashboard to display earnings, ratings, and reliability
 */
data class WorkerStats(
    val totalShiftsCompleted: Int = 0,
    val totalEarnings: Long = 0L,
    val walletBalance: Long = 0L,
    val frozenAmount: Long = 0L,
    val ratingAvg: Double = 0.0,
    val ratingCount: Int = 0,
    val reliabilityScore: Double = 100.0,
    val tier: String = "bronze"
) {
    val formattedBalance: String
        get() = "Rp ${String.format("%,d", walletBalance)}"
}
}
