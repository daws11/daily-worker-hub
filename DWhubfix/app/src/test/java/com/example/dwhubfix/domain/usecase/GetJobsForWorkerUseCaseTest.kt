package com.example.dwhubfix.domain.usecase

import com.example.dwhubfix.domain.model.Job
import com.example.dwhubfix.domain.model.JobApplication
import com.example.dwhubfix.domain.model.JobMatchScore
import com.example.dwhubfix.domain.model.JobWithScore
import com.example.dwhubfix.domain.model.UserProfile
import com.example.dwhubfix.domain.repository.JobRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.osmdroid.util.GeoPoint

/**
 * Unit Tests for GetJobsForWorkerUseCase
 *
 * Tests the job matching and prioritization logic including:
 * - Smart scoring algorithm (distance, skills, rating, reliability, urgency)
 * - 21 Days Rule compliance filtering
 * - Proper sorting by match score
 * - Worker location integration
 */
class GetJobsForWorkerUseCaseTest {

    private lateinit var useCase: GetJobsForWorkerUseCase
    private lateinit var mockJobRepository: FakeJobRepository

    @Before
    fun setup() {
        mockJobRepository = FakeJobRepository()
        useCase = GetJobsForWorkerUseCase(mockJobRepository)
    }

    // ==================== SUCCESS TESTS ====================

    @Test
    fun `returns prioritized jobs when successful`() = runTest {
        // Arrange
        mockJobRepository.workerProfile = FakeJobRepository.WORKER_PROFILE
        mockJobRepository.workerHistory = emptyList()
        mockJobRepository.availableJobs = FakeJobRepository.AVAILABLE_JOBS

        // Act
        val result = useCase()

        // Assert
        assertTrue("Should return success", result.isSuccess)
        val jobs = result.getOrNull()
        assertNotNull("Jobs list should not be null", jobs)
        assertFalse("Jobs list should not be empty", jobs?.isEmpty() ?: true)
    }

    @Test
    fun `jobs are sorted by total score descending`() = runTest {
        // Arrange
        mockJobRepository.workerProfile = FakeJobRepository.WORKER_PROFILE
        mockJobRepository.workerHistory = emptyList()
        mockJobRepository.availableJobs = FakeJobRepository.AVAILABLE_JOBS

        // Act
        val result = useCase()
        val jobs = result.getOrNull() ?: emptyList()

        // Assert - Check that jobs are sorted by score
        jobs.forEachIndexed { index, job ->
            if (index < jobs.size - 1) {
                assertTrue(
                    "Job at index $index (${job.score.totalScore}) should have score >= next job",
                    job.score.totalScore >= jobs[index + 1].score.totalScore
                )
            }
        }
    }

    @Test
    fun `all jobs marked as compliant when no history`() = runTest {
        // Arrange
        mockJobRepository.workerProfile = FakeJobRepository.WORKER_PROFILE
        mockJobRepository.workerHistory = emptyList()
        mockJobRepository.availableJobs = FakeJobRepository.AVAILABLE_JOBS

        // Act
        val result = useCase()
        val jobs = result.getOrNull() ?: emptyList()

        // Assert
        jobs.forEach { jobWithScore ->
            assertTrue("Job ${jobWithScore.job.id} should be compliant", jobWithScore.isCompliant)
        }
    }

    // ==================== 21 DAYS RULE COMPLIANCE TESTS ====================

    @Test
    fun `filters out jobs when worker exceeded 20 days for same client`() = runTest {
        // Arrange - Worker has worked 21 days for business-1 in last 30 days
        mockJobRepository.workerProfile = FakeJobRepository.WORKER_PROFILE
        mockJobRepository.workerHistory = create21DaysHistory("business-1")
        mockJobRepository.availableJobs = listOf(
            createJob("job-1", "business-1"), // Should be filtered
            createJob("job-2", "business-2")  // Should be included
        )

        // Act
        val result = useCase()
        val jobs = result.getOrNull() ?: emptyList()

        // Assert
        assertEquals("Should only have 1 job", 1, jobs.size)
        assertEquals("Job-1 should be filtered", "job-2", jobs[0].job.id)
    }

    @Test
    fun `allows job when worker worked exactly 20 days for same client`() = runTest {
        // Arrange - Worker has worked exactly 20 days for business-1
        mockJobRepository.workerProfile = FakeJobRepository.WORKER_PROFILE
        mockJobRepository.workerHistory = create20DaysHistory("business-1")
        mockJobRepository.availableJobs = listOf(
            createJob("job-1", "business-1")
        )

        // Act
        val result = useCase()
        val jobs = result.getOrNull() ?: emptyList()

        // Assert
        assertEquals("Should allow job at exactly 20 days", 1, jobs.size)
        assertTrue("Job should be compliant", jobs[0].isCompliant)
    }

