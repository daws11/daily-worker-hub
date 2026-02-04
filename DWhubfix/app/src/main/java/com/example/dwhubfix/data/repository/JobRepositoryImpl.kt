package com.example.dwhubfix.data.repository

import android.content.Context
import com.example.dwhubfix.core.network.SupabaseClient
import com.example.dwhubfix.data.SessionManager
import com.example.dwhubfix.domain.model.Job
import com.example.dwhubfix.domain.model.JobApplication
import com.example.dwhubfix.domain.model.UserProfile
import com.example.dwhubfix.domain.repository.JobRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Job Repository Implementation
 *
 * Handles job-related operations using Supabase Postgrest.
 * Uses injected dependencies for better testability.
 */
@Singleton
class JobRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    @ApplicationContext private val context: Context
) : JobRepository {

    private val client get() = supabaseClient.client

    override suspend fun getWorkerProfile(): Result<UserProfile> =
        withContext(Dispatchers.IO) {
            try {
                val token = getAccessTokenOrThrow()

                val response = client.from("profiles")
                    .select()
                    .decodeSingle<Map<String, Any?>>()

                val profile = UserProfile(
                    id = response["id"] as? String ?: "",
                    fullName = response["full_name"] as? String,
                    email = response["email"] as? String,
                    phoneNumber = response["phone_number"] as? String,
                    avatarUrl = response["avatar_url"] as? String,
                    role = response["role"] as? String,
                    createdAt = response["created_at"] as? String,
                    updatedAt = response["updated_at"] as? String
                )

                Result.success(profile)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getWorkerHistory(): Result<List<JobApplication>> =
        withContext(Dispatchers.IO) {
            try {
                val userId = getUserIdOrThrow()

                val response = client.from("job_applications")
                    .select()
                    .decodeList<Map<String, Any?>>()

                val applications = response.map { appMap ->
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
                        job = null // Job details would need to be fetched separately
                    )
                }

                Result.success(applications)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getAvailableJobs(): Result<List<Job>> =
        withContext(Dispatchers.IO) {
            try {
                getAccessTokenOrThrow()

                val response = client.from("jobs")
                    .select()
                    .decodeList<Map<String, Any?>>()

                Result.success(response.toJobList())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun acceptJob(jobId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                getAccessTokenOrThrow()
                val userId = getUserIdOrThrow()

                val applicationData = mapOf(
                    "job_id" to jobId,
                    "worker_id" to userId,
                    "status" to "accepted"
                )

                client.from("job_applications").insert(applicationData)

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getJobById(jobId: String): Result<Job> =
        withContext(Dispatchers.IO) {
            try {
                getAccessTokenOrThrow()

                val response = client.from("jobs").select() {
                    filter { eq("id", jobId) }
                }.decodeSingle<Map<String, Any?>>()

                Result.success(response.toJob())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun deleteJob(jobId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                getAccessTokenOrThrow()

                client.from("jobs").delete {
                    filter { eq("id", jobId) }
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // Helper functions

    private fun getAccessTokenOrThrow(): String {
        return SessionManager.getAccessToken(context)
            ?: throw IllegalStateException("Not authenticated")
    }

    private fun getUserIdOrThrow(): String {
        return SessionManager.getUserId(context)
            ?: throw IllegalStateException("No user ID found")
    }

    // Extension functions for Map to Job conversion
    private fun Map<String, Any?>.toJob(): Job {
        return Job(
            id = this["id"] as? String ?: "",
            businessId = this["business_id"] as? String ?: "",
            title = this["title"] as? String ?: "",
            description = this["description"] as? String,
            wage = this["wage"] as? Double,
            wageType = this["wage_type"] as? String,
            location = this["location"] as? String,
            category = this["category"] as? String,
            status = this["status"] as? String ?: "open",
            createdAt = this["created_at"] as? String,
            updatedAt = this["updated_at"] as? String,
            startTime = this["start_time"] as? String,
            endTime = this["end_time"] as? String,
            shiftDate = this["shift_date"] as? String,
            isUrgent = this["is_urgent"] as? Boolean ?: false,
            isCompliant = this["is_compliant"] as? Boolean,
            workerCount = this["worker_count"] as? Int,
            businessName = null, // Would need to join with profiles
            businessLatitude = null,
            businessLongitude = null
        )
    }

    private fun List<Map<String, Any?>>.toJobList(): List<Job> {
        return map { it.toJob() }
    }
}
