package com.example.dwhubfix.data.repository

import android.content.Context
import com.example.dwhubfix.core.network.SupabaseClient
import com.example.dwhubfix.data.SessionManager
import com.example.dwhubfix.domain.model.MatchingConstants
import com.example.dwhubfix.model.BusinessMatchingResult
import com.example.dwhubfix.model.WorkerCandidate
import com.example.dwhubfix.model.WorkerScoreBreakdown
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Business Matching Repository
 *
 * Handles business-side worker matching operations.
 * Provides matched workers sorted by compatibility score.
 */
@Singleton
class BusinessMatchingRepository @Inject constructor(
    private val supabaseClient: SupabaseClient,
    @ApplicationContext private val context: Context
) {
    private val client get() = supabaseClient.client

    /**
     * Get workers for job based on matching criteria
     *
     * @param jobId The job ID to find workers for
     * @return BusinessMatchingResult with sorted candidates
     */
    suspend fun getWorkersForJob(jobId: String): Result<BusinessMatchingResult> {
        return withContext(Dispatchers.IO) {
            try {
                requireAuthenticated()

                // Get job details first
                val job = client.from("jobs").select() {
                    filter { eq("id", jobId) }
                }.decodeSingle<Map<String, Any?>>()

                // Get workers from profiles
                val workers = client.from("profiles").select() {
                    filter { eq("role", "worker") }
                }.decodeList<Map<String, Any?>>()

                // Convert to WorkerCandidate list with match scores
                val candidates = workers.mapIndexed { index, worker ->
                    createWorkerCandidate(worker, index)
                }

                val result = BusinessMatchingResult(
                    jobId = jobId,
                    candidates = candidates,
                    totalCandidates = candidates.size,
                    matchedCandidates = candidates.count { it.matchScore >= MatchingConstants.GOOD_MATCH_THRESHOLD },
                    averageScore = candidates.map { it.matchScore }.average(),
                    recommendedCandidate = candidates.firstOrNull()
                )

                Result.success(result)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Get worker profile with skills
     *
     * @param workerId The worker ID
     * @return Worker profile with skills
     */
    suspend fun getWorkerWithSkills(workerId: String): Result<Map<String, Any?>> {
        return withContext(Dispatchers.IO) {
            try {
                requireAuthenticated()

                val profile = client.from("profiles").select() {
                    filter { eq("id", workerId) }
                }.decodeSingle<Map<String, Any?>>()

                // Get worker skills
                val skills = client.from("worker_skills").select() {
                    filter { eq("profile_id", workerId) }
                }.decodeList<Map<String, Any?>>()

                val result = profile.toMutableMap()
                result["worker_skills"] = skills

                Result.success(result)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Get business job applications
     *
     * @param businessId The business ID
     * @return List of job applications
     */
    suspend fun getBusinessJobApplications(businessId: String): Result<List<Map<String, Any?>>> {
        return withContext(Dispatchers.IO) {
            try {
                requireAuthenticated()

                // Note: This would need proper joins with jobs table
                val applications = client.from("job_applications").select()
                    .decodeList<Map<String, Any?>>()

                Result.success(applications)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // =====================================================
    // HELPER FUNCTIONS
    // =====================================================

    private fun createWorkerCandidate(
        worker: Map<String, Any?>,
        index: Int
    ): WorkerCandidate {
        return WorkerCandidate(
            workerId = worker["id"] as? String ?: "",
            workerName = worker["full_name"] as? String ?: "Unknown",
            avatarUrl = worker["avatar_url"] as? String,
            skills = (worker["skills"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            rating = (worker["rating"] as? Number)?.toDouble() ?: 0.0,
            matchScore = calculateMockMatchScore(index),
            breakdown = createScoreBreakdown(),
            distance = "${index * 2}.0 km",
            reliabilityScore = 12.0,
            noShowRate = 0.05,
            isCompliant = true,
            totalShiftsCompleted = (worker["total_shifts"] as? Number)?.toInt() ?: 0,
            hourlyRate = 50000.0,
            hourlyRateString = "Rp 50rb/jam",
            availabilityStatus = "Tersedia",
            lastActiveDate = worker["updated_at"] as? String,
            distanceValue = (index * 2.0)
        )
    }

    private fun calculateMockMatchScore(index: Int): Double {
        // TODO: Calculate actual match score based on:
        // - Skill matching
        // - Rating
        // - Distance
        // - Reliability
        // - Availability
        return 100.0 - (index * 5.0)
    }

    private fun createScoreBreakdown(): WorkerScoreBreakdown {
        return WorkerScoreBreakdown(
            distanceScore = 20.0,
            skillScore = 25.0,
            ratingScore = 15.0,
            reliabilityScore = 12.0,
            availabilityScore = 8.0
        )
    }

    private fun requireAuthenticated() {
        val token = SessionManager.getAccessToken(context)
        require(token != null) { "Not authenticated" }
    }
}