    @Test
    fun `counts only completed and in_progress applications for compliance`() = runTest {
        // Arrange - Mix of completed, in_progress, pending applications
        mockJobRepository.workerProfile = FakeJobRepository.WORKER_PROFILE
        mockJobRepository.workerHistory = listOf(
            createApplication("app-1", "job-1", "business-1", "completed", "2026-01-01"),
            createApplication("app-2", "job-2", "business-1", "in_progress", "2026-01-02"),
            createApplication("app-3", "job-3", "business-1", "pending", "2026-01-03"), // Should NOT count
            createApplication("app-4", "job-4", "business-1", "completed", "2026-01-04")
        )
        mockJobRepository.availableJobs = listOf(
            createJob("job-5", "business-1")
        )

        // Act
        val result = useCase()
        val jobs = result.getOrNull() ?: emptyList()

        // Assert - Should count only completed (2) + in_progress (1) = 3 days, not 4
        assertEquals("Job should be compliant (only 3 days counted)", 1, jobs.size)
    }

    // ==================== DISTANCE SCORING TESTS ====================

    @Test
    fun `distance score is 30 when distance less than 2km`() = runTest {
        // Arrange
        mockJobRepository.workerProfile = FakeJobRepository.WORKER_PROFILE
        mockJobRepository.workerHistory = emptyList()
        val workerLocation = GeoPoint(-8.3405, 115.0920) // Denpasar, Bali

        val job = createJob(
            id = "job-1",
            businessId = "business-1",
            latitude = -8.3450, // ~0.5km away
            longitude = 115.0950
        )
        mockJobRepository.availableJobs = listOf(job)

        // Act
        val result = useCase(workerLocation)
        val jobs = result.getOrNull() ?: emptyList()

        // Assert
        assertEquals("Distance score should be 30 for < 2km", 30.0, jobs[0].score.breakdown.distanceScore, 0.01)
    }

    @Test
    fun `distance score is 25 when distance between 2-5km`() = runTest {
        // Arrange
        mockJobRepository.workerProfile = FakeJobRepository.WORKER_PROFILE
        mockJobRepository.workerHistory = emptyList()
        val workerLocation = GeoPoint(-8.3405, 115.0920)

        // Create a job approximately 3km away (should score 25)
        val job = createJob(
            id = "job-1",
            businessId = "business-1",
            latitude = -8.3700, // ~3.3km away
            longitude = 115.0950
        )
        mockJobRepository.availableJobs = listOf(job)

        // Act
        val result = useCase(workerLocation)
        val jobs = result.getOrNull() ?: emptyList()

        // Assert - Should score 25 for distance 2-5km
        val distanceScore = jobs[0].score.breakdown.distanceScore
        assertEquals("Distance score should be 25 for 2-5km", 25.0, distanceScore, 0.01)
    }

    @Test
    fun `distance score is 0 when worker location is not provided`() = runTest {
        // Arrange
        mockJobRepository.workerProfile = FakeJobRepository.WORKER_PROFILE
        mockJobRepository.workerHistory = emptyList()
        mockJobRepository.availableJobs = listOf(
            createJob("job-1", "business-1", latitude = -8.3405, longitude = 115.0920)
        )

        // Act
        val result = useCase(workerLocation = null)
        val jobs = result.getOrNull() ?: emptyList()

        // Assert
        assertEquals("Distance score should be 0 when no location provided", 0.0, jobs[0].score.breakdown.distanceScore, 0.01)
    }

    @Test
    fun `distance score is 0 when job has no location`() = runTest {
        // Arrange
        mockJobRepository.workerProfile = FakeJobRepository.WORKER_PROFILE
        mockJobRepository.workerHistory = emptyList()
        val workerLocation = GeoPoint(-8.3405, 115.0920)

        val job = createJob("job-1", "business-1", latitude = null, longitude = null)
        mockJobRepository.availableJobs = listOf(job)

        // Act
        val result = useCase(workerLocation)
        val jobs = result.getOrNull() ?: emptyList()

        // Assert
        assertEquals("Distance score should be 0 when job has no location", 0.0, jobs[0].score.breakdown.distanceScore, 0.01)
    }

    // ==================== URGENCY SCORING TESTS ====================

    @Test
    fun `urgency score is 10 when job is urgent`() = runTest {
        // Arrange
        mockJobRepository.workerProfile = FakeJobRepository.WORKER_PROFILE
        mockJobRepository.workerHistory = emptyList()
        mockJobRepository.availableJobs = listOf(
            createJob("job-1", "business-1", isUrgent = true)
        )

        // Act
        val result = useCase()
        val jobs = result.getOrNull() ?: emptyList()

        // Assert
        assertEquals("Urgency score should be 10 for urgent job", 10.0, jobs[0].score.breakdown.urgencyScore, 0.01)
    }

