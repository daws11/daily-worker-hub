package com.example.dwhubfix.domain.usecase

import com.example.dwhubfix.domain.model.Job
import com.example.dwhubfix.domain.model.JobWithDetails
import com.example.dwhubfix.domain.repository.JobRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

/**
 * Unit Tests for GetJobDetailsUseCase
 *
 * Tests retrieving detailed job information, including:
 * - Success scenarios
 * - Helper functions (isJobAvailable, isAcceptingApplications)
 * - Error handling
 */
class GetJobDetailsUseCaseTest {

    private lateinit var useCase: GetJobDetailsUseCase
    private lateinit var mockJobRepository: FakeJobRepository

    @Before
    fun setup() {
        mockJobRepository = FakeJobRepository()
        useCase = GetJobDetailsUseCase(mockJobRepository)
    }

    // ==================== SUCCESS TESTS ====================

    @Test
    fun `returns job details for valid job ID`() = runTest {
        // Arrange
        val job = createJob("job-1", "business-1", "Barista Needed", "open")
        val jobDetails = createJobWithDetails(
            job = job,
            businessName = "Coffee Shop Inc",
            businessLocation = "Jakarta Selatan",
            businessRating = 4.5,
            applicationStatus = null
        )
        mockJobRepository.jobDetails = jobDetails

        // Act
        val result = useCase("job-1")

        // Assert
        assertTrue("Should return success", result.isSuccess)
        val details = result.getOrNull()
        assertNotNull("Job details should not be null", details)
        assertEquals("Job title should match", "Barista Needed", details?.job?.title)
        assertEquals("Business name should match", "Coffee Shop Inc", details?.businessName)
        assertEquals("Business location should match", "Jakarta Selatan", details?.businessLocation)
        assertEquals("Business rating should match", 4.5, details?.businessRating ?: 0.0, 0.01)
    }

    @Test
    fun `returns job details with pending application status`() = runTest {
        // Arrange
        val job = createJob("job-1", "business-1", "Waiter Needed", "open")
        val jobDetails = createJobWithDetails(
            job = job,
            businessName = "Restaurant XYZ",
            businessLocation = "Jakarta Pusat",
            businessRating = 4.0,
            applicationStatus = "pending",
            applicationId = "app-123"
        )
        mockJobRepository.jobDetails = jobDetails

        // Act
        val result = useCase("job-1")

        // Assert
        assertTrue("Should return success", result.isSuccess)
        val details = result.getOrNull()
        assertEquals("Application status should be pending", "pending", details?.applicationStatus)
        assertEquals("Application ID should match", "app-123", details?.applicationId)
        assertTrue("isApplied should be true", details?.isApplied ?: false)
        assertTrue("isPending should be true", details?.isPending ?: false)
        assertFalse("isAccepted should be false", details?.isAccepted ?: true)
    }

    @Test
    fun `returns job details with accepted application status`() = runTest {
        // Arrange
        val job = createJob("job-1", "business-1", "Cleaner Needed", "open")
        val jobDetails = createJobWithDetails(
            job = job,
            businessName = "Cleaning Co",
            businessLocation = "Jakarta Barat",
            businessRating = 4.8,
            applicationStatus = "accepted",
            applicationId = "app-456"
        )
        mockJobRepository.jobDetails = jobDetails

        // Act
        val result = useCase("job-1")

        // Assert
        assertTrue("Should return success", result.isSuccess)
        val details = result.getOrNull()
        assertEquals("Application status should be accepted", "accepted", details?.applicationStatus)
        assertTrue("isApplied should be true", details?.isApplied ?: false)
        assertTrue("isAccepted should be true", details?.isAccepted ?: false)
        assertFalse("isPending should be false", details?.isPending ?: true)
    }

    @Test
    fun `returns job details with rejected application status`() = runTest {
        // Arrange
        val job = createJob("job-1", "business-1", "Kitchen Staff", "open")
        val jobDetails = createJobWithDetails(
            job = job,
            businessName = "Kitchen Bros",
            businessLocation = "Jakarta Utara",
            businessRating = 3.5,
            applicationStatus = "rejected",
            applicationId = "app-789"
        )
        mockJobRepository.jobDetails = jobDetails

        // Act
        val result = useCase("job-1")

        // Assert
        assertTrue("Should return success", result.isSuccess)
        val details = result.getOrNull()
        assertEquals("Application status should be rejected", "rejected", details?.applicationStatus)
        assertTrue("isRejected should be true", details?.isRejected ?: false)
        assertFalse("isAccepted should be false", details?.isAccepted ?: true)
    }

