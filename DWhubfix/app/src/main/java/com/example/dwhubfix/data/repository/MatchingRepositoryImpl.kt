package com.example.dwhubfix.data.repository

import android.content.Context
import com.example.dwhubfix.core.network.SupabaseClient
import com.example.dwhubfix.data.SessionManager
import com.example.dwhubfix.domain.model.Job
import com.example.dwhubfix.domain.model.JobApplication
import com.example.dwhubfix.domain.model.JobMatchScore
import com.example.dwhubfix.domain.model.JobWithScore
import com.example.dwhubfix.domain.model.ScoreBreakdown
import com.example.dwhubfix.domain.model.UserProfile
import com.example.dwhubfix.domain.repository.JobRepository
import com.example.dwhubfix.utils.calculateDistance
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Matching Repository
 *
 * Handles job-worker matching logic including:
 * - Smart matching based on multiple criteria
 * - 21 Days Rule compliance checking
 * - Job prioritization by match score
 */
@Singleton
class MatchingRepository @Inject constructor(
    private val jobRepository: JobRepository,
    private val supabaseClient: SupabaseClient,
    @ApplicationContext private val context: Context
) {
    private val client get() = supabaseClient.client

    /**
     * Get jobs for worker with smart matching
     *
     * @param workerLocation Optional worker location for distance scoring
     * @return Result containing prioritized list of jobs with scores
     */
    suspend fun getJobsForWorker(
        workerLocation: GeoPoint? = null
    ): Result<List<JobWithScore>> = withContext(Dispatchers.IO) {
        try {
            val userId = getUserIdOrThrow()

            // 1. Fetch Worker Profile & History
            val profile = jobRepository.getWorkerProfile()
                .getOrElse { throw it }

            val workerHistory = jobRepository.getWorkerHistory()
                .getOrElse { emptyList() }

            // 2. Fetch All Open Jobs
            val allJobs = jobRepository.getAvailableJobs()
                .getOrElse { throw it }

            // 3. Compliance Filter (21 Days Rule)
            val compliantJobs = allJobs.filter { job ->
                isJobCompliant(job, workerHistory)
            }

            // 4. Calculate Score & Prioritize
            val prioritizedJobs = prioritizeJobs(
                compliantJobs,
                profile,
                workerLocation
            )

            Result.success(prioritizedJobs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Check if job is compliant with 21 Days Rule (PP 35/2021)
     *
     * @param job The job to check
     * @param workerHistory Worker's job application history
     * @return true if worker has worked <= 20 days for this client in last 30 days
     */
    private fun isJobCompliant(
        job: Job,
        workerHistory: List<JobApplication>
    ): Boolean {
        val clientId = job.businessId
        val thirtyDaysAgo = LocalDate.now().minusDays(30)

        // Count days worked for this client in last 30 days
        val daysWorkedForClient = workerHistory.count { application ->
            application.job?.businessId == clientId &&
            application.status in listOf("completed", "in_progress") &&
            application.startedAt?.let { startedAt ->
                try {
                    val startDate = LocalDate.parse(startedAt.substring(0, 10))
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
     * Prioritize jobs by match score
     *
     * @param jobs List of compliant jobs
     * @param worker Worker profile
     * @param workerLocation Optional worker location
     * @return Jobs sorted by score (highest first)
     */
    private fun prioritizeJobs(
        jobs: List<Job>,
        worker: UserProfile,
        workerLocation: GeoPoint?
    ): List<JobWithScore> {
        return jobs.map { job ->
            val score = calculateJobScore(job, worker, workerLocation)
            val isCompliant = true // Already filtered
            JobWithScore(job, score, isCompliant)
        }.sortedByDescending { it.score.totalScore }
    }

    /**
     * Calculate job match score
     *
     * Scores are calculated based on:
     * - Distance: 30 points max
     * - Skill: 25 points max
     * - Rating: 20 points max
     * - Reliability: 15 points max
     * - Urgency: 10 points max
     *
     * @param job The job to score
     * @param worker Worker profile
     * @param workerLocation Optional worker location
     * @return JobMatchScore with breakdown
     */
    private fun calculateJobScore(
        job: Job,
        worker: UserProfile,
        workerLocation: GeoPoint?
    ): JobMatchScore {
        // 1. Distance Score (0-30)
        val distanceScore = if (workerLocation != null && job.businessLatitude != null && job.businessLongitude != null) {
            val distance = calculateDistance(
                workerLocation.latitude,
                workerLocation.longitude,
                job.businessLatitude,
                job.businessLongitude
            )
            calculateDistanceScore(distance)
        } else {
            0.0
        }

        // 2. Skill Score (0-25)
        val skillScore = 25.0 // Assuming skill match for now (can be improved later)

        // 3. Rating Score (0-20)
        val ratingScore = 20.0 // Default max score (can use worker rating later)

        // 4. Reliability Score (0-15)
        val reliabilityScore = 15.0 // Default max score

        // 5. Urgency Score (0-10)
        val urgencyScore = if (job.isUrgent) 10.0 else 0.0

        val totalScore = distanceScore + skillScore + ratingScore + reliabilityScore + urgencyScore

        return JobMatchScore(
            jobId = job.id,
            totalScore = totalScore,
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
     * Calculate distance score based on distance in km
     */
    private fun calculateDistanceScore(distanceKm: Double): Double {
        return when {
            distanceKm < 2.0 -> 30.0
            distanceKm < 5.0 -> 25.0
            distanceKm < 10.0 -> 15.0
            distanceKm < 20.0 -> 5.0
            distanceKm < 30.0 -> 2.0
            else -> 0.0
        }
    }

    private fun getUserIdOrThrow(): String {
        return SessionManager.getUserId(context)
            ?: throw IllegalStateException("No user ID found")
    }
}
