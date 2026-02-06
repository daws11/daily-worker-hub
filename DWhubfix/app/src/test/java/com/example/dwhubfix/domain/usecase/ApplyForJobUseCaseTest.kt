package com.example.dwhubfix.domain.usecase

import com.example.dwhubfix.domain.model.ApplyForJobRequest
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
 * Unit Tests for ApplyForJobUseCase
 *
 * Tests aplikasi worker untuk job, termasuk:
 * - Status job validation
 * - Duplicate application prevention
 * - 21 Days Rule compliance
 * - Error handling
 */
class ApplyForJobUseCaseTest {

    private lateinit var useCase: ApplyForJobUseCase
    private lateinit var mockJobRepository: FakeJobRepository

    @Before
    fun setup() {
        mockJobRepository = FakeJobRepository()
        useCase = ApplyForJobUseCase(mockJobRepository)
    }

    // ==================== SUCCESS TESTS ====================

    @Test
    fun `applies successfully for open job`() = runTest {
        // Arrange
        val job = createJob("job-1", "business-1", "open")
        mockJobRepository.job = job
        mockJobRepository.workerHistory = emptyList()

        val request = ApplyForJobRequest(
            jobId = "job-1",
            coverLetter = "I am interested in this position"
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should return success", result.isSuccess)
        val application = result.getOrNull()
        assertNotNull("Application should not be null", application)
        assertEquals("Application should be for correct job", "job-1", application?.jobId)
        assertEquals("Application status should be pending", "pending", application?.status)
    }

    @Test
    fun `applies successfully for filled job`() = runTest {
        // Arrange
        val job = createJob("job-2", "business-2", "filled")
        mockJobRepository.job = job
        mockJobRepository.workerHistory = emptyList()

        val request = ApplyForJobRequest(
            jobId = "job-2",
            coverLetter = "I can fill this position"
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should return success for filled job", result.isSuccess)
    }

    @Test
    fun `applies without cover letter`() = runTest {
        // Arrange
        val job = createJob("job-3", "business-3", "open")
        mockJobRepository.job = job
        mockJobRepository.workerHistory = emptyList()

        val request = ApplyForJobRequest(
            jobId = "job-3",
            coverLetter = null // No cover letter provided
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should return success without cover letter", result.isSuccess)
    }

    // ==================== JOB STATUS VALIDATION TESTS ====================

    @Test
    fun `fails when job status is closed`() = runTest {
        // Arrange
        val job = createJob("job-1", "business-1", "closed")
        mockJobRepository.job = job
        mockJobRepository.workerHistory = emptyList()

        val request = ApplyForJobRequest(
            jobId = "job-1",
            coverLetter = "Test cover letter"
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should fail for closed job", result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull("Exception should not be null", exception)
        assertTrue("Exception should mention availability", exception?.message?.contains("available", ignoreCase = true) ?: false)
    }

    @Test
    fun `fails when job status is cancelled`() = runTest {
        // Arrange
        val job = createJob("job-2", "business-2", "cancelled")
        mockJobRepository.job = job
        mockJobRepository.workerHistory = emptyList()

        val request = ApplyForJobRequest(
            jobId = "job-2",
            coverLetter = null
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should fail for cancelled job", result.isFailure)
    }

    @Test
    fun `fails when job status is completed`() = runTest {
        // Arrange
        val job = createJob("job-3", "business-3", "completed")
        mockJobRepository.job = job
        mockJobRepository.workerHistory = emptyList()

        val request = ApplyForJobRequest(
            jobId = "job-3",
            coverLetter = null
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should fail for completed job", result.isFailure)
    }

    // ==================== DUPLICATE APPLICATION TESTS ====================

    @Test
    fun `fails when worker already applied for job`() = runTest {
        // Arrange - Worker has history showing they applied to this job
        val job = createJob("job-1", "business-1", "open")
        mockJobRepository.job = job

        val previousApplication = createApplication("app-1", "job-1", "worker-1", "rejected")
        mockJobRepository.workerHistory = listOf(previousApplication)

        val request = ApplyForJobRequest(
            jobId = "job-1",
            coverLetter = "Trying to apply again"
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should fail when already applied", result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull("Exception should not be null", exception)
        assertTrue("Exception should mention already applied", exception?.message?.contains("already applied", ignoreCase = true) ?: false)
    }

    @Test
    fun `fails when worker has pending application for job`() = runTest {
        // Arrange
        val job = createJob("job-1", "business-1", "open")
        mockJobRepository.job = job

        val previousApplication = createApplication("app-1", "job-1", "worker-1", "pending")
        mockJobRepository.workerHistory = listOf(previousApplication)

        val request = ApplyForJobRequest(
            jobId = "job-1",
            coverLetter = null
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should fail with pending application", result.isFailure)
        assertTrue("Exception should mention already applied", result.exceptionOrNull()?.message?.contains("already applied", ignoreCase = true) ?: false)
    }

    @Test
    fun `allows apply for different job even if previous application exists`() = runTest {
        // Arrange - Worker applied to job-1 before, now applying to job-2
        val job1 = createJob("job-1", "business-1", "open")
        val job2 = createJob("job-2", "business-2", "open")
        mockJobRepository.job = job2 // Getting job-2 details

        val previousApplication = createApplication("app-1", "job-1", "worker-1", "rejected")
        mockJobRepository.workerHistory = listOf(previousApplication)

        val request = ApplyForJobRequest(
            jobId = "job-2",
            coverLetter = null
        )

        // Act
        val result = useCase(request)

        // Assert - History contains job-1 application, but we're applying to job-2
        assertTrue("Should allow applying to different job", result.isSuccess)
    }

    // ==================== 21 DAYS RULE COMPLIANCE TESTS ====================

    @Test
    fun `fails when worker exceeded 20 days for same client`() = runTest {
        // Arrange - Worker has worked 21 days for business-1 in last 30 days
        val job = createJob("job-1", "business-1", "open")
        mockJobRepository.job = job

        // Use dynamic dates within last 30 days
        // Days: 1 (29 days ago), 2 (28 days ago), ..., 20 (9 days ago), 21 (0 days ago)
        mockJobRepository.workerHistory = create21DaysHistoryDynamic("business-1")

        val request = ApplyForJobRequest(
            jobId = "job-1",
            coverLetter = "I want to apply"
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should fail with 21 days rule violation", result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull("Exception should not be null", exception)
        assertTrue("Exception should mention 21 days rule", exception?.message?.contains("20 days", ignoreCase = true) ?: false)
        assertTrue("Exception should be IllegalStateException", exception is IllegalStateException)
    }

    @Test
    fun `allows apply when worker worked exactly 20 days for same client`() = runTest {
        // Arrange - Worker has worked exactly 20 days for business-1
        val job = createJob("job-1", "business-1", "open")
        mockJobRepository.job = job

        // Use dynamic dates within last 30 days (oldest is 10 days ago)
        mockJobRepository.workerHistory = create20DaysHistoryDynamic("business-1")

        val request = ApplyForJobRequest(
            jobId = "job-1",
            coverLetter = null
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should allow apply at exactly 20 days", result.isSuccess)
    }

    @Test
    fun `allows apply when worker worked less than 20 days for same client`() = runTest {
        // Arrange - Worker has worked 10 days for business-1
        val job = createJob("job-1", "business-1", "open")
        mockJobRepository.job = job

        // Use dynamic dates within last 30 days
        mockJobRepository.workerHistory = createNDaysHistoryDynamic("business-1", 10)

        val request = ApplyForJobRequest(
            jobId = "job-1",
            coverLetter = null
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should allow apply at 10 days", result.isSuccess)
    }

    @Test
    fun `only counts completed applications for 21 days rule`() = runTest {
        // Arrange - Mix of completed, in_progress, pending applications
        val job = createJob("job-1", "business-1", "open")
        mockJobRepository.job = job

        // Use dynamic dates for all (within last 30 days)
        val today = LocalDate.now()
        mockJobRepository.workerHistory = listOf(
            createApplicationWithDate("app-1", "job-1", "business-1", "completed", today.minusDays(5)),
            createApplicationWithDate("app-2", "job-2", "business-1", "in_progress", today.minusDays(3)),
            createApplicationWithDate("app-3", "job-3", "business-1", "pending", today.minusDays(2)), // Should NOT count
            createApplicationWithDate("app-4", "job-4", "business-1", "rejected", today.minusDays(1)), // Should NOT count
            createApplicationWithDate("app-5", "job-5", "business-1", "completed", today.minusDays(15))
        )

        val request = ApplyForJobRequest(
            jobId = "job-1",
            coverLetter = null
        )

        // Act
        val result = useCase(request)

        // Assert - Should count only completed (2) not others, so total = 2 days
        assertTrue("Should succeed (only 2 completed jobs counted)", result.isSuccess)
    }

    @Test
    fun `only counts applications within last 30 days`() = runTest {
        // Arrange - Some completed jobs are older than 30 days
        val job = createJob("job-1", "business-1", "open")
        mockJobRepository.job = job

        val today = LocalDate.now()

        // Use dynamic dates
        val oldDate = today.minusDays(40) // Too old (40 days ago)
        val recentDate1 = today.minusDays(10) // Recent (10 days ago)
        val recentDate2 = today.minusDays(5)  // Recent (5 days ago)

        mockJobRepository.workerHistory = listOf(
            createApplicationWithDate("app-1", "job-1", "business-1", "completed", oldDate), // Too old
            createApplicationWithDate("app-2", "job-2", "business-1", "completed", recentDate1), // Recent
            createApplicationWithDate("app-3", "job-3", "business-1", "completed", recentDate2) // Recent
        )

        val request = ApplyForJobRequest(
            jobId = "job-1",
            coverLetter = null
        )

        // Act
        val result = useCase(request)

        // Assert - Only 2 recent jobs should count, not 3
        assertTrue("Should succeed (only 2 recent jobs counted)", result.isSuccess)
    }

    @Test
    fun `allows apply for different client even if exceeded 20 days for another`() = runTest {
        // Arrange - Worker has worked 21 days for business-1, but applying to business-2
        val job = createJob("job-1", "business-2", "open")
        mockJobRepository.job = job

        // Worker has history for business-1 (not business-2)
        mockJobRepository.workerHistory = create21DaysHistoryDynamic("business-1")

        val request = ApplyForJobRequest(
            jobId = "job-1",
            coverLetter = null
        )

        // Act
        val result = useCase(request)

        // Assert - History is for business-1, applying to business-2
        assertTrue("Should allow apply to different client", result.isSuccess)
    }

    // ==================== REPOSITORY ERROR TESTS ====================

    @Test
    fun `fails when get job by id fails`() = runTest {
        // Arrange
        mockJobRepository.shouldFailGetJobById = true

        val request = ApplyForJobRequest(
            jobId = "job-1",
            coverLetter = null
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should fail when job fetch fails", result.isFailure)
    }

    @Test
    fun `fails when get worker history fails`() = runTest {
        // Arrange
        mockJobRepository.job = createJob("job-1", "business-1", "open")
        mockJobRepository.shouldFailGetWorkerHistory = true

/ private fun createApplicationWithDate(/i    private fun createApplication(/
        val request = ApplyForJobRequest(
            jobId = "job-1",
            coverLetter = null
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should fail when history fetch fails", result.isFailure)
    }

    @Test
    fun `fails when apply for job repository fails`() = runTest {
        // Arrange
        mockJobRepository.job = createJob("job-1", "business-1", "open")
        mockJobRepository.workerHistory = emptyList()
        mockJobRepository.shouldFailApplyForJob = true

        val request = ApplyForJobRequest(
            jobId = "job-1",
            coverLetter = null
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should fail when apply fails", result.isFailure)
    }

    // ==================== HELPER FUNCTIONS ====================

    private fun createJob(
        id: String,
        businessId: String,
        status: String
    ): Job {
        return Job(
            id = id,
            businessId = businessId,
            title = "Test Job $id",
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

    private fun createApplicationWithDate(
        id: String,
        jobId: String,
        businessId: String,
        status: String,
        startedAt: LocalDate
    ): JobApplication {
        val startTime = startedAt.atStartOfDay().toString()
        return JobApplication(
            id = id,
            jobId = jobId,
            workerId = "worker-1",
            status = status,
            message = "Test message",
            appliedAt = LocalDateTime.now().toString(),
            acceptedAt = if (status != "pending") LocalDateTime.now().toString() else null,
            startedAt = startTime,
            completedAt = null,
            workerRating = null,
            businessRating = null,
            workerReview = null,
            businessReview = null,
            cancellationReason = null,
            createdAt = LocalDateTime.now().toString(),
            updatedAt = LocalDateTime.now().toString(),
            job = createJob(jobId, businessId, "open")
        )
    }

    private fun create20DaysHistoryDynamic(businessId: String): List<JobApplication> {
        // Create 20 jobs where the most recent was 10 days ago
        // This ensures all jobs are within the 30-day window
        val today = LocalDate.now()
        return (1..20).map { i ->
            val daysAgo = 30 - i
            val jobDate = today.minusDays(daysAgo.toLong())
            createApplicationWithDate(
                id = "app-20-$i",
                jobId = "job-20-$i",
                businessId = businessId,
                status = "completed",
                startedAt = jobDate
            )
        }
    }

    private fun create21DaysHistoryDynamic(businessId: String): List<JobApplication> {
        // Create 21 jobs where the most recent was 0 days ago (today)
        // This will violate the 20-day limit
        val today = LocalDate.now()
        return (1..21).map { i ->
            val daysAgo = i - 1
            val jobDate = today.minusDays(daysAgo.toLong())
            createApplicationWithDate(
                id = "app-21-$i",
                jobId = "job-21-$i",
                businessId = businessId,
                status = "completed",
                startedAt = jobDate
            )
        }
    }

    private fun createNDaysHistoryDynamic(businessId: String, n: Int): List<JobApplication> {
        // Create N jobs within last 30 days (most recent was 1 day ago)
        val today = LocalDate.now()
        return (1..n).map { i ->
            val daysAgo = i
            val jobDate = today.minusDays(daysAgo.toLong())
            createApplicationWithDate(
                id = "app-n-$i",
                jobId = "job-n-$i",
                businessId = businessId,
                status = "completed",
                startedAt = jobDate
            )
        }
    }

    // ==================== FAKE REPOSITORY ====================

    /**
     * Fake JobRepository for testing ApplyForJobUseCase
     *
     * Provides predictable behavior without real network calls.
     * Simulates job fetching, history, and application creation.
     */
    private class FakeJobRepository : JobRepository {

        var job: Job? = null
        var workerHistory: List<JobApplication> = emptyList()

        var shouldFailGetJobById: Boolean = false
        var shouldFailGetWorkerHistory: Boolean = false
        var shouldFailApplyForJob: Boolean = false

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

        override suspend fun getWorkerHistory(): Result<List<JobApplication>> {
            if (shouldFailGetWorkerHistory) {
                return Result.failure(Exception("Failed to fetch history"))
            }
            return Result.success(workerHistory)
        }

        override suspend fun applyForJob(request: ApplyForJobRequest): Result<JobApplication> {
            if (shouldFailApplyForJob) {
                return Result.failure(Exception("Failed to apply for job"))
            }

            val jobForApplication = this.job

            val application = JobApplication(
                id = "app-${System.currentTimeMillis()}",
                jobId = request.jobId,
                workerId = "worker-1",
                status = "pending",
                message = request.coverLetter,
                appliedAt = LocalDateTime.now().toString(),
                acceptedAt = null,
                startedAt = null,
                completedAt = null,
                workerRating = null,
                businessRating = null,
                workerReview = null,
                businessReview = null,
                cancellationReason = null,
                createdAt = LocalDateTime.now().toString(),
                updatedAt = LocalDateTime.now().toString(),
                job = jobForApplication
            )

            return Result.success(application)
        }

        // Not implemented for ApplyForJobUseCase tests
        override suspend fun getWorkerProfile(): Result<com.example.dwhubfix.domain.model.UserProfile> =
            Result.failure(NotImplementedError("Not implemented"))

        override suspend fun getWorkerStats(): Result<com.example.dwhubfix.domain.model.WorkerStats?> =
            Result.failure(NotImplementedError("Not implemented"))

        override suspend fun getAvailableJobs(): Result<List<Job>> =
            Result.failure(NotImplementedError("Not implemented"))

        override suspend fun acceptJob(jobId: String): Result<kotlin.Unit> =
            Result.failure(NotImplementedError("Not implemented"))

        override suspend fun getApplicationById(applicationId: String): Result<JobApplication> =
            Result.failure(NotImplementedError("Not implemented"))

        override suspend fun deleteJob(jobId: String): Result<kotlin.Unit> =
            Result.failure(NotImplementedError("Not implemented"))

        override suspend fun createJob(request: com.example.dwhubfix.domain.model.CreateJobRequest): Result<Job> =
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
