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
        authenticatedCall(context) {
            val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")
            
            // 1. Fetch Worker Profile & History
            val profile = SupabaseRepository.getProfile(context).getOrNull() ?: throw Exception("Profile not found")
            val workerHistory = getWorkerHistory(context).getOrNull() ?: emptyList()
            
            // 2. Fetch All Open Jobs
            val columns = Columns.list(
                "*",
                "worker_profiles(*)",
                "worker_skills(*)",
                "business_profiles(*)"
            )
            val allJobs = client.from("jobs").select(columns = columns) {
                filter { 
                    eq("status", "open") 
                }
            }.decodeList<Job>()
            
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
            
            // Return jobs sorted by score (highest first)
            Result.success(prioritizedJobs)
        }
    }

private fun getWorkerHistory(context: Context): Result<List<JobApplication>> {
    val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")
    
    val columns = Columns.list(
        "id",
        "job_id",
        "business_id",
        "status",
        "started_at",
        "jobs(*)" // Join with jobs table
    )
    
    // Fetch applications from last 30 days
    val thirtyDaysAgo = LocalDate.now().minusDays(30).toString()
    
    return authenticatedCall {
        client.from("job_applications").select(columns = columns) {
            filter { 
                eq("worker_id", userId)
                    gte("created_at", thirtyDaysAgo)
            }
        }.decodeList<JobApplication>()
    }
}

private fun isJobCompliant(
    job: Job,
    workerHistory: List<JobApplication>
): Boolean {
    val clientId = job.businessId ?: return false
    
    // Count days worked for this client in last 30 days
    val daysWorkedForClient = workerHistory.count { application ->
        application.businessId == clientId &&
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
    worker: WorkerProfile?,
    workerLocation: GeoPoint?
): List<JobWithScore> {
    return jobs.map { job ->
        val score = calculateJobScore(job, worker, workerLocation)
        JobWithScore(job, score)
    }.sortedByDescending { it.score.score }
}

private fun calculateJobScore(
    job: Job,
    worker: WorkerProfile?,
    workerLocation: GeoPoint?
): JobMatchScore {
    val workerSkills = worker?.workerSkills?.map { it.skillName }?.toSet() ?: emptySet()
    val workerRating = worker?.workerProfile?.rating ?: 0.0
    val workerNoShowRate = 0.1 // Default 10% no-show rate
    
    val jobLat = job.businessInfo?.businessProfile?.latitude
    val jobLon = job.businessInfo?.businessProfile?.longitude
    
    // 1. Distance Score (0-30)
    val distanceScore = if (workerLocation != null && jobLat != null && jobLon != null) {
        val distance = calculateDistance(
            workerLocation.latitude,
            workerLocation.longitude,
            jobLat,
            jobLon
        )
        when {
            distance < 2.0 -> 30.0
            distance in 2.0..5.0 -> 25.0
            distance in 5.0..10.0 -> 15.0
            distance in 10.0..20.0 -> 5.0
            distance in 20.0..30.0 -> 2.0
            else -> 0.0
        }
    } else {
        0.0
    }
    
    // 2. Skill Score (0-25)
    val jobCategory = job.category ?: ""
    val skillScore = if (jobCategory in workerSkills) {
        25.0
    } else {
        0.0
    }
    
    // 3. Rating Score (0-20)
    val ratingScore = (workerRating / 5.0) * 20.0
    
    // 4. Reliability Score (0-15)
    val reliabilityScore = (1.0 - workerNoShowRate) * 15.0
    
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
