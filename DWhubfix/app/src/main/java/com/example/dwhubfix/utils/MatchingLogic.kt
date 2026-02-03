package com.example.dwhubfix.utils

import android.content.Context
import com.example.dwhubfix.model.Job
import com.example.dwhubfix.model.WorkerProfile
import com.example.dwhubfix.model.JobMatchScore
import com.example.dwhubfix.model.ScoreBreakdown
import com.example.dwhubfix.model.WorkerAvailability
import com.example.dwhubfix.model.JobWithScore
import java.time.LocalDate
import kotlin.math.*
import org.osmdroid.util.GeoPoint

/**
 * SCORING SYSTEM
 * Calculate match score between Job and Worker
 * Based on matching-algorithm.md Section 6.2
 */
fun calculateJobScore(
    job: Job,
    worker: WorkerProfile,
    workerLocation: GeoPoint?
): JobMatchScore {
    
    // 1. Distance Score (30 bobot) - Max 30km
    val distance = if (workerLocation != null && job.businessInfo?.businessProfile?.latitude != null) {
        calculateDistance(
            workerLocation.latitude,
            workerLocation.longitude,
            job.businessInfo!!.businessProfile!!.latitude!!,
            job.businessInfo!!.businessProfile!!.longitude!!
        )
    } else {
        Double.MAX_VALUE // Punish if no location
    }
    
    val distanceScore = when {
        distance < 2.0 -> 30.0
        distance in 2.0..5.0 -> 25.0
        distance in 5.0..10.0 -> 15.0
        distance in 10.0..20.0 -> 5.0
        distance in 20.0..30.0 -> 2.0
        else -> 0.0
    }
    
    // 2. Skill Score (25 bobot)
    val skillScore = if (job.category in worker.workerSkills.map { it.skillName }) {
        25.0
    } else {
        0.0
    }
    
    // 3. Rating Score (20 bobot)
    val ratingScore = (worker.rating ?: 0.0) / 5.0 * 20.0
    
    // 4. Reliability Score (15 bobot)
    val reliabilityScore = (1.0 - (worker.noShowRate ?: 0.0)) * 15.0
    
    // 5. Urgency Score (10 bobot)
    val urgencyScore = if (job.isUrgent) {
        10.0
    } else {
        0.0
    }
    
    val totalScore = distanceScore + skillScore + ratingScore + reliabilityScore + urgencyScore
    
    return JobMatchScore(
        jobId = job.id,
        score = totalScore,
        breakdown = ScoreBreakdown(
            distanceScore = distanceScore,
            skillScore = skillScore,
            ratingScore = ratingScore,
            reliabilityScore = reliabilityScore,
            urgencyScore = urgencyScore
        )
    )
}

/**
 * COMPLIANCE GUARD
 * Rule 21 Days (PP 35/2021)
 * Based on matching-algorithm.md Section 3.2
 * 
 * Check if worker has worked for the same client > 20 days in the last 30 days
 */
fun isJobCompliant(
    job: Job,
    workerHistory: List<com.example.dwhubfix.model.JobApplication>
): Boolean {
    val clientId = job.businessId
    
    // Count days worked for this client in the last 30 days
    val thirtyDaysAgo = LocalDate.now().minusDays(30)
    
    val daysWorkedForClient = workerHistory.count { application ->
        application.businessId == clientId &&
        application.status in listOf("completed", "ongoing") &&
        application.startedAt?.let { startedAt ->
            try {
                // Parse date string (assume ISO format)
                val startDate = java.time.LocalDate.parse(startedAt.substring(0, 10))
                startDate.isAfter(thirtyDaysAgo)
            } catch (e: Exception) {
                false
            }
        } ?: false
    }
    
    // Compliant if <= 20 days
    return daysWorkedForClient <= 20
}

/**
 * PRIORITY ALGORITHM
 * Sort jobs based on score and other factors
 * Based on matching-algorithm.md Section 2.1
 */
fun prioritizeJobs(
    jobs: List<Job>,
    worker: WorkerProfile,
    workerLocation: GeoPoint?
): List<JobWithScore> {
    return jobs
        .map { job ->
            val score = calculateJobScore(job, worker, workerLocation)
            JobWithScore(job, score)
        }
        .sortedByDescending { it.score.score }
}
