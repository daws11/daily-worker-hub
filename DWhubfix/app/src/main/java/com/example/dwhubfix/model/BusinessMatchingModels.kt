package com.example.dwhubfix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual

/**
 * BUSINESS WORKER MATCHING MODELS
 *
 * Based on matching-algorithm.md (Business Side):
 * - Distance Score (25%)
 * - Skill Score (30%) - Exact match with required skills
 * - Rating Score (20%) - Worker's overall rating
 * - Reliability Score (15%) - Worker's no-show rate
 * - Availability Score (10%) - Worker's availability status
 *
 * Based on business-model.md (Rate Bali):
 * - Business can see workers with calculated scores
 * - Workers hired based on scores (highest first)
 * - Compliance check (21 Days Rule) for workers
 */

@Serializable
data class WorkerCandidate(
    val workerId: String,
    val workerName: String,
    val avatarUrl: String?,
    val skills: List<String>,
    val rating: Double = 0.0,
    val matchScore: Double = 0.0, // From business matching algorithm (0.0 - 100.0)
    val breakdown: WorkerScoreBreakdown,
    val distance: String = "0.0 km",
    val reliabilityScore: Double = 0.0, // 0.0 - 15.0
    val noShowRate: Double = 0.0, // 0.0 - 1.0
    val isCompliant: Boolean = true, // Compliance for worker (21 days rule)
    val totalShiftsCompleted: Int = 0,
    val hourlyRate: Double? = null,
    val hourlyRateString: String? = null, // Formatted display string (e.g., "Rp 50rb/jam")
    val availabilityStatus: String = "Tersedia", // Tersedia, Sedang Bekerja, Sibuk
    val lastActiveDate: String? = null,
    val distanceValue: Double = 0.0 // Raw distance value for sorting
)

@Serializable
data class WorkerScoreBreakdown(
    val distanceScore: Double,    // 0.0 - 25.0
    val skillScore: Double,       // 0.0 - 30.0
    val ratingScore: Double,      // 0.0 - 20.0
    val reliabilityScore: Double, // 0.0 - 15.0
    val availabilityScore: Double // 0.0 - 10.0
)

@Serializable
data class BusinessMatchingParams(
    val jobId: String,
    val businessId: String,
    val businessLatitude: Double,
    val businessLongitude: Double,
    val workerSkillsRequired: List<String>,
    val workerMinRating: Double = 0.0,
    val workerMinExperience: String? = null,
    val maxDistance: Double = 20.0 // km
)

/**
 * Business Matching Result
 * Contains list of worker candidates sorted by score
 */
@Serializable
data class BusinessMatchingResult(
    val jobId: String,
    val candidates: List<WorkerCandidate>,
    val totalCandidates: Int,
    val matchedCandidates: Int, // Candidates with score >= 70
    val averageScore: Double,
    val recommendedCandidate: WorkerCandidate? // Top candidate
)
