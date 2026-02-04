package com.example.dwhubfix.data

import android.content.Context
import com.example.dwhubfix.data.repository.MatchingRepository
import com.example.dwhubfix.data.repository.BusinessMatchingRepository
import com.example.dwhubfix.model.JobWithScore
import com.example.dwhubfix.model.BusinessMatchingResult
import org.osmdroid.util.GeoPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Compatibility object for MatchingRepository
 *
 * This provides backward compatibility while migrating to DI.
 * TODO: Remove this in Phase 2 when UI is migrated to ViewModels
 * @deprecated Use injected MatchingRepository via ViewModels
 */
@Deprecated("Replace with ViewModel-based approach")
object MatchingRepository {

    /**
     * Temporary singleton instance
     * TODO: Remove in Phase 2
     */
    private lateinit var impl: MatchingRepository

    /**
     * Initialize the repository (call from Application.onCreate)
     */
    fun init(context: Context) {
        // For now, this is a placeholder
        // In Phase 2, this will be properly injected
    }

    suspend fun getJobsForWorker(
        workerLocation: GeoPoint? = null
    ): Result<List<JobWithScore>> {
        // TODO: Use injected repository in Phase 2
        // For now, return empty list to avoid crashes
        return withContext(Dispatchers.IO) {
            Result.success(emptyList())
        }
    }
}

/**
 * Compatibility object for BusinessMatchingRepository
 *
 * This provides backward compatibility while migrating to DI.
 * TODO: Remove this in Phase 2 when UI is migrated to ViewModels
 * @deprecated Use injected BusinessMatchingRepository via ViewModels
 */
@Deprecated("Replace with ViewModel-based approach")
object BusinessMatchingRepository {

    /**
     * Temporary singleton instance
     * TODO: Remove in Phase 2
     */
    private lateinit var impl: BusinessMatchingRepository

    /**
     * Initialize the repository (call from Application.onCreate)
     */
    fun init(context: Context) {
        // For now, this is a placeholder
        // In Phase 2, this will be properly injected
    }

    suspend fun getWorkersForJob(jobId: String): Result<BusinessMatchingResult> {
        // TODO: Use injected repository in Phase 2
        // For now, return empty result to avoid crashes
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            Result.success(BusinessMatchingResult(
                jobId = jobId,
                candidates = emptyList(),
                totalCandidates = 0,
                matchedCandidates = 0,
                averageScore = 0.0,
                recommendedCandidate = null
            ))
        }
    }

    suspend fun getWorkerWithSkills(workerId: String): Result<Map<String, Any?>> {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            Result.success(emptyMap())
        }
    }

    suspend fun getBusinessJobApplications(businessId: String): Result<List<Map<String, Any?>>> {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            Result.success(emptyList())
        }
    }
}
