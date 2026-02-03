package com.example.dwhubfix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.osmdroid.util.GeoPoint

// Existing models...
// Assuming Job and WorkerProfile exist in other files

@Serializable
data class JobMatchScore(
    val jobId: String,
    val score: Double, // 0.0 - 100.0
    val breakdown: ScoreBreakdown
)

@Serializable
data class ScoreBreakdown(
    val distanceScore: Double,
    val skillScore: Double,
    val ratingScore: Double,
    val reliabilityScore: Double,
    val urgencyScore: Double
)

@Serializable
data class WorkerAvailability(
    val isAvailable: Boolean,
    val preferredCategories: List<String>,
    val maxShiftsPerMonth: Int,
    val rating: Double,
    val noShowRate: Double
)

@Serializable
data class JobWithScore(
    val job: Job,
    val score: JobMatchScore
)
