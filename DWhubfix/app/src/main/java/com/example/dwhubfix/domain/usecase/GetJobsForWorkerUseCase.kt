package com.example.dwhubfix.domain.usecase

import com.example.dwhubfix.domain.model.Job
import com.example.dwhubfix.domain.model.JobApplication
import com.example.dwhubfix.domain.model.JobMatchScore
import com.example.dwhubfix.domain.model.JobWithScore
import com.example.dwhubfix.domain.model.UserProfile
import com.example.dwhubfix.domain.repository.JobRepository
import org.osmdroid.util.GeoPoint
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Get Jobs for Worker Use Case
 *
 * Retrieves and prioritizes available jobs for a worker based on:
 * - Smart matching algorithm (distance, skills, rating, reliability, urgency)
 * - 21 Days Rule compliance checking
 *
 * @property jobRepository Repository for job-related operations
 */
class GetJobsForWorkerUseCase @Inject constructor(
    private val jobRepository: JobRepository
) {
    /**
     * Execute the use case
     *
     * @param workerLocation Optional worker location for distance-based scoring
     * @return Result containing prioritized jobs with scores
     */
    suspend operator fun invoke(
        workerLocation: GeoPoint? = null
    ): Result<List<JobWithScore>> = withContext(Dispatchers.IO) {
        try {
            // 1. Get worker profile
            val profile = jobRepository.getWorkerProfile()
                .getOrElse { throw it }

            // 2. Get worker history for compliance checking
            val history = jobRepository.getWorkerHistory()
                .getOrElse { emptyList() }

            // 3. Get all available jobs
            val allJobs = jobRepository.getAvailableJobs()
                .getOrElse { throw it }

            // 4. Filter by compliance (21 Days Rule)
            val compliantJobs = allJobs.filter { job ->
                isJobCompliant(job, history)
            }

            // 5. Calculate scores and prioritize
            val scoredJobs = prioritizeJobs(
                jobs = compliantJobs,
                profile = profile,
                workerLocation = workerLocation
            )

            Result.success(scoredJobs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Check if job complies with 21 Days Rule (PP 35/2021)
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
        val thirtyDaysAgo = java.time.LocalDate.now().minusDays(30)

        val daysWorkedForClient = workerHistory.count { application ->
            application.job?.businessId == clientId &&
            application.status in listOf("completed", "in_progress") &&
            application.startedAt?.let { startedAt ->
                try {
                    val startDate = java.time.LocalDate.parse(startedAt.substring(0, 10))
                    startDate.isAfter(thirtyDaysAgo)
                } catch (e: Exception) {
                    false
                }
            } ?: false
        }

        return daysWorkedForClient <= 20
    }

    /**
     * Prioritize jobs by match score
     */
    private fun prioritizeJobs(
        jobs: List<Job>,
        profile: UserProfile,
        workerLocation: GeoPoint?
    ): List<JobWithScore> {
        return jobs.map { job ->
            val score = calculateJobScore(job, profile, workerLocation)
            JobWithScore(job, score, isCompliant = true)
        }.sortedByDescending { it.score.totalScore }
    }

    /**
     * Calculate job match score
     *
     * Scores based on:
     * - Distance: 30 points max
     * - Skills: 25 points max
     * - Rating: 20 points max
     * - Reliability: 15 points max
     * - Urgency: 10 points max
     */
    private fun calculateJobScore(
        job: Job,
        profile: UserProfile,
        workerLocation: GeoPoint?
    ): JobMatchScore {
        // 1. Distance Score (0-30)
        val distanceScore = if (workerLocation != null && job.businessLatitude != null && job.businessLongitude != null) {
            val distanceKm = calculateDistance(
                workerLocation.latitude,
                workerLocation.longitude,
                job.businessLatitude!!,
                job.businessLongitude!!
            )
            calculateDistanceScore(distanceKm)
        } else {
            0.0
        }

        // 2. Skill Score (0-25) - assuming match for now
        val skillScore = if (job.category != null) 25.0 else 0.0

        // 3. Rating Score (0-20) - default max score
        val ratingScore = 20.0

        // 4. Reliability Score (0-15) - default max score
        val reliabilityScore = 15.0

        // 5. Urgency Score (0-10)
        val urgencyScore = if (job.isUrgent) 10.0 else 0.0

        val totalScore = distanceScore + skillScore + ratingScore + reliabilityScore + urgencyScore

        return JobMatchScore(
            jobId = job.id,
            totalScore = totalScore,
            breakdown = com.example.dwhubfix.domain.model.ScoreBreakdown(
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

    private fun calculateDistance(
        lat1: Double,
        lng1: Double,
        lat2: Double,
        lng2: Double
    ): Double {
        val R = 6371 // Earth's radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return R * c
    }
}
