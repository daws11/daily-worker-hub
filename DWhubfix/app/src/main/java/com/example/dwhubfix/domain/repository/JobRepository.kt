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
     * Delete a job
     *
     * @param jobId The job ID to delete
     * @return Result indicating success or failure
     */
    suspend fun deleteJob(jobId: String): Result<Unit>
}
