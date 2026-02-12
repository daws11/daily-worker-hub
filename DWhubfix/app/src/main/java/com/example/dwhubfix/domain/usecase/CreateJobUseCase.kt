package com.example.dwhubfix.domain.usecase

import com.example.dwhubfix.domain.model.CreateJobRequest
import com.example.dwhubfix.domain.model.Job
import com.example.dwhubfix.domain.repository.JobRepository
import javax.inject.Inject

/**
 * Create Job Use Case
 *
 * Handles business creating a new job posting.
 * Validates input data before creation.
 *
 * @property jobRepository Repository for job operations
 */
class CreateJobUseCase @Inject constructor(
    private val jobRepository: JobRepository
) {
    /**
     * Execute use case - create a new job
     *
     * Validates:
     * - Wage must be positive
     * - Worker count must be between 1 and 10
     * - Start time must be before end time
     * - Shift date must be today or in the future
     *
     * @param request Job creation request data
     * @return Result containing created job on success
     */
    suspend operator fun invoke(request: CreateJobRequest): Result<Job> {
        // Validate wage
        if (request.wage <= 0) {
            return Result.failure(IllegalArgumentException("Wage must be greater than 0"))
        }

        // Validate worker count
        if (request.workerCount < 1 || request.workerCount > 10) {
            return Result.failure(IllegalArgumentException("Worker count must be between 1 and 10"))
        }

        // Validate time range
        if (request.endTime <= request.startTime) {
            return Result.failure(IllegalArgumentException("End time must be after start time"))
        }

        // Validate shift date
        val today = java.time.LocalDate.now()
        val shiftDate = request.shiftDate
        if (shiftDate.isBefore(today)) {
            return Result.failure(IllegalArgumentException("Shift date cannot be in the past"))
        }

        // All validations passed, create job
        return jobRepository.createJob(request)
    }

    /**
     * Validate wage type
     *
     * @param wageType Wage type to validate
     * @return true if valid
     */
    private fun isValidWageType(wageType: String): Boolean {
        return wageType in listOf("per_shift", "per_hour", "per_day")
    }
}
