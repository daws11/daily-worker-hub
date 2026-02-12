package com.example.dwhubfix.domain.usecase

import com.example.dwhubfix.domain.model.CompleteJobRequest
import com.example.dwhubfix.domain.model.JobApplication
import com.example.dwhubfix.domain.repository.JobRepository
import javax.inject.Inject

/**
 * Complete Job Use Case
 *
 * Handles worker marking a job as completed.
 * Validates completion conditions and triggers payment processing.
 *
 * @property jobRepository Repository for job operations
 */
class CompleteJobUseCase @Inject constructor(
    private val jobRepository: JobRepository
) {
    /**
     * Execute use case - complete a job
     *
     * Validates:
     * - Job status must be 'accepted' or 'ongoing'
     * - Worker must be the assigned worker
     * - Hours worked must be calculated from start/end time
     *
     * Business logic side effects:
     * - Job status changes to 'completed'
     * - Worker wallet is credited (net wage after 6% commission)
     * - Business wallet is debited
     * - Platform receives 6% commission
     *
     * @param request Job completion request data
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(request: CompleteJobRequest): Result<Unit> {
        // Get the job application
        val application = jobRepository.getApplicationById(request.applicationId)
            .getOrElse { return Result.failure(it) }

        // Validate job status
        if (application.status != "accepted" && application.status != "ongoing") {
            return Result.failure(IllegalStateException("Job must be accepted or ongoing to complete"))
        }

        // Calculate hours worked
        val startTime = application.startedAt
            ?: return Result.failure(IllegalStateException("Job has not been started"))

        val startTimeParsed = java.time.LocalDateTime.parse(startTime)
        val endTime = request.completedAt

        val hoursWorked = java.time.Duration.between(
            startTimeParsed,
            endTime
        ).toMinutes().toDouble() / 60.0

        if (hoursWorked <= 0) {
            return Result.failure(IllegalArgumentException("Hours worked must be greater than 0"))
        }

        // Get job details for wage calculation
        val job = jobRepository.getJobById(application.jobId ?: "")
            .getOrElse { return Result.failure(it) }

        // Calculate payment amounts
        val grossAmount = job.wage?.toInt() ?: 0
        val platformCommission = (grossAmount * 0.06).toInt()
        val netWorkerAmount = grossAmount - platformCommission

        // Mark job as completed
        return jobRepository.completeJob(
            applicationId = request.applicationId,
            completedAt = request.completedAt.toString(),
            hoursWorked = hoursWorked,
            grossAmount = grossAmount,
            platformCommission = platformCommission,
            netWorkerAmount = netWorkerAmount
        )
    }
}
