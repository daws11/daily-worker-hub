package com.example.dwhubfix.domain.usecase

import com.example.dwhubfix.domain.model.Job
import com.example.dwhubfix.domain.repository.JobRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

/**
 * Unit Tests for GetBusinessJobsUseCase
 *
 * Tests retrieving jobs posted by the current business user, including:
 * - Success scenarios
 * - Empty list scenarios
 * - Error handling
 */
class GetBusinessJobsUseCaseTest {

    private lateinit var useCase: GetBusinessJobsUseCase
    private lateinit var mockJobRepository: FakeJobRepository

    @Before
    fun setup() {
        mockJobRepository = FakeJobRepository()
        useCase = GetBusinessJobsUseCase(mockJobRepository)
    }

    // ==================== SUCCESS TESTS ====================

    @Test
    fun `returns list of jobs when jobs exist`() = runTest {
        // Arrange
        val jobs = listOf(
            createJob("job-1", "business-1", "Barista", "open"),
            createJob("job-2", "business-1", "Waiter", "filled"),
            createJob("job-3", "business-1", "Cleaner", "open")
        )
        mockJobRepository.availableJobs = jobs

        // Act
        val result = useCase()

        // Assert
        assertTrue("Should return success", result.isSuccess)
        val returnedJobs = result.getOrNull()
        assertNotNull("Jobs should not be null", returnedJobs)
        assertEquals("Should return 3 jobs", 3, returnedJobs?.size)
    }

    @Test
    fun `returns single job when only one job exists`() = runTest {
        // Arrange
        val jobs = listOf(
            createJob("job-1", "business-1", "Barista", "open")
        )
        mockJobRepository.availableJobs = jobs

        // Act
        val result = useCase()

        // Assert
        assertTrue("Should return success", result.isSuccess)
        val returnedJobs = result.getOrNull()
        assertEquals("Should return 1 job", 1, returnedJobs?.size)
        assertEquals("Job title should match", "Barista", returnedJobs?.first()?.title)
    }

    @Test
    fun `returns empty list when no jobs exist`() = runTest {
        // Arrange
        mockJobRepository.availableJobs = emptyList()

        // Act
        val result = useCase()

        // Assert
        assertTrue("Should return success", result.isSuccess)
        val returnedJobs = result.getOrNull()
        assertNotNull("Jobs should not be null", returnedJobs)
        assertEquals("Should return empty list", 0, returnedJobs?.size)
    }

    @Test
    fun `returns jobs with different statuses`() = runTest {
        // Arrange
        val jobs = listOf(
            createJob("job-1", "business-1", "Open Job", "open"),
            createJob("job-2", "business-1", "Filled Job", "filled"),
            createJob("job-3", "business-1", "Cancelled Job", "cancelled"),
            createJob("job-4", "business-1", "Closed Job", "closed")
        )
        mockJobRepository.availableJobs = jobs

        // Act
        val result = useCase()

        // Assert
        assertTrue("Should return success", result.isSuccess)
        val returnedJobs = result.getOrNull()
        assertEquals("Should return 4 jobs", 4, returnedJobs?.size)

        val statuses = returnedJobs?.map { it.status }
        assertTrue("Should contain 'open' status", statuses?.contains("open") ?: false)
        assertTrue("Should contain 'filled' status", statuses?.contains("filled") ?: false)
        assertTrue("Should contain 'cancelled' status", statuses?.contains("cancelled") ?: false)
        assertTrue("Should contain 'closed' status", statuses?.contains("closed") ?: false)
    }

    @Test
    fun `returns jobs with all wage types`() = runTest {
        // Arrange
        val jobs = listOf(
            createJobWithWageType("job-1", "business-1", "Hourly Job", "hourly", 50000.0),
            createJobWithWageType("job-2", "business-1", "Daily Job", "daily", 350000.0),
            createJobWithWageType("job-3", "business-1", "Shift Job", "shift", 150000.0)
        )
        mockJobRepository.availableJobs = jobs

        // Act
        val result = useCase()

        // Assert
        assertTrue("Should return success", result.isSuccess)
        val returnedJobs = result.getOrNull()
        assertEquals("Should return 3 jobs", 3, returnedJobs?.size)

        val wageTypes = returnedJobs?.map { it.wageType }
        assertTrue("Should contain 'hourly' wage type", wageTypes?.contains("hourly") ?: false)
        assertTrue("Should contain 'daily' wage type", wageTypes?.contains("daily") ?: false)
        assertTrue("Should contain 'shift' wage type", wageTypes?.contains("shift") ?: false)
    }

