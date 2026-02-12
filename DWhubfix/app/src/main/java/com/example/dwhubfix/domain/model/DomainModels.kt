package com.example.dwhubfix.domain.model

/**
 * Domain Models
 *
 * These are pure Kotlin data classes that represent the core business entities.
 * They are independent of any framework or data source implementation.
 */

/**
 * User Profile
 *
 * Represents a user's profile data in the domain layer
 */
data class UserProfile(
    val id: String,
    val fullName: String?,
    val email: String?,
    val phoneNumber: String?,
    val avatarUrl: String?,
    val role: String?, // "worker" or "business"
    val createdAt: String?,
    val updatedAt: String?
) {
    companion object {
        fun empty() = UserProfile(
            id = "",
            fullName = null,
            email = null,
            phoneNumber = null,
            avatarUrl = null,
            role = null,
            createdAt = null,
            updatedAt = null
        )
    }

    val isWorker: Boolean get() = role == "worker"
    val isBusiness: Boolean get() = role == "business"
    val displayName: String get() = fullName ?: email ?: "User"
}

/**
 * Job Model
 *
 * Represents a job posting in the domain layer
 */
data class Job(
    val id: String,
    val businessId: String,
    val title: String,
    val description: String? = null,
    val wage: Double? = null,
    val wageType: String? = null, // "hourly", "daily", "shift"
    val location: String? = null,
    val category: String? = null,
    val status: String = "open", // "open", "filled", "cancelled"
    val createdAt: String? = null,
    val updatedAt: String? = null,

    // Time fields
    val startTime: String? = null,
    val endTime: String? = null,
    val shiftDate: String? = null,

    // Flags
    val isUrgent: Boolean = false,
    val isCompliant: Boolean? = null, // 21 Days Rule compliance

    // Worker info
    val workerCount: Int? = null, // Number of workers needed

    // Business info (denormalized for queries)
    val businessName: String? = null,
    val businessLatitude: Double? = null,
    val businessLongitude: Double? = null
) {
    val formattedWage: String
        get() = when (wageType) {
            "hourly" -> "Rp ${wage?.toInt()}/jam"
            "daily" -> "Rp ${wage?.toInt()}/hari"
            "shift" -> "Rp ${wage?.toInt()}/shift"
            else -> "Rp ${wage?.toInt()}"
        }
}

/**
 * Job Application Model
 *
 * Represents a worker's application to a job
 */
data class JobApplication(
    val id: String,
    val jobId: String,
    val workerId: String,
    val status: String, // "pending", "accepted", "rejected", "completed", "cancelled"
    val message: String? = null,
    val appliedAt: String? = null,
    val acceptedAt: String? = null,
    val startedAt: String? = null,
    val completedAt: String? = null,
    val workerRating: Int? = null,
    val businessRating: Int? = null,
    val workerReview: String? = null,
    val businessReview: String? = null,
    val cancellationReason: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,

    // Nested job data (may be null)
    val job: Job? = null
) {
    val isActive: Boolean get() = status in listOf("pending", "accepted")
    val isCompleted: Boolean get() = status == "completed"
    val isCancelled: Boolean get() = status == "cancelled"
}

/**
 * Job Match Score
 *
 * Represents the compatibility score between a job and a worker
 */
data class JobMatchScore(
    val jobId: String,
    val totalScore: Double,
    val breakdown: ScoreBreakdown
) {
    val isExcellentMatch: Boolean get() = totalScore >= 85
    val isGoodMatch: Boolean get() = totalScore >= 70
    val isAcceptableMatch: Boolean get() = totalScore >= 50

    val matchQuality: String
        get() = when {
            isExcellentMatch -> "Sangat Cocok"
            isGoodMatch -> "Cocok"
            isAcceptableMatch -> "Cukup Cocok"
            else -> "Kurang Cocok"
        }

    val matchPercentage: Int get() = totalScore.toInt()
}

/**
 * Score Breakdown
 *
 * Detailed breakdown of how the match score was calculated
 */
data class ScoreBreakdown(
    val distanceScore: Double = 0.0,
    val skillScore: Double = 0.0,
    val ratingScore: Double = 0.0,
    val reliabilityScore: Double = 0.0,
    val urgencyScore: Double = 0.0
) {
    val total: Double get() =
        distanceScore + skillScore + ratingScore + reliabilityScore + urgencyScore
}

/**
 * Job with Score
 *
 * Wrapper containing a job and its match score
 */
data class JobWithScore(
    val job: Job,
    val score: JobMatchScore,
    val isCompliant: Boolean
)

/**
 * Worker Stats
 *
 * Aggregated statistics for a worker
 */
data class WorkerStats(
    val totalShiftsCompleted: Int = 0,
    val totalEarnings: Long = 0L,
    val walletBalance: Long = 0L,
    val frozenAmount: Long = 0L,
    val ratingAvg: Double = 0.0,
    val ratingCount: Int = 0,
    val reliabilityScore: Double = 100.0,
    val tier: String = "bronze" // "bronze", "silver", "gold", "platinum"
) {
    val availableBalance: Long get() = walletBalance - frozenAmount

    val formattedBalance: String get() = "Rp ${String.format("%,d", walletBalance)}"
    val formattedEarnings: String get() = "Rp ${String.format("%,d", totalEarnings)}"
}

/**
 * Wallet
 *
 * Worker's wallet information
 */
data class Wallet(
    val balance: Long = 0L,
    val frozenAmount: Long = 0L,
    val currency: String = "IDR"
) {
    val availableBalance: Long get() = balance - frozenAmount

    val formattedBalance: String
        get() = "Rp ${String.format("%,d", balance)}"

    val formattedAvailableBalance: String
        get() = "Rp ${String.format("%,d", availableBalance)}"
}
