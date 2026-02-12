package com.example.dwhubfix.domain.usecase

import com.example.dwhubfix.domain.model.WorkerStats
import com.example.dwhubfix.domain.repository.JobRepository
import javax.inject.Inject

/**
 * Get Worker Stats Use Case
 *
 * Retrieves worker statistics including completed shifts,
 * earnings, wallet balance, and ratings.
 *
 * @property jobRepository Repository for job operations
 */
class GetWorkerStatsUseCase @Inject constructor(
    private val jobRepository: JobRepository
) {

    /**
     * Execute use case
     *
     * @return Worker statistics
     */
    suspend operator fun invoke(): Result<WorkerStats> {
        return try {
            val stats = jobRepository.getWorkerStats()
                .getOrElse { throw it }

            // For now, return default stats if repository returns null
            Result.success(stats ?: WorkerStats())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