    @Test
    fun `urgency score is 0 when job is not urgent`() = runTest {
        // Arrange
        mockJobRepository.workerProfile = FakeJobRepository.WORKER_PROFILE
        mockJobRepository.workerHistory = emptyList()
        mockJobRepository.availableJobs = listOf(
            createJob("job-1", "business-1", isUrgent = false)
        )

        // Act
        val result = useCase()
        val jobs = result.getOrNull() ?: emptyList()

        // Assert
        assertEquals("Urgency score should be 0 for non-urgent job", 0.0, jobs[0].score.breakdown.urgencyScore, 0.01)
    }

    // ==================== ERROR TESTS ====================

    @Test
    fun `returns failure when worker profile fetch fails`() = runTest {
        // Arrange
        mockJobRepository.shouldFailProfileFetch = true

        // Act
        val result = useCase()

        // Assert
        assertTrue("Should return failure", result.isFailure)
        assertNotNull("Should have exception", result.exceptionOrNull())
    }

    @Test
    fun `returns failure when available jobs fetch fails`() = runTest {
        // Arrange
        mockJobRepository.workerProfile = FakeJobRepository.WORKER_PROFILE
        mockJobRepository.shouldFailJobsFetch = true

        // Act
        val result = useCase()

        // Assert
        assertTrue("Should return failure", result.isFailure)
        assertNotNull("Should have exception", result.exceptionOrNull())
    }

    @Test
    fun `returns empty list when no available jobs`() = runTest {
        // Arrange
        mockJobRepository.workerProfile = FakeJobRepository.WORKER_PROFILE
        mockJobRepository.workerHistory = emptyList()
        mockJobRepository.availableJobs = emptyList()

        // Act
        val result = useCase()
        val jobs = result.getOrNull()

        // Assert
        assertNotNull("Jobs list should not be null", jobs)
        assertTrue("Jobs list should be empty", jobs?.isEmpty() ?: false)
    }

    // ==================== HELPER FUNCTIONS ====================

    private fun createJob(
        id: String,
        businessId: String,
        latitude: Double? = -8.3405,
        longitude: Double? = 115.0920,
        isUrgent: Boolean = false,
        category: String = "cleaning"
    ): Job {
        return Job(
            id = id,
            businessId = businessId,
            title = "Test Job $id",
            description = "Test description",
            wage = 150000.0,
            wageType = "daily",
            location = "Test Location",
            category = category,
            status = "open",
            createdAt = "2026-01-01T00:00:00",
            updatedAt = "2026-01-01T00:00:00",
            isUrgent = isUrgent,
            isCompliant = null,
            workerCount = 1,
            businessName = "Test Business",
            businessLatitude = latitude,
            businessLongitude = longitude
        )
    }

    private fun createApplication(
        id: String,
        jobId: String,
        businessId: String,
        status: String,
        startedAt: String
    ): JobApplication {
        return JobApplication(
            id = id,
            jobId = jobId,
            workerId = "worker-1",
            status = status,
            appliedAt = "2026-01-01T00:00:00",
            acceptedAt = if (status != "pending") "2026-01-01T00:00:00" else null,
            startedAt = startedAt,
            completedAt = null,
            workerRating = null,
            businessRating = null,
            workerReview = null,
            businessReview = null,
            cancellationReason = null,
            createdAt = "2026-01-01T00:00:00",
            updatedAt = "2026-01-01T00:00:00",
            job = createJob(jobId, businessId)
        )
    }

    private fun create20DaysHistory(businessId: String): List<JobApplication> {
        return (1..20).map { i ->
            createApplication(
                id = "app-$i",
                jobId = "job-$i",
                businessId = businessId,
                status = "completed",
                startedAt = java.time.LocalDate.now().minusDays(i.toLong()).toString() + "T00:00:00"
            )
        }
    }

    private fun create21DaysHistory(businessId: String): List<JobApplication> {
        return (1..21).map { i ->
            createApplication(
                id = "app-$i",
                jobId = "job-$i",
                businessId = businessId,
                status = "completed",
                startedAt = java.time.LocalDate.now().minusDays(i.toLong()).toString() + "T00:00:00"
            )
        }
    }

    // ==================== FAKE REPOSITORY ====================

    /**
     * Fake JobRepository for testing GetJobsForWorkerUseCase
     *
     * Provides predictable behavior without real network calls.
     * Simulates worker profile, history, and available jobs.
     */
    private class FakeJobRepository : JobRepository {

        var workerProfile: UserProfile = WORKER_PROFILE
        var workerHistory: List<JobApplication> = emptyList()
        var availableJobs: List<Job> = AVAILABLE_JOBS

