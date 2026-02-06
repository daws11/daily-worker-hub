package com.example.dwhubfix.domain.usecase

import com.example.dwhubfix.domain.model.CompleteJobRequest
import com.example.dwhubfix.domain.model.Job
import com.example.dwhubfix.domain.model.JobApplication
import com.example.dwhubfix.domain.repository.JobRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Unit Tests for CompleteJobUseCase
 *
 * Tests worker marking a job as completed, including:
 * - Status validation (accepted/ongoing only)
 * - Hours worked calculation
 * - Payment calculations (gross, commission, net)
 * - Error handling
 */
class CompleteJobUseCaseTest {

    private lateinit var useCase: CompleteJobUseCase
    private lateinit var mockJobRepository: FakeJobRepository

    @Before
    fun setup() {
        mockJobRepository = FakeJobRepository()
        useCase = CompleteJobUseCase(mockJobRepository)
    }

    // ==================== SUCCESS TESTS ====================

    @Test
    fun `complete job with accepted status returns success`() = runTest {
        // Arrange
        val startTime = LocalDateTime.now().minusHours(8)
        val completedAt = LocalDateTime.now()

        val application = createApplicationWithStartTime("app-1", "job-1", "accepted", startTime)
        mockJobRepository.application = application

        val job = createJob("job-1", 150000.0)
        mockJobRepository.job = job

        val request = CompleteJobRequest(
            applicationId = "app-1",
            completedAt = completedAt
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should succeed with accepted status", result.isSuccess)
        assertEquals("Repository should be called with correct application ID", "app-1", mockJobRepository.lastCompletedApplicationId)
        assertTrue("Repository should receive approximately 8 hours worked", mockJobRepository.lastHoursWorked >= 7.9 && mockJobRepository.lastHoursWorked <= 8.1)
    }

    @Test
    fun `complete job with ongoing status returns success`() = runTest {
        // Arrange
        val application = createApplication("app-2", "job-2", "ongoing")
        mockJobRepository.application = application

        val job = createJob("job-2", 200000.0)
        mockJobRepository.job = job

        val request = CompleteJobRequest(
            applicationId = "app-2",
            completedAt = LocalDateTime.now()
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should succeed with ongoing status", result.isSuccess)
    }

    @Test
    fun `calculates platform commission correctly at 6 percent`() = runTest {
        // Arrange
        val application = createApplication("app-1", "job-1", "accepted")
        mockJobRepository.application = application

        val job = createJob("job-1", 150000.0)
        mockJobRepository.job = job

        val request = CompleteJobRequest(
            applicationId = "app-1",
            completedAt = LocalDateTime.now()
        )

        // Act
        useCase(request)

        // Assert
        assertEquals("Gross amount should be 150000", 150000, mockJobRepository.lastGrossAmount)
        assertEquals("Platform commission should be 6% (9000)", 9000, mockJobRepository.lastPlatformCommission)
        assertEquals("Net worker amount should be gross - commission (141000)", 141000, mockJobRepository.lastIndexOfWorkerAmount)
    }

    @Test
    fun `calculates payment correctly for minimum wage`() = runTest {
        // Arrange
        val application = createApplication("app-1", "job-1", "accepted")
        mockJobRepository.application = application

        val job = createJob("job-1", 50000.0)
        mockJobRepository.job = job

        val request = CompleteJobRequest(
            applicationId = "app-1",
            completedAt = LocalDateTime.now()
        )

        // Act
        useCase(request)

        // Assert
        assertEquals("Gross amount should be 50000", 50000, mockJobRepository.lastGrossAmount)
        assertEquals("Platform commission should be 6% (3000)", 3000, mockJobRepository.lastPlatformCommission)
        assertEquals("Net worker amount should be 47000", 47000, mockJobRepository.lastIndexOfWorkerAmount)
    }

    @Test
    fun `calculates payment correctly for high wage`() = runTest {
        // Arrange
        val application = createApplication("app-1", "job-1", "accepted")
        mockJobRepository.application = application

        val job = createJob("job-1", 500000.0)
        mockJobRepository.job = job

        val request = CompleteJobRequest(
            applicationId = "app-1",
            completedAt = LocalDateTime.now()
        )

        // Act
        useCase(request)

        // Assert
        assertEquals("Gross amount should be 500000", 500000, mockJobRepository.lastGrossAmount)
        assertEquals("Platform commission should be 6% (30000)", 30000, mockJobRepository.lastPlatformCommission)
        assertEquals("Net worker amount should be 470000", 470000, mockJobRepository.lastIndexOfWorkerAmount)
    }

    // ==================== STATUS VALIDATION TESTS ====================

    @Test
    fun `fails when job status is pending`() = runTest {
        // Arrange
        val application = createApplication("app-1", "job-1", "pending")
        mockJobRepository.application = application

        val request = CompleteJobRequest(
            applicationId = "app-1",
            completedAt = LocalDateTime.now()
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should fail with pending status", result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue("Exception should mention accepted or ongoing", exception?.message?.contains("accepted or ongoing", ignoreCase = true) ?: false)
        assertTrue("Exception should be IllegalStateException", exception is IllegalStateException)
    }

    @Test
    fun `fails when job status is completed`() = runTest {
        // Arrange
        val application = createApplication("app-1", "job-1", "completed")
        mockJobRepository.application = application

        val request = CompleteJobRequest(
            applicationId = "app-1",
            completedAt = LocalDateTime.now()
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should fail with completed status", result.isFailure)
    }

    @Test
    fun `fails when job status is cancelled`() = runTest {
        // Arrange
        val application = createApplication("app-1", "job-1", "cancelled")
        mockJobRepository.application = application

        val request = CompleteJobRequest(
            applicationId = "app-1",
            completedAt = LocalDateTime.now()
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should fail with cancelled status", result.isFailure)
    }

    @Test
    fun `fails when job status is rejected`() = runTest {
        // Arrange
        val application = createApplication("app-1", "job-1", "rejected")
        mockJobRepository.application = application

        val request = CompleteJobRequest(
            applicationId = "app-1",
            completedAt = LocalDateTime.now()
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should fail with rejected status", result.isFailure)
    }

    // ==================== STARTED AT VALIDATION TESTS ====================

    @Test
    fun `fails when job has not been started`() = runTest {
        // Arrange
        val application = createApplication("app-1", "job-1", "accepted", startedAt = null)
        mockJobRepository.application = application

        val job = createJob("job-1", 150000.0)
        mockJobRepository.job = job

        val request = CompleteJobRequest(
            applicationId = "app-1",
            completedAt = LocalDateTime.now()
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should fail when job not started", result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue("Exception should mention not started", exception?.message?.contains("not been started", ignoreCase = true) ?: false)
        assertTrue("Exception should be IllegalStateException", exception is IllegalStateException)
    }

    @Test
    fun `calculates hours worked correctly from started time`() = runTest {
        // Arrange
        val startTime = LocalDateTime.now().minusHours(5)
        val completedAt = LocalDateTime.now()

        val application = createApplicationWithStartTime("app-1", "job-1", "accepted", startTime)
        mockJobRepository.application = application

        val job = createJob("job-1", 150000.0)
        mockJobRepository.job = job

        val request = CompleteJobRequest(
            applicationId = "app-1",
            completedAt = completedAt
        )

        // Act
        useCase(request)

        // Assert - Should be approximately 5 hours (with small tolerance for test execution time)
        assertTrue("Hours worked should be approximately 5", mockJobRepository.lastHoursWorked >= 4.9 && mockJobRepository.lastHoursWorked <= 5.1)
    }

    @Test
    fun `fails when completed at is before started at`() = runTest {
        // Arrange
        val startTime = LocalDateTime.now()
        val completedAt = startTime.minusHours(1) // Completed before started!

        val application = createApplicationWithStartTime("app-1", "job-1", "accepted", startTime)
        mockJobRepository.application = application

        val job = createJob("job-1", 150000.0)
        mockJobRepository.job = job

        val request = CompleteJobRequest(
            applicationId = "app-1",
            completedAt = completedAt
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should fail when completed before started", result.isFailure)
    }

    // ==================== PAYMENT CALCULATION TESTS ====================

    @Test
    fun `handles zero wage job`() = runTest {
        // Arrange
        val application = createApplication("app-1", "job-1", "accepted")
        mockJobRepository.application = application

        val job = createJob("job-1", 0.0)
        mockJobRepository.job = job

        val request = CompleteJobRequest(
            applicationId = "app-1",
            completedAt = LocalDateTime.now()
        )

        // Act
        useCase(request)

        // Assert
        assertEquals("Gross amount should be 0", 0, mockJobRepository.lastGrossAmount)
        assertEquals("Platform commission should be 0", 0, mockJobRepository.lastPlatformCommission)
        assertEquals("Net worker amount should be 0", 0, mockJobRepository.lastIndexOfWorkerAmount)
    }

    @Test
    fun `handles null wage by defaulting to zero`() = runTest {
        // Arrange
        val application = createApplication("app-1", "job-1", "accepted")
        mockJobRepository.application = application

        val job = createJobWithNullWage("job-1")
        mockJobRepository.job = job

        val request = CompleteJobRequest(
            applicationId = "app-1",
            completedAt = LocalDateTime.now()
        )

        // Act
        useCase(request)

        // Assert
        assertEquals("Gross amount should default to 0 for null wage", 0, mockJobRepository.lastGrossAmount)
    }

    // ==================== REPOSITORY ERROR TESTS ====================

    @Test
    fun `fails when get application by ID fails`() = runTest {
        // Arrange
        mockJobRepository.shouldFailGetApplicationById = true

        val request = CompleteJobRequest(
            applicationId = "app-1",
            completedAt = LocalDateTime.now()
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should fail when application fetch fails", result.isFailure)
    }

    @Test
    fun `fails when get job by ID fails`() = runTest {
        // Arrange
        val application = createApplication("app-1", "job-1", "accepted")
        mockJobRepository.application = application

        mockJobRepository.shouldFailGetJobById = true

        val request = CompleteJobRequest(
            applicationId = "app-1",
            completedAt = LocalDateTime.now()
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should fail when job fetch fails", result.isFailure)
    }

    @Test
    fun `fails when complete job repository call fails`() = runTest {
        // Arrange
        val application = createApplication("app-1", "job-1", "accepted")
        mockJobRepository.application = application

        val job = createJob("job-1", 150000.0)
        mockJobRepository.job = job

        mockJobRepository.shouldFailCompleteJob = true

        val request = CompleteJobRequest(
            applicationId = "app-1",
            completedAt = LocalDateTime.now()
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should fail when complete job fails", result.isFailure)
    }

    // ==================== HELPER FUNCTIONS ====================

    private fun createJob(id: String, wage: Double): Job {
        return Job(
            id = id,
            businessId = "business-1",
            title = "Test Job $id",
            description = "Test description",
            wage = wage,
            wageType = "daily",
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

    private fun createJobWithNullWage(id: String): Job {
        return Job(
            id = id,
            businessId = "business-1",
            title = "Test Job $id",
            description = "Test description",
            wage = null,
            wageType = "daily",
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

    private fun createApplication(
        id: String,
        jobId: String,
        status: String,
        startedAt: String? = LocalDateTime.now().minusHours(2).toString()
    ): JobApplication {
        return JobApplication(
            id = id,
            jobId = jobId,
            workerId = "worker-1",
            status = status,
            message = "Test message",
            appliedAt = LocalDateTime.now().minusHours(3).toString(),
            acceptedAt = LocalDateTime.now().minusHours(2).toString(),
            startedAt = startedAt,
            completedAt = null,
            workerRating = null,
            businessRating = null,
            workerReview = null,
            businessReview = null,
            cancellationReason = null,
            createdAt = LocalDateTime.now().minusHours(3).toString(),
            updatedAt = LocalDateTime.now().toString(),
            job = null
        )
    }

    private fun createApplicationWithStartTime(
        id: String,
        jobId: String,
        status: String,
        startTime: LocalDateTime
    ): JobApplication {
        return JobApplication(
            id = id,
            jobId = jobId,
            workerId = "worker-1",
            status = status,
            message = "Test message",
            appliedAt = startTime.minusHours(1).toString(),
            acceptedAt = startTime.toString(),
            startedAt = startTime.toString(),
            completedAt = null,
            workerRating = null,
            businessRating = null,
            workerReview = null,
            businessReview = null,
            cancellationReason = null,
            createdAt = startTime.minusHours(1).toString(),
            updatedAt = startTime.toString(),
            job = null
        )
    }

    // ==================== FAKE REPOSITORY ====================

    /**
     * Fake JobRepository for testing CompleteJobUseCase
     *
     * Provides predictable behavior without real network calls.
     */
    private class FakeJobRepository : JobRepository {

        var application: JobApplication? = null
        var job: Job? = null

        var lastCompletedApplicationId: String? = null
        var lastHoursWorked: Double = 0.0
        var lastGrossAmount: Int = 0
        var lastPlatformCommission: Int = 0
        var lastIndexOfWorkerAmount: Int = 0

        var shouldFailGetApplicationById: Boolean = false
        var shouldFailGetJobById: Boolean = false
        var shouldFailCompleteJob: Boolean = false

        override suspend fun getApplicationById(applicationId: String): Result<JobApplication> {
            if (shouldFailGetApplicationById) {
                return Result.failure(Exception("Failed to fetch application"))
            }
            val appToReturn = this.application
            return if (appToReturn != null && appToReturn.id == applicationId) {
                Result.success(appToReturn)
            } else {
                Result.failure(Exception("Application not found"))
            }
        }

        override suspend fun getJobById(jobId: String): Result<Job> {
            if (shouldFailGetJobById) {
                return Result.failure(Exception("Failed to fetch job"))
            }
            val jobToReturn = this.job
            return if (jobToReturn != null && jobToReturn.id == jobId) {
                Result.success(jobToReturn)
            } else {
                Result.failure(Exception("Job not found"))
            }
        }

        override suspend fun completeJob(
            applicationId: String,
            completedAt: String,
            hoursWorked: Double,
            grossAmount: Int,
            platformCommission: Int,
            netWorkerAmount: Int
        ): Result<kotlin.Unit> {
            if (shouldFailCompleteJob) {
                return Result.failure(Exception("Failed to complete job"))
            }

            lastCompletedApplicationId = applicationId
            lastHoursWorked = hoursWorked
            lastGrossAmount = grossAmount
            lastPlatformCommission = platformCommission
            lastIndexOfWorkerAmount = netWorkerAmount

            return Result.success(Unit)
        }

        // Not implemented for CompleteJobUseCase tests
        override suspend fun getWorkerProfile(): Result<com.example.dwhubfix.domain.model.UserProfile> =
            Result.failure(NotImplementedError("Not implemented"))

        override suspend fun getWorkerHistory(): Result<List<com.example.dwhubfix.domain.model.JobApplication>> =
            Result.failure(NotImplementedError("Not implemented"))

        override suspend fun getWorkerStats(): Result<com.example.dwhubfix.domain.model.WorkerStats?> =
            Result.failure(NotImplementedError("Not implemented"))

        override suspend fun getAvailableJobs(): Result<List<com.example.dwhubfix.domain.model.Job>> =
            Result.failure(NotImplementedError("Not implemented"))

        override suspend fun acceptJob(jobId: String): Result<kotlin.Unit> =
            Result.failure(NotImplementedError("Not implemented"))

        override suspend fun deleteJob(jobId: String): Result<kotlin.Unit> =
            Result.failure(NotImplementedError("Not implemented"))

        override suspend fun createJob(request: com.example.dwhubfix.domain.model.CreateJobRequest): Result<com.example.dwhubfix.domain.model.Job> =
            Result.failure(NotImplementedError("Not implemented"))

        override suspend fun applyForJob(request: com.example.dwhubfix.domain.model.ApplyForJobRequest): Result<com.example.dwhubfix.domain.model.JobApplication> =
            Result.failure(NotImplementedError("Not implemented"))

        override suspend fun getJobDetails(jobId: String): Result<com.example.dwhubfix.domain.model.JobWithDetails> =
            Result.failure(NotImplementedError("Not implemented"))
    }
}
