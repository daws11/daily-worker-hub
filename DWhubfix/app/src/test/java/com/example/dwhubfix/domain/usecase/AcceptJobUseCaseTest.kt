package com.example.dwhubfix.domain.usecase

import com.example.dwhubfix.domain.repository.JobRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit Tests for AcceptJobUseCase
 *
 * Tests worker accepting a job posting, including:
 * - Success scenarios
 * - Error handling
 * - Edge cases
 */
class AcceptJobUseCaseTest {

    private lateinit var useCase: AcceptJobUseCase
    private lateinit var mockJobRepository: FakeJobRepository

    @Before
    fun setup() {
        mockJobRepository = FakeJobRepository()
        useCase = AcceptJobUseCase(mockJobRepository)
    }

    // ==================== SUCCESS TESTS ====================

    @Test
    fun `accept job with valid job ID returns success`() = runTest {
        // Arrange
        val jobId = "job-123"

        // Act
        val result = useCase(jobId)

        // Assert
        assertTrue("Should return success for valid job ID", result.isSuccess)
    }

    @Test
    fun `accept job calls repository with correct job ID`() = runTest {
        // Arrange
        val jobId = "job-456"

        // Act
        useCase(jobId)

        // Assert
        assertEquals("Repository should be called with correct job ID", jobId, mockJobRepository.lastAcceptedJobId)
    }

    // ==================== ERROR TESTS ====================

    @Test
    fun `accept job fails when repository returns failure`() = runTest {
        // Arrange
        mockJobRepository.shouldFailAcceptJob = true

        // Act
        val result = useCase("job-123")

        // Assert
        assertTrue("Should fail when repository fails", result.isFailure)
        assertNotNull("Exception should be present", result.exceptionOrNull())
    }

    // ==================== EDGE CASES ====================

    @Test
    fun `accept job with empty job ID returns failure`() = runTest {
        // Arrange
        val jobId = ""

        // Act
        val result = useCase(jobId)

        // Assert
        assertTrue("Should fail with empty job ID", result.isFailure)
    }

    @Test
    fun `accept job with blank job ID returns failure`() = runTest {
        // Arrange
        val jobId = "   "

        // Act
        val result = useCase(jobId)

        // Assert
        assertTrue("Should fail with blank job ID", result.isFailure)
    }

    @Test
    fun `multiple job accepts are handled correctly`() = runTest {
        // Arrange
        val jobIds = listOf("job-1", "job-2", "job-3")

        // Act
        val results = jobIds.map { useCase(it) }

        // Assert
        assertTrue("All accepts should succeed", results.all { it.isSuccess })
        assertEquals("Repository should be called 3 times", 3, mockJobRepository.acceptJobCallCount)
    }

    // ==================== FAKE REPOSITORY ====================

    /**
     * Fake JobRepository for testing AcceptJobUseCase
     *
     * Provides predictable behavior without real network calls.
     */
    private class FakeJobRepository : JobRepository {

        var lastAcceptedJobId: String? = null
        var acceptJobCallCount: Int = 0
        var shouldFailAcceptJob: Boolean = false

        override suspend fun acceptJob(jobId: String): Result<Unit> {
            acceptJobCallCount++
            lastAcceptedJobId = jobId

            if (shouldFailAcceptJob) {
                return Result.failure(Exception("Failed to accept job"))
            }

            if (jobId.isBlank()) {
                return Result.failure(IllegalArgumentException("Job ID cannot be blank"))
            }

            return Result.success(Unit)
        }

        // Not implemented for AcceptJobUseCase tests
        override suspend fun getWorkerProfile(): Result<com.example.dwhubfix.domain.model.UserProfile> =
            Result.failure(NotImplementedError("Not implemented"))

        override suspend fun getWorkerHistory(): Result<List<com.example.dwhubfix.domain.model.JobApplication>> =
            Result.failure(NotImplementedError("Not implemented"))

        override suspend fun getWorkerStats(): Result<com.example.dwhubfix.domain.model.WorkerStats?> =
            Result.failure(NotImplementedError("Not implemented"))

        override suspend fun getAvailableJobs(): Result<List<com.example.dwhubfix.domain.model.Job>> =
            Result.failure(NotImplementedError("Not implemented"))

        override suspend fun getJobById(jobId: String): Result<com.example.dwhubfix.domain.model.Job> =
            Result.failure(NotImplementedError("Not implemented"))

        override suspend fun getApplicationById(applicationId: String): Result<com.example.dwhubfix.domain.model.JobApplication> =
            Result.failure(NotImplementedError("Not implemented"))

        override suspend fun deleteJob(jobId: String): Result<kotlin.Unit> =
            Result.failure(NotImplementedError("Not implemented"))

        override suspend fun createJob(request: com.example.dwhubfix.domain.model.CreateJobRequest): Result<com.example.dwhubfix.domain.model.Job> =
            Result.failure(NotImplementedError("Not implemented"))

        override suspend fun applyForJob(request: com.example.dwhubfix.domain.model.ApplyForJobRequest): Result<com.example.dwhubfix.domain.model.JobApplication> =
            Result.failure(NotImplementedError("Not implemented"))

        override suspend fun completeJob(
            applicationId: String,
            completedAt: String,
            hoursWorked: Double,
            grossAmount: Int,
            platformCommission: Int,
            netWorkerAmount: Int
        ): Result<kotlin.Unit> =
            Result.failure(NotImplementedError("Not implemented"))

        override suspend fun getJobDetails(jobId: String): Result<com.example.dwhubfix.domain.model.JobWithDetails> =
            Result.failure(NotImplementedError("Not implemented"))
    }
}
