package com.example.dwhubfix.domain.repository

import com.example.dwhubfix.domain.model.Job
import com.example.dwhubfix.domain.model.JobApplication
import com.example.dwhubfix.domain.model.UserProfile
import com.example.dwhubfix.domain.model.WorkerStats
import org.osmdroid.util.GeoPoint

/**
 * Job Repository Interface
 *
 * Defines the contract for job-related operations including:
 * - Fetching available jobs
 * - Applying for jobs
 * - Managing worker profile and history
 */
interface JobRepository {

    /**
     * Get worker's profile
     *
     * @return Worker's profile data
     */
    suspend fun getWorkerProfile(): Result<UserProfile>

    /**
     * Get worker's job history
     *
     * @return List of past job applications
     */
    suspend fun getWorkerHistory(): Result<List<JobApplication>>

    /**
     * Get worker's statistics
     *
     * @return Worker's stats including earnings, shifts, ratings
     */
    suspend fun getWorkerStats(): Result<WorkerStats?>

    /**
     * Get all available jobs
     *
     * @return List of available jobs
     */
    suspend fun getAvailableJobs(): Result<List<Job>>

    /**
     * Accept a job
     *
     * @param jobId The job ID to accept
     * @return Result indicating success or failure
     */
    suspend fun acceptJob(jobId: String): Result<Unit>

    /**
     * Get job by ID
     *
     * @param jobId The job ID
     * @return The job details
     */
    suspend fun getJobById(jobId: String): Result<Job>

    /**
     * Get application by ID
     *
     * @param applicationId The application ID
     * @return The job application details
     */
    suspend fun getApplicationById(applicationId: String): Result<JobApplication>

    /**
     * Delete a job
     *
     * @param jobId The job ID to delete
     * @return Result indicating success or failure
     */
    suspend fun deleteJob(jobId: String): Result<Unit>

    // =====================
    // Create Job Operations
    // =====================

    /**
     * Create a new job
     *
     * @param request Job creation request data
     * @return Result containing created job on success
     */
    suspend fun createJob(request: com.example.dwhubfix.domain.model.CreateJobRequest): Result<Job>

    /**
     * Apply for a job
     *
     * @param request Job application request data
     * @return Result containing application on success
     */
    suspend fun applyForJob(request: com.example.dwhubfix.domain.model.ApplyForJobRequest): Result<JobApplication>

    /**
     * Complete a job
     *
     * @param applicationId The application ID
     * @param completedAt When the job was completed
     * @param hoursWorked Hours worked
     * @param grossAmount Original wage amount
     * @param platformCommission Platform fee (6% of gross)
     * @param netWorkerAmount Net amount paid to worker
     * @return Result indicating success or failure
     */
    suspend fun completeJob(
        applicationId: String,
        completedAt: String,
        hoursWorked: Double,
        grossAmount: Int,
        platformCommission: Int,
        netWorkerAmount: Int
    ): Result<Unit>

    /**
     * Get job with full details
     *
     * @param jobId The job ID
     * @return Job with business and application details
     */
    suspend fun getJobDetails(jobId: String): Result<com.example.dwhubfix.domain.model.JobWithDetails>
}