    // ==================== ERROR TESTS ====================

    @Test
    fun `fails when repository returns failure`() = runTest {
        // Arrange
        mockJobRepository.shouldFailGetAvailableJobs = true

        // Act
        val result = useCase()

        // Assert
        assertTrue("Should fail when repository fails", result.isFailure)
        assertNotNull("Exception should be present", result.exceptionOrNull())
    }

    @Test
    fun `exception message is propagated from repository`() = runTest {
        // Arrange
        mockJobRepository.shouldFailGetAvailableJobs = true
        val expectedMessage = "Failed to fetch available jobs"

        // Act
        val result = useCase()

        // Assert
        assertTrue("Should fail", result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue("Exception message should match", exception?.message?.contains("Failed") ?: false)
    }

    // ==================== EDGE CASES ====================

    @Test
    fun `returns jobs with all categories`() = runTest {
        // Arrange
        val jobs = listOf(
            createJobWithCategory("job-1", "business-1", "Cleaning Job", "cleaning"),
            createJobWithCategory("job-2", "business-1", "Service Job", "service"),
            createJobWithCategory("job-3", "business-1", "Kitchen Job", "kitchen")
        )
        mockJobRepository.availableJobs = jobs

        // Act
        val result = useCase()

        // Assert
        assertTrue("Should return success", result.isSuccess)
        val returnedJobs = result.getOrNull()
        assertEquals("Should return 3 jobs", 3, returnedJobs?.size)

        val categories = returnedJobs?.map { it.category }
        assertTrue("Should contain 'cleaning' category", categories?.contains("cleaning") ?: false)
        assertTrue("Should contain 'service' category", categories?.contains("service") ?: false)
        assertTrue("Should contain 'kitchen' category", categories?.contains("kitchen") ?: false)
    }

    @Test
    fun `returns jobs with urgent flag variations`() = runTest {
        // Arrange
        val jobs = listOf(
            createJobWithUrgent("job-1", "business-1", "Urgent Job", isUrgent = true),
            createJobWithUrgent("job-2", "business-1", "Regular Job", isUrgent = false)
        )
        mockJobRepository.availableJobs = jobs

        // Act
        val result = useCase()

        // Assert
        assertTrue("Should return success", result.isSuccess)
        val returnedJobs = result.getOrNull()
        assertEquals("Should return 2 jobs", 2, returnedJobs?.size)

        val urgentJob = returnedJobs?.find { it.id == "job-1" }
        val regularJob = returnedJobs?.find { it.id == "job-2" }

        assertTrue("Urgent job should have isUrgent = true", urgentJob?.isUrgent ?: false)
        assertFalse("Regular job should have isUrgent = false", regularJob?.isUrgent ?: true)
    }

    @Test
    fun `returns jobs with different worker counts`() = runTest {
        // Arrange
        val jobs = listOf(
            createJobWithWorkerCount("job-1", "business-1", "Single Worker Job", 1),
            createJobWithWorkerCount("job-2", "business-1", "Multiple Workers Job", 5),
            createJobWithWorkerCount("job-3", "business-1", "Team Job", 10)
        )
        mockJobRepository.availableJobs = jobs

        // Act
        val result = useCase()

        // Assert
        assertTrue("Should return success", result.isSuccess)
        val returnedJobs = result.getOrNull()
        assertEquals("Should return 3 jobs", 3, returnedJobs?.size)

        val job1 = returnedJobs?.find { it.id == "job-1" }
        val job2 = returnedJobs?.find { it.id == "job-2" }
        val job3 = returnedJobs?.find { it.id == "job-3" }

        assertEquals("Job 1 should have 1 worker", 1, job1?.workerCount)
        assertEquals("Job 2 should have 5 workers", 5, job2?.workerCount)
        assertEquals("Job 3 should have 10 workers", 10, job3?.workerCount)
    }

    @Test
    fun `handles repository returning same list on multiple calls`() = runTest {
        // Arrange
        val jobs = listOf(
            createJob("job-1", "business-1", "Job 1", "open"),
            createJob("job-2", "business-1", "Job 2", "open")
        )
        mockJobRepository.availableJobs = jobs

        // Act
        val result1 = useCase()
        val result2 = useCase()

        // Assert
        assertTrue("First call should succeed", result1.isSuccess)
        assertTrue("Second call should succeed", result2.isSuccess)
        assertEquals("Both calls should return same number of jobs", result1.getOrNull()?.size, result2.getOrNull()?.size)
    }

    // ==================== HELPER FUNCTIONS ====================

    private fun createJob(
        id: String,
        businessId: String,
        title: String,
        status: String
    ): Job {
        return Job(
            id = id,
            businessId = businessId,
            title = title,
            description = "Test description",
            wage = 150000.0,
            wageType = "daily",
            location = "Test Location",
            category = "cleaning",
            status = status,
            createdAt = LocalDateTime.now().toString(),
            updatedAt = LocalDateTime.now().toString(),
            isUrgent = false,
            isCompliant = null,
            workerCount = 1,
            businessName = "Test Business",
            businessLatitude = null,
            businessLongitude = null
        )
    }

    private fun createJobWithWageType(
        id: String,
        businessId: String,
        title: String,
        wageType: String,
        wage: Double
    ): Job {
        return Job(
            id = id,
            businessId = businessId,
            title = title,
            description = "Test description",
            wage = wage,
            wageType = wageType,
            location = "Test Location",
            category = "cleaning",
            status = "open",
            createdAt = LocalDateTime.now().toString(),
            updatedAt = LocalDateTime.now().toString(),
            isUrgent = false,
            isCompliant = null,
            workerCount = 1,
            businessName = "Test Business",
            businessLatitude = null,
            businessLongitude = null
        )
    }

    private fun createJobWithCategory(
        id: String,
        businessId: String,
        title: String,
        category: String
    ): Job {
        return Job(
            id = id,
            businessId = businessId,
            title = title,
            description = "Test description",
            wage = 150000.0,
            wageType = "daily",
            location = "Test Location",
            category = category,
            status = "open",
            createdAt = LocalDateTime.now().toString(),
            updatedAt = LocalDateTime.now().toString(),
            isUrgent = false,
            isCompliant = null,
            workerCount = 1,
            businessName = "Test Business",
            businessLatitude = null,
            businessLongitude = null
        )
    }

    private fun createJobWithUrgent(
        id: String,
        businessId: String,
        title: String,
        isUrgent: Boolean
    ): Job {
        return Job(
            id = id,
            businessId = businessId,
            title = title,
            description = "Test description",
            wage = 150000.0,
            wageType = "daily",
            location = "Test Location",
            category = "cleaning",
            status = "open",
            createdAt = LocalDateTime.now().toString(),
            updatedAt = LocalDateTime.now().toString(),
            isUrgent = isUrgent,
            isCompliant = null,
            workerCount = 1,
            businessName = "Test Business",
            businessLatitude = null,
            businessLongitude = null
        )
    }

    private fun createJobWithWorkerCount(
        id: String,
        businessId: String,
        title: String,
        workerCount: Int
    ): Job {
        return Job(
            id = id,
            businessId = businessId,
            title = title,
            description = "Test description",
            wage = 150000.0,
            wageType = "daily",
            location = "Test Location",
            category = "cleaning",
            status = "open",
            createdAt = LocalDateTime.now().toString(),
            updatedAt = LocalDateTime.now().toString(),
            isUrgent = false,
            isCompliant = null,
            workerCount = workerCount,
            businessName = "Test Business",
            businessLatitude = null,
            businessLongitude = null
        )
    }

    // ==================== FAKE REPOSITORY ====================

    /**
     * Fake JobRepository for testing GetBusinessJobsUseCase
     *
     * Provides predictable behavior without real network calls.
     */
    private class FakeJobRepository : JobRepository {

        var availableJobs: List<Job> = emptyList()
        var shouldFailGetAvailableJobs: Boolean = false

        override suspend fun getAvailableJobs(): Result<List<Job>> {
            if (shouldFailGetAvailableJobs) {
                return Result.failure(Exception("Failed to fetch available jobs"))
            }
            return Result.success(availableJobs)
        }

        // Not implemented for GetBusinessJobsUseCase tests
        override suspend fun getWorkerProfile(): Result<com.example.dwhubfix.domain.model.UserProfile> =
            Result.failure(NotImplementedError("Not implemented"))

        override suspend fun getWorkerHistory(): Result<List<com.example.dwhubfix.domain.model.JobApplication>> =
            Result.failure(NotImplementedError("Not implemented"))

        override suspend fun getWorkerStats(): Result<com.example.dwhubfix.domain.model.WorkerStats?> =
            Result.failure(NotImplementedError("Not implemented"))

        override suspend fun acceptJob(jobId: String): Result<kotlin.Unit> =
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
