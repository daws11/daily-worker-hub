package com.example.dwhubfix.domain.usecase

import com.example.dwhubfix.domain.repository.JobRepository
import javax.inject.Inject

/**
 * Accept Job Use Case
 *
 * Handles worker accepting a job posting.
 *
 * @property jobRepository Repository for job operations
 */
class AcceptJobUseCase @Inject constructor(
    private val jobRepository: JobRepository
) {
    /**
     * Accept a job
     *
     * @param jobId The job ID to accept
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(jobId: String): Result<Unit> {
        return jobRepository.acceptJob(jobId)
    }
}
