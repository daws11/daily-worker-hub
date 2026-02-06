package com.example.dwhubfix.domain.usecase

import com.example.dwhubfix.domain.model.Job
import com.example.dwhubfix.domain.model.JobWithDetails
import com.example.dwhubfix.domain.repository.JobRepository
import javax.inject.Inject

/**
 * Get Job Details Use Case
 *
 * Retrieves detailed job information including:
 * - Job details (title, wage, schedule, etc.)
 * - Business information (name, location, rating)
 * - Application status for the current worker
 *
 * @property jobRepository Repository for job operations
 */
class GetJobDetailsUseCase @Inject constructor(
    private val jobRepository: JobRepository
) {
    /**
     * Execute use case - get job details
     *
     * @param jobId The job ID to fetch
     * @return Result containing job details on success, error on failure
     */
    suspend operator fun invoke(jobId: String): Result<JobWithDetails> {
        return jobRepository.getJobDetails(jobId)
    }

    /**
     * Check if job is available for application
     *
     * @param job The job to check
     * @return true if status is 'open' or 'filled'
     */
    fun isJobAvailable(job: Job): Boolean {
        return job.status in listOf("open", "filled")
    }

    /**
     * Check if job is accepting applications
     *
     * @param job The job to check
     * @return true if status is 'open'
     */
    fun isAcceptingApplications(job: Job): Boolean {
        return job.status == "open"
    }
}
