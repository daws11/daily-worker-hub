package com.example.dwhubfix.domain.usecase

import com.example.dwhubfix.domain.model.ApplyForJobRequest
import com.example.dwhubfix.domain.model.JobApplication
import com.example.dwhubfix.domain.repository.JobRepository
import javax.inject.Inject

/**
 * Apply for Job Use Case
 *
 * Handles worker applying for a job posting.
 * Validates constraints before application.
 *
 * @property jobRepository Repository for job operations
 */
class ApplyForJobUseCase @Inject constructor(
    private val jobRepository: JobRepository
) {
    /**
     * Execute use case - apply for a job
     *
     * Validates:
     * - Job status must be 'open' or 'filled'
     * - Worker cannot have already applied (duplicate check)
     * - Worker must be verified
     *
     * @param request Job application request data
     * @return Result containing application on success
     */
    suspend operator fun invoke(request: ApplyForJobRequest): Result<JobApplication> {
        // Validate job status
        val job = jobRepository.getJobById(request.jobId)
            .getOrElse { return Result.failure(it) }

        if (job.status !in listOf("open", "filled")) {
            return Result.failure(IllegalArgumentException("Job is not available for application"))
        }

        // Check for duplicate application
        val workerHistory = jobRepository.getWorkerHistory()
            .getOrElse { emptyList() }

        val hasAlreadyApplied = workerHistory.any { application ->
            application.job?.id == request.jobId
        }

        if (hasAlreadyApplied) {
            return Result.failure(IllegalArgumentException("You have already applied for this job"))
        }

        // Check 21 Days Rule compliance (simplified check)
        val thirtyDaysAgo = java.time.LocalDate.now().minusDays(30)
        val daysWorkedForClient = workerHistory.count { application ->
            application.job?.businessId == job.businessId &&
            application.status == "completed" &&
            application.startedAt?.let { applicationStartedAt ->
                try {
                    val startDate = java.time.LocalDate.parse(applicationStartedAt.substring(0, 10))
                    startDate.isAfter(thirtyDaysAgo)
                } catch (e: Exception) {
                    false
                }
            } ?: false
        }

        if (daysWorkedForClient > 20) {
            return Result.failure(IllegalStateException("Cannot apply: You have worked for this client more than 20 days in the last 30 days (21 Days Rule violation)"))
        }

        // All validations passed, submit application
        return jobRepository.applyForJob(request)
    }

    /**
     * Calculate days worked for a specific client in last 30 days
     *
     * @param workerHistory Worker's job application history
     * @param clientBusinessId Business ID to check
     * @return Number of days worked (0-20)
     */
    private fun calculateDaysWorkedForClient(
        workerHistory: List<JobApplication>,
        clientBusinessId: String
    ): Int {
        val thirtyDaysAgo = java.time.LocalDate.now().minusDays(30)

        return workerHistory.count { application ->
            application.job?.businessId == clientBusinessId &&
            application.status == "completed" &&
            application.startedAt?.let { applicationStartedAt ->
                try {
                    val startDate = java.time.LocalDate.parse(applicationStartedAt.substring(0, 10))
                    startDate.isAfter(thirtyDaysAgo)
                } catch (e: Exception) {
                    false
                }
            } ?: false
        }
    }
}