    // ==================== ERROR TESTS ====================

    @Test
    fun `fails when repository returns failure`() = runTest {
        // Arrange
        mockJobRepository.shouldFailGetJobDetails = true

        // Act
        val result = useCase("job-1")

        // Assert
        assertTrue("Should fail when repository fails", result.isFailure)
        assertNotNull("Exception should be present", result.exceptionOrNull())
    }

    @Test
    fun `fails for empty job ID`() = runTest {
        // Arrange
        mockJobRepository.shouldFailGetJobDetails = true

        // Act
        val result = useCase("")

        // Assert
        assertTrue("Should fail with empty job ID", result.isFailure)
    }

    @Test
    fun `fails for blank job ID`() = runTest {
        // Arrange
        mockJobRepository.shouldFailGetJobDetails = true

        // Act
        val result = useCase("   ")

        // Assert
        assertTrue("Should fail with blank job ID", result.isFailure)
    }

    // ==================== HELPER FUNCTION TESTS ====================

    @Test
    fun `isJobAvailable returns true for open status`() {
        // Arrange
        val job = createJob("job-1", "business-1", "Test Job", "open")

        // Act
        val result = useCase.isJobAvailable(job)

        // Assert
        assertTrue("isJobAvailable should return true for 'open' status", result)
    }

    @Test
    fun `isJobAvailable returns true for filled status`() {
        // Arrange
        val job = createJob("job-1", "business-1", "Test Job", "filled")

        // Act
        val result = useCase.isJobAvailable(job)

        // Assert
        assertTrue("isJobAvailable should return true for 'filled' status", result)
    }

    @Test
    fun `isJobAvailable returns false for closed status`() {
        // Arrange
        val job = createJob("job-1", "business-1", "Test Job", "closed")

        // Act
        val result = useCase.isJobAvailable(job)

        // Assert
        assertFalse("isJobAvailable should return false for 'closed' status", result)
    }

    @Test
    fun `isJobAvailable returns false for cancelled status`() {
        // Arrange
        val job = createJob("job-1", "business-1", "Test Job", "cancelled")

        // Act
        val result = useCase.isJobAvailable(job)

        // Assert
        assertFalse("isJobAvailable should return false for 'cancelled' status", result)
    }

    @Test
    fun `isJobAvailable returns false for completed status`() {
        // Arrange
        val job = createJob("job-1", "business-1", "Test Job", "completed")

        // Act
        val result = useCase.isJobAvailable(job)

        // Assert
        assertFalse("isJobAvailable should return false for 'completed' status", result)
    }

    @Test
    fun `isJobAvailable returns false for unknown status`() {
        // Arrange
        val job = createJob("job-1", "business-1", "Test Job", "unknown")

        // Act
        val result = useCase.isJobAvailable(job)

        // Assert
        assertFalse("isJobAvailable should return false for unknown status", result)
    }

    @Test
    fun `isAcceptingApplications returns true for open status`() {
        // Arrange
        val job = createJob("job-1", "business-1", "Test Job", "open")

        // Act
        val result = useCase.isAcceptingApplications(job)

        // Assert
        assertTrue("isAcceptingApplications should return true for 'open' status", result)
    }

    @Test
    fun `isAcceptingApplications returns false for filled status`() {
        // Arrange
        val job = createJob("job-1", "business-1", "Test Job", "filled")

        // Act
        val result = useCase.isAcceptingApplications(job)

        // Assert
        assertFalse("isAcceptingApplications should return false for 'filled' status", result)
    }

    @Test
    fun `isAcceptingApplications returns false for closed status`() {
        // Arrange
        val job = createJob("job-1", "business-1", "Test Job", "closed")

        // Act
        val result = useCase.isAcceptingApplications(job)

        // Assert
        assertFalse("isAcceptingApplications should return false for 'closed' status", result)
    }

