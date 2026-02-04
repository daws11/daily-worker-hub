package com.example.dwhubfix.data

import android.content.Context
import org.osmdroid.util.GeoPoint
import com.example.dwhubfix.model.Job
import com.example.dwhubfix.model.JobApplication
import com.example.dwhubfix.model.JobMatchScore
import com.example.dwhubfix.model.ScoreBreakdown
import com.example.dwhubfix.model.WorkerProfile
import com.example.dwhubfix.model.WorkerAvailability
import com.example.dwhubfix.model.JobWithScore
import com.example.dwhubfix.utils.calculateDistance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

object MatchingRepository {

    suspend fun getJobsForWorker(
        context: Context,
        workerLocation: GeoPoint? = null
    ): Result<List<JobWithScore>> = withContext(Dispatchers.IO) {
        try {
            val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")

            // 1. Fetch Worker Profile & History
            val profile = SupabaseRepository.getProfile(context).getOrNull() ?: throw Exception("Profile not found")
            val workerHistory = getWorkerHistory(context).getOrNull() ?: emptyList()

            // 2. Fetch All Open Jobs
            val allJobs = SupabaseRepository.getAvailableJobs(context).getOrNull() ?: emptyList()

            // Jobs are already Job objects now
            val jobList = allJobs

            // 3. Compliance Filter (21 Days Rule)
            val compliantJobs = jobList.filter { job ->
                isJobCompliant(job, workerHistory)
            }

            // 4. Calculate Score & Prioritize
            val prioritizedJobs = prioritizeJobs(
                compliantJobs,
                workerLocation
            )

            // Return jobs sorted by score (highest first)
            Result.success(prioritizedJobs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getWorkerHistory(context: Context): Result<List<JobApplication>> = withContext(Dispatchers.IO) {
        try {
            val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")

            // Fetch applications from last 30 days
            val thirtyDaysAgo = LocalDate.now().minusDays(30).toString()

            val applications = SupabaseRepository.getWorkerJobs(context).getOrNull() ?: emptyList()

            // Convert Map to JobApplication objects - getWorkerJobs returns List<Map<String, Any?>>
            val applicationList = applications.mapNotNull { appMap ->
                try {
                    JobApplication(
                        id = appMap["id"] as? String ?: "",
                        jobId = appMap["job_id"] as? String ?: "",
                        workerId = appMap["worker_id"] as? String ?: userId,
                        status = appMap["status"] as? String ?: "pending",
                        message = appMap["message"] as? String,
                        appliedAt = appMap["applied_at"] as? String,
                        acceptedAt = appMap["accepted_at"] as? String,
                        startedAt = appMap["started_at"] as? String,
                        completedAt = appMap["completed_at"] as? String,
                        workerRating = appMap["worker_rating"] as? Int,
                        businessRating = appMap["business_rating"] as? Int,
                        workerReview = appMap["worker_review"] as? String,
                        businessReview = appMap["business_review"] as? String,
                        cancellationReason = appMap["cancellation_reason"] as? String,
                        createdAt = appMap["created_at"] as? String,
                        updatedAt = appMap["updated_at"] as? String,
                        job = null // Could be populated if joining with jobs table
                    )
                } catch (e: Exception) {
                    null
                }
            }

            Result.success(applicationList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun isJobCompliant(
        job: Job,
        workerHistory: List<JobApplication>
    ): Boolean {
        val clientId = job.businessId

        // Count days worked for this client in last 30 days
        val daysWorkedForClient = workerHistory.count { application ->
            // Note: JobApplication doesn't have businessId field, we need to get it from the nested job
            application.job?.businessId == clientId &&
            application.status in listOf("completed", "ongoing") &&
            application.startedAt?.let { startedAt ->
                try {
                    val startDate = LocalDate.parse(startedAt.substring(0, 10))
                    startDate.isAfter(LocalDate.now().minusDays(30))
                } catch (e: Exception) {
                    false
                }
            } ?: false
        }

        // Compliant if <= 20 days
        return daysWorkedForClient <= 20
    }

    private fun prioritizeJobs(
        jobs: List<Job>,
        workerLocation: GeoPoint?
    ): List<JobWithScore> {
        return jobs.map { job ->
            val score = calculateJobScore(job, workerLocation)
            JobWithScore(job, score)
        }.sortedByDescending { it.score.score }
    }

    private fun calculateJobScore(
        job: Job,
        workerLocation: GeoPoint?
    ): JobMatchScore {
        // 1. Distance Score (0-30)
        val distanceScore = if (workerLocation != null) {
            // For now, give default score since we don't have business coordinates
            15.0
        } else {
            0.0
        }

        // 2. Skill Score (0-25)
        val jobCategory = job.category ?: ""
        val skillScore = if (jobCategory.isNotEmpty()) {
            25.0
        } else {
            0.0
        }

        // 3. Rating Score (0-20)
        val ratingScore = 20.0 // Default max score

        // 4. Reliability Score (0-15)
        val reliabilityScore = 15.0 // Default max score

        // 5. Urgency Score (0-10)
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
}
