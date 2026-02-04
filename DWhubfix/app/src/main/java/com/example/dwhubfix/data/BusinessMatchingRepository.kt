package com.example.dwhubfix.data

import android.content.Context
import com.example.dwhubfix.BuildConfig
import com.example.dwhubfix.model.*
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * BUSINESS WORKER MATCHING LOGIC
 */
object BusinessMatchingRepository {

    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }

    /**
     * Get workers for job based on matching criteria
     * Returns BusinessMatchingResult with sorted candidates
     */
    suspend fun getWorkersForJob(
        context: Context,
        jobId: String
    ): Result<BusinessMatchingResult> {
        return withContext(Dispatchers.IO) {
            try {
                val token = SessionManager.getAccessToken(context)
                    ?: return@withContext Result.failure(Exception("Not authenticated"))

                // Get job details first
                val job = client.from("jobs").select() {
                    filter { eq("id", jobId) }
                }.decodeSingle<Map<String, Any?>>()

                // Get workers from profiles
                val workers = client.from("profiles").select() {
                    filter { eq("role", "worker") }
                }.decodeList<Map<String, Any?>>()

                // Convert to WorkerCandidate list with mock scores for now
                val candidates = workers.mapIndexed { index, worker ->
                    WorkerCandidate(
                        workerId = worker["id"] as? String ?: "",
                        workerName = worker["full_name"] as? String ?: "Unknown",
                        avatarUrl = worker["avatar_url"] as? String,
                        skills = (worker["skills"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                        rating = (worker["rating"] as? Number)?.toDouble() ?: 0.0,
                        matchScore = 100.0 - (index * 5.0), // Mock score for now
                        breakdown = WorkerScoreBreakdown(
                            distanceScore = 20.0,
                            skillScore = 25.0,
                            ratingScore = 15.0,
                            reliabilityScore = 12.0,
                            availabilityScore = 8.0
                        ),
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

                val result = BusinessMatchingResult(
                    jobId = jobId,
                    candidates = candidates,
                    totalCandidates = candidates.size,
                    matchedCandidates = candidates.count { it.matchScore >= 70 },
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
     */
    suspend fun getWorkerWithSkills(context: Context, workerId: String): Result<Map<String, Any?>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = SessionManager.getAccessToken(context)
                    ?: return@withContext Result.failure(Exception("Not authenticated"))

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
     */
    suspend fun getBusinessJobApplications(context: Context, businessId: String): Result<List<Map<String, Any?>>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = SessionManager.getAccessToken(context)
                    ?: return@withContext Result.failure(Exception("Not authenticated"))

                val applications = client.from("job_applications").select() {
                    // Note: job_applications has job_id, not business_id directly
                    // We need to join with jobs table
                }.decodeList<Map<String, Any?>>()

                Result.success(applications)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