    @Test
    fun `isAcceptingApplications returns false for cancelled status`() {
        // Arrange
        val job = createJob("job-1", "business-1", "Test Job", "cancelled")

        // Act
        val result = useCase.isAcceptingApplications(job)

        // Assert
        assertFalse("isAcceptingApplications should return false for 'cancelled' status", result)
    }

    // ==================== EDGE CASES ====================

    @Test
    fun `handles job with null business rating`() = runTest {
        // Arrange
        val job = createJob("job-1", "business-1", "Test Job", "open")
        val jobDetails = createJobWithDetails(
            job = job,
            businessName = "Test Business",
            businessLocation = "Test Location",
            businessRating = null,
            applicationStatus = null
        )
        mockJobRepository.jobDetails = jobDetails

        // Act
        val result = useCase("job-1")

        // Assert
        assertTrue("Should return success", result.isSuccess)
        val details = result.getOrNull()
        assertNull("Business rating should be null", details?.businessRating)
    }

    @Test
    fun `handles job with all optional fields populated`() = runTest {
        // Arrange
        val job = createJobWithAllFields("job-1", "business-1")
        val jobDetails = createJobWithDetails(
            job = job,
            businessName = "Full Details Business",
            businessLocation = "Full Details Location",
            businessRating = 5.0,
            applicationStatus = "pending",
            applicationId = "app-full"
        )
        mockJobRepository.jobDetails = jobDetails

        // Act
        val result = useCase("job-1")

        // Assert
        assertTrue("Should return success", result.isSuccess)
        val details = result.getOrNull()
        assertNotNull("Job should have description", details?.job?.description)
        assertNotNull("Job should have wage", details?.job?.wage)
        assertNotNull("Job should have wage type", details?.job?.wageType)
        assertNotNull("Job should have location", details?.job?.location)
        assertNotNull("Job should have category", details?.job?.category)
        assertNotNull("Job should have start time", details?.job?.startTime)
        assertNotNull("Job should have end time", details?.job?.endTime)
        assertNotNull("Job should have shift date", details?.job?.shiftDate)
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
            description = null,
            wage = null,
            wageType = null,
            location = null,
            category = null,
            status = status,
            createdAt = null,
            updatedAt = null,
            startTime = null,
            endTime = null,
            shiftDate = null,
            isUrgent = false,
            isCompliant = null,
            workerCount = null,
            businessName = null,
            businessLatitude = null,
            businessLongitude = null
        )
    }

    private fun createJobWithAllFields(
        id: String,
        businessId: String
    ): Job {
        return Job(
            id = id,
            businessId = businessId,
            title = "Complete Job",
            description = "Full job description",
            wage = 150000.0,
            wageType = "daily",
            location = "Jakarta",
            category = "cleaning",
            status = "open",
            createdAt = LocalDateTime.now().toString(),
            updatedAt = LocalDateTime.now().toString(),
            startTime = "08:00",
            endTime = "17:00",
            shiftDate = "2024-12-01",
            isUrgent = true,
            isCompliant = true,
            workerCount = 3,
            businessName = "Test Business",
            businessLatitude = -6.2088,
            businessLongitude = 106.8456
        )
    }

    private fun createJobWithDetails(
        job: Job,
        businessName: String?,
        businessLocation: String?,
        businessRating: Double?,
        applicationStatus: String?,
        applicationId: String? = null
    ): JobWithDetails {
        return JobWithDetails(
            job = job,
            businessName = businessName,
            businessLocation = businessLocation,
            businessRating = businessRating,
            applicationStatus = applicationStatus,
            applicationId = applicationId
        )
    }

    // ==================== FAKE REPOSITORY ====================

    /**
     * Fake JobRepository for testing GetJobDetailsUseCase
     *
     * Provides predictable behavior without real network calls.
     */
    private class FakeJobRepository : JobRepository {

        var jobDetails: JobWithDetails? = null
        var shouldFailGetJobDetails: Boolean = false

        override suspend fun getJobDetails(jobId: String): Result<JobWithDetails> {
            if (shouldFailGetJobDetails) {
                return Result.failure(Exception("Failed to fetch job details"))
            }
            val detailsToReturn = this.jobDetails
            return if (detailsToReturn != null && detailsToReturn.job.id == jobId) {
                Result.success(detailsToReturn)
            } else {
                Result.failure(Exception("Job details not found"))
            }
        }

        // Not implemented for GetJobDetailsUseCase tests
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
    }
}