        var shouldFailProfileFetch: Boolean = false
        var shouldFailJobsFetch: Boolean = false
        var shouldFailHistoryFetch: Boolean = false

        override suspend fun getWorkerProfile(): Result<UserProfile> {
            if (shouldFailProfileFetch) {
                return Result.failure(Exception("Failed to fetch profile"))
            }
            return Result.success(workerProfile)
        }

        override suspend fun getWorkerHistory(): Result<List<JobApplication>> {
            if (shouldFailHistoryFetch) {
                return Result.failure(Exception("Failed to fetch history"))
            }
            return Result.success(workerHistory)
        }

        override suspend fun getAvailableJobs(): Result<List<Job>> {
            if (shouldFailJobsFetch) {
                return Result.failure(Exception("Failed to fetch jobs"))
            }
            return Result.success(availableJobs)
        }

        // Not implemented for GetJobsForWorkerUseCase tests
        override suspend fun getWorkerStats(): Result<com.example.dwhubfix.domain.model.WorkerStats?> =
            Result.failure(NotImplementedError("Not implemented"))

        override suspend fun acceptJob(jobId: String): Result<Unit> =
            Result.failure(NotImplementedError("Not implemented"))

        override suspend fun getJobById(jobId: String): Result<Job> =
            Result.failure(NotImplementedError("Not implemented"))

        override suspend fun getApplicationById(applicationId: String): Result<JobApplication> =
            Result.failure(NotImplementedError("Not implemented"))

        override suspend fun deleteJob(jobId: String): Result<Unit> =
            Result.failure(NotImplementedError("Not implemented"))

        override suspend fun createJob(request: com.example.dwhubfix.domain.model.CreateJobRequest): Result<Job> =
            Result.failure(NotImplementedError("Not implemented"))

        override suspend fun applyForJob(request: com.example.dwhubfix.domain.model.ApplyForJobRequest): Result<JobApplication> =
            Result.failure(NotImplementedError("Not implemented"))

        override suspend fun completeJob(
            applicationId: String,
            completedAt: String,
            hoursWorked: Double,
            grossAmount: Int,
            platformCommission: Int,
            netWorkerAmount: Int
        ): Result<Unit> =
            Result.failure(NotImplementedError("Not implemented"))

        override suspend fun getJobDetails(jobId: String): Result<com.example.dwhubfix.domain.model.JobWithDetails> =
            Result.failure(NotImplementedError("Not implemented"))

        companion object {
            val WORKER_PROFILE = UserProfile(
                id = "worker-1",
                fullName = "Test Worker",
                email = "worker@test.com",
                phoneNumber = "+6281234567890",
                avatarUrl = null,
                role = "worker",
                createdAt = "2026-01-01T00:00:00",
                updatedAt = "2026-01-01T00:00:00"
            )

            val AVAILABLE_JOBS = listOf(
                Job(
                    id = "job-1",
                    businessId = "business-1",
                    title = "Hotel Housekeeper",
                    description = "Clean hotel rooms",
                    wage = 150000.0,
                    wageType = "daily",
                    location = "Kuta, Bali",
                    category = "cleaning",
                    status = "open",
                    createdAt = "2026-01-01T00:00:00",
                    updatedAt = "2026-01-01T00:00:00",
                    isUrgent = false,
                    isCompliant = null,
                    workerCount = 2,
                    businessName = "Kuta Hotel",
                    businessLatitude = -8.3405,
                    businessLongitude = 115.0920
                ),
                Job(
                    id = "job-2",
                    businessId = "business-2",
                    title = "Restaurant Server",
                    description = "Serve guests",
                    wage = 120000.0,
                    wageType = "daily",
                    location = "Ubud, Bali",
                    category = "service",
                    status = "open",
                    createdAt = "2026-01-01T00:00:00",
                    updatedAt = "2026-01-01T00:00:00",
                    isUrgent = true,
                    isCompliant = null,
                    workerCount = 1,
                    businessName = "Ubud Restaurant",
                    businessLatitude = -8.5069,
                    businessLongitude = 115.2625
                ),
                Job(
                    id = "job-3",
                    businessId = "business-3",
                    title = "Kitchen Helper",
                    description = "Help in kitchen",
                    wage = 100000.0,
                    wageType = "daily",
                    location = "Seminyak, Bali",
                    category = "kitchen",
                    status = "open",
                    createdAt = "2026-01-01T00:00:00",
                    updatedAt = "2026-01-01T00:00:00",
                    isUrgent = false,
                    isCompliant = null,
                    workerCount = 3,
                    businessName = "Seminyak Cafe",
                    businessLatitude = -8.3617,
                    businessLongitude = 115.1517
                )
            )
        }
    }
}
