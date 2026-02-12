package com.example.dwhubfix.domain.usecase

import com.example.dwhubfix.domain.model.Job
import com.example.dwhubfix.domain.repository.JobRepository
import javax.inject.Inject

/**
 * Get Business Jobs Use Case
 *
 * Retrieves all jobs posted by the current business user.
 *
 * @property jobRepository Repository for job operations
 */
class GetBusinessJobsUseCase @Inject constructor(
    private val jobRepository: JobRepository
) {

    /**
     * Execute use case
     *
     * @return List of jobs posted by the business
     */
    suspend operator fun invoke(): Result<List<Job>> {
        return try {
            // For now, use getAvailableJobs as a placeholder
            // In the future, this would filter by business_id
            jobRepository.getAvailableJobs()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
