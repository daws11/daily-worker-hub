package com.example.dwhubfix.domain.usecase

import com.example.dwhubfix.domain.model.CreateJobRequest
import com.example.dwhubfix.domain.model.Job
import com.example.dwhubfix.domain.repository.JobRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Unit Tests for CreateJobUseCase
 *
 * Tests the business logic for creating new job postings including:
 * - Input validation (wage, worker count, times, dates)
 * - Repository integration
 * - Error handling
 */
class CreateJobUseCaseTest {

    private lateinit var useCase: CreateJobUseCase
    private lateinit var mockJobRepository: FakeJobRepository

    @Before
    fun setup() {
        mockJobRepository = FakeJobRepository()
        useCase = CreateJobUseCase(mockJobRepository)
    }

    // ==================== SUCCESS TESTS ====================

    @Test
    fun `creates job successfully with valid data`() = runTest {
        // Arrange
        val request = CreateJobRequest(
            title = "Hotel Housekeeper",
            description = "Clean hotel rooms daily",
            wage = 150000.0,
            wageType = "per_day",
            location = "Kuta, Bali",
            category = "cleaning",
            shiftDate = LocalDate.now(),
            startTime = "08:00",
            endTime = "17:00",
            workerCount = 2,
            isUrgent = false
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should return success", result.isSuccess)
        val job = result.getOrNull()
        assertNotNull("Job should not be null", job)
        assertEquals("Job title should match", "Hotel Housekeeper", job?.title)
        assertEquals("Job wage should match", 150000.0, job?.wage)
        assertEquals("Job status should be open", "open", job?.status)
    }

    @Test
    fun `creates job with urgent flag`() = runTest {
        // Arrange
        val request = CreateJobRequest(
            title = "Urgent Server Needed",
            description = "Immediate need",
            wage = 120000.0,
            wageType = "per_shift",
            location = "Ubud, Bali",
            category = "service",
            shiftDate = LocalDate.now(),
            startTime = "09:00",
            endTime = "18:00",
            workerCount = 1,
            isUrgent = true
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should return success", result.isSuccess)
        val job = result.getOrNull()
        assertTrue("Job should be marked as urgent", job?.isUrgent ?: false)
    }

    @Test
    fun `creates job with shift date in future`() = runTest {
        // Arrange - Shift date 7 days from now
        val futureDate = LocalDate.now().plusDays(7)
        val request = CreateJobRequest(
            title = "Future Job",
            description = "Job next week",
            wage = 100000.0,
            wageType = "per_hour",
            location = "Seminyak, Bali",
            category = "kitchen",
            shiftDate = futureDate,
            startTime = "10:00",
            endTime = "14:00",
            workerCount = 3,
            isUrgent = false
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should return success with future date", result.isSuccess)
    }

    @Test
    fun `creates job with shift date today`() = runTest {
        // Arrange - Shift date is today
        val today = LocalDate.now()
        val request = CreateJobRequest(
            title = "Today's Job",
            description = "Job today",
            wage = 80000.0,
            wageType = "per_hour",
            location = "Canggu, Bali",
            category = "service",
            shiftDate = today,
            startTime = "13:00",
            endTime = "18:00",
            workerCount = 2,
            isUrgent = false
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should return success with today's date", result.isSuccess)
    }

    // ==================== WAGE VALIDATION TESTS ====================

    @Test
    fun `fails when wage is zero`() = runTest {
        // Arrange
        val request = CreateJobRequest(
            title = "Test Job",
            description = "Test",
            wage = 0.0,
            wageType = "per_day",
            location = "Kuta, Bali",
            category = "cleaning",
            shiftDate = LocalDate.now(),
            startTime = "08:00",
            endTime = "17:00",
            workerCount = 1,
            isUrgent = false
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should fail when wage is zero", result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull("Exception should not be null", exception)
        assertTrue("Exception message should mention wage", exception?.message?.contains("wage", ignoreCase = true) ?: false)
    }

    @Test
    fun `fails when wage is negative`() = runTest {
        // Arrange
        val request = CreateJobRequest(
            title = "Test Job",
            description = "Test",
            wage = -50000.0,
            wageType = "per_day",
            location = "Kuta, Bali",
            category = "cleaning",
            shiftDate = LocalDate.now(),
            startTime = "08:00",
            endTime = "17:00",
            workerCount = 1,
            isUrgent = false
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should fail when wage is negative", result.isFailure)
    }

    @Test
    fun `succeeds when wage is very small but positive`() = runTest {
        // Arrange
        val request = CreateJobRequest(
            title = "Test Job",
            description = "Test",
            wage = 0.01, // Minimum positive wage
            wageType = "per_hour",
            location = "Kuta, Bali",
            category = "cleaning",
            shiftDate = LocalDate.now(),
            startTime = "08:00",
            endTime = "09:00",
            workerCount = 1,
            isUrgent = false
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should succeed with small positive wage", result.isSuccess)
    }

    @Test
    fun `succeeds when wage is very large`() = runTest {
        // Arrange
        val request = CreateJobRequest(
            title = "Test Job",
            description = "Test",
            wage = 10000000.0, // Very large wage
            wageType = "per_shift",
            location = "Kuta, Bali",
            category = "cleaning",
            shiftDate = LocalDate.now(),
            startTime = "08:00",
            endTime = "18:00",
            workerCount = 5,
            isUrgent = true
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should succeed with large wage", result.isSuccess)
    }

    // ==================== WORKER COUNT VALIDATION TESTS ====================

    @Test
    fun `fails when worker count is zero`() = runTest {
        // Arrange - This should fail at CreateJobRequest init, not use case
        try {
            val request = CreateJobRequest(
                title = "Test Job",
                description = "Test",
                wage = 100000.0,
                wageType = "per_day",
                location = "Kuta, Bali",
                category = "cleaning",
                shiftDate = LocalDate.now(),
                startTime = "08:00",
                endTime = "17:00",
                workerCount = 0, // Invalid - should throw at init
                isUrgent = false
            )

            // Act
            val result = useCase(request)

            // Assert - Should not reach here
            fail("Should have thrown IllegalArgumentException at CreateJobRequest init")
        } catch (e: IllegalArgumentException) {
            // Expected exception at init
            assertTrue("Exception should mention worker count", e.message?.contains("Worker count", ignoreCase = true) ?: false)
        }
    }

    @Test
    fun `fails when worker count is negative`() = runTest {
        // Arrange
        try {
            val request = CreateJobRequest(
                title = "Test Job",
                description = "Test",
                wage = 100000.0,
                wageType = "per_day",
                location = "Kuta, Bali",
                category = "cleaning",
                shiftDate = LocalDate.now(),
                startTime = "08:00",
                endTime = "17:00",
                workerCount = -1, // Invalid
                isUrgent = false
            )

            // Act
            val result = useCase(request)

            // Assert
            fail("Should have thrown IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue("Exception should mention worker count", e.message?.contains("Worker count", ignoreCase = true) ?: false)
        }
    }

    @Test
    fun `fails when worker count exceeds maximum`() = runTest {
        // Arrange
        try {
            val request = CreateJobRequest(
                title = "Test Job",
                description = "Test",
                wage = 100000.0,
                wageType = "per_day",
                location = "Kuta, Bali",
                category = "cleaning",
                shiftDate = LocalDate.now(),
                startTime = "08:00",
                endTime = "17:00",
                workerCount = 11, // Invalid - max is 10
                isUrgent = false
            )

            // Act
            val result = useCase(request)

            // Assert
            fail("Should have thrown IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue("Exception should mention worker count", e.message?.contains("Worker count", ignoreCase = true) ?: false)
        }
    }

    @Test
    fun `succeeds with minimum worker count of 1`() = runTest {
        // Arrange
        val request = CreateJobRequest(
            title = "Test Job",
            description = "Test",
            wage = 100000.0,
            wageType = "per_day",
            location = "Kuta, Bali",
            category = "cleaning",
            shiftDate = LocalDate.now(),
            startTime = "08:00",
            endTime = "17:00",
            workerCount = 1, // Minimum valid
            isUrgent = false
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should succeed with minimum worker count", result.isSuccess)
    }

    @Test
    fun `succeeds with maximum worker count of 10`() = runTest {
        // Arrange
        val request = CreateJobRequest(
            title = "Test Job",
            description = "Test",
            wage = 100000.0,
            wageType = "per_day",
            location = "Kuta, Bali",
            category = "cleaning",
            shiftDate = LocalDate.now(),
            startTime = "08:00",
            endTime = "17:00",
            workerCount = 10, // Maximum valid
            isUrgent = false
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should succeed with maximum worker count", result.isSuccess)
    }

    // ==================== TIME VALIDATION TESTS ====================

    @Test
    fun `fails when end time is before start time`() = runTest {
        // Arrange
        val request = CreateJobRequest(
            title = "Test Job",
            description = "Test",
            wage = 100000.0,
            wageType = "per_day",
            location = "Kuta, Bali",
            category = "cleaning",
            shiftDate = LocalDate.now(),
            startTime = "17:00",
            endTime = "08:00", // End before start
            workerCount = 1,
            isUrgent = false
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should fail when end time is before start time", result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull("Exception should not be null", exception)
        assertTrue("Exception message should mention time", exception?.message?.contains("time", ignoreCase = true) ?: false)
    }

    @Test
    fun `fails when end time equals start time`() = runTest {
        // Arrange
        val request = CreateJobRequest(
            title = "Test Job",
            description = "Test",
            wage = 100000.0,
            wageType = "per_day",
            location = "Kuta, Bali",
            category = "cleaning",
            shiftDate = LocalDate.now(),
            startTime = "08:00",
            endTime = "08:00", // Same as start
            workerCount = 1,
            isUrgent = false
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should fail when end time equals start time", result.isFailure)
    }

    @Test
    fun `succeeds when end time is after start time`() = runTest {
        // Arrange
        val request = CreateJobRequest(
            title = "Test Job",
            description = "Test",
            wage = 100000.0,
            wageType = "per_day",
            location = "Kuta, Bali",
            category = "cleaning",
            shiftDate = LocalDate.now(),
            startTime = "08:00",
            endTime = "08:01", // Just 1 minute later
            workerCount = 1,
            isUrgent = false
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should succeed when end time is after start time", result.isSuccess)
    }

    // ==================== DATE VALIDATION TESTS ====================

    @Test
    fun `fails when shift date is in the past`() = runTest {
        // Arrange - Shift date yesterday
        val pastDate = LocalDate.now().minusDays(1)
        val request = CreateJobRequest(
            title = "Test Job",
            description = "Test",
            wage = 100000.0,
            wageType = "per_day",
            location = "Kuta, Bali",
            category = "cleaning",
            shiftDate = pastDate, // Yesterday
            startTime = "08:00",
            endTime = "17:00",
            workerCount = 1,
            isUrgent = false
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should fail when shift date is in the past", result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull("Exception should not be null", exception)
        assertTrue("Exception message should mention past or date", exception?.message?.contains("past", ignoreCase = true) ?: exception?.message?.contains("date", ignoreCase = true) ?: false)
    }

    @Test
    fun `fails when shift date is far in the past`() = runTest {
        // Arrange - Shift date one year ago
        val pastDate = LocalDate.now().minusYears(1)
        val request = CreateJobRequest(
            title = "Test Job",
            description = "Test",
            wage = 100000.0,
            wageType = "per_day",
            location = "Kuta, Bali",
            category = "cleaning",
            shiftDate = pastDate,
            startTime = "08:00",
            endTime = "17:00",
            workerCount = 1,
            isUrgent = false
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should fail when shift date is far in the past", result.isFailure)
    }

    // ==================== REPOSITORY ERROR TESTS ====================

    @Test
    fun `fails when repository create job fails`() = runTest {
        // Arrange
        mockJobRepository.shouldFailCreateJob = true
        val request = CreateJobRequest(
            title = "Test Job",
            description = "Test",
            wage = 100000.0,
            wageType = "per_day",
            location = "Kuta, Bali",
            category = "cleaning",
            shiftDate = LocalDate.now(),
            startTime = "08:00",
            endTime = "17:00",
            workerCount = 1,
            isUrgent = false
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should fail when repository fails", result.isFailure)
        assertNotNull("Exception should not be null", result.exceptionOrNull())
    }

    // ==================== WAGE TYPE TESTS ====================

    @Test
    fun `fails when wage type is invalid at request init`() = runTest {
        // Arrange - This should fail at CreateJobRequest init, not use case
        try {
            val request = CreateJobRequest(
                title = "Test Job",
                description = "Test",
                wage = 100000.0,
                wageType = "invalid_type", // Invalid wage type
                location = "Kuta, Bali",
                category = "cleaning",
                shiftDate = LocalDate.now(),
                startTime = "08:00",
                endTime = "17:00",
                workerCount = 1,
                isUrgent = false
            )

            // Act
            val result = useCase(request)

            // Assert - Should not reach here
            fail("Should have thrown IllegalArgumentException at CreateJobRequest init")
        } catch (e: IllegalArgumentException) {
            // Expected exception at init
            assertTrue("Exception should mention wage type", e.message?.contains("wage type", ignoreCase = true) ?: false)
        }
    }

    @Test
    fun `succeeds with per_shift wage type`() = runTest {
        // Arrange
        val request = CreateJobRequest(
            title = "Test Job",
            description = "Test",
            wage = 150000.0,
            wageType = "per_shift",
            location = "Kuta, Bali",
            category = "cleaning",
            shiftDate = LocalDate.now(),
            startTime = "08:00",
            endTime = "18:00",
            workerCount = 1,
            isUrgent = false
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should succeed with per_shift wage type", result.isSuccess)
    }

    @Test
    fun `succeeds with per_hour wage type`() = runTest {
        // Arrange
        val request = CreateJobRequest(
            title = "Test Job",
            description = "Test",
            wage = 25000.0,
            wageType = "per_hour",
            location = "Kuta, Bali",
            category = "cleaning",
            shiftDate = LocalDate.now(),
            startTime = "09:00",
            endTime = "14:00",
            workerCount = 1,
            isUrgent = false
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should succeed with per_hour wage type", result.isSuccess)
    }

    @Test
    fun `succeeds with per_day wage type`() = runTest {
        // Arrange
        val request = CreateJobRequest(
            title = "Test Job",
            description = "Test",
            wage = 150000.0,
            wageType = "per_day",
            location = "Kuta, Bali",
            category = "cleaning",
            shiftDate = LocalDate.now(),
            startTime = "08:00",
            endTime = "17:00",
            workerCount = 2,
            isUrgent = false
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Should succeed with per_day wage type", result.isSuccess)
    }

    // ==================== FAKE REPOSITORY ====================

    /**
     * Fake JobRepository for testing CreateJobUseCase
     *
     * Provides predictable behavior without real network calls.
     * Simulates job creation with optional failures.
     */
    private class FakeJobRepository : JobRepository {

        var shouldFailCreateJob: Boolean = false
        var createdJob: Job? = null

        override suspend fun createJob(request: CreateJobRequest): Result<Job> {
            if (shouldFailCreateJob) {
                return Result.failure(Exception("Failed to create job"))
            }

            val job = Job(
                id = "job-${System.currentTimeMillis()}",
                businessId = "business-1",
                title = request.title,
                description = request.description,
                wage = request.wage,
                wageType = request.wageType,
                location = request.location,
                category = request.category,
                status = "open",
                createdAt = LocalDateTime.now().toString(),
                updatedAt = LocalDateTime.now().toString(),
                startTime = request.startTime,
                endTime = request.endTime,
                shiftDate = request.shiftDate.toString(),
                isUrgent = request.isUrgent,
                isCompliant = null,
                workerCount = request.workerCount,
                businessName = "Test Business",
                businessLatitude = null,
                businessLongitude = null
            )

            createdJob = job
            return Result.success(job)
        }

        // Not implemented for CreateJobUseCase tests
        override suspend fun getWorkerProfile(): Result<com.example.dwhubfix.domain.model.UserProfile> =
            Result.failure(NotImplementedError("Not implemented"))

        override suspend fun getWorkerHistory(): Result<List<com.example.dwhubfix.domain.model.JobApplication>> =
            Result.failure(NotImplementedError("Not implemented"))

        override suspend fun getWorkerStats(): Result<com.example.dwhubfix.domain.model.WorkerStats?> =
            Result.failure(NotImplementedError("Not implemented"))

        override suspend fun getAvailableJobs(): Result<List<Job>> =
            Result.failure(NotImplementedError("Not implemented"))

        override suspend fun acceptJob(jobId: String): Result<kotlin.Unit> =
            Result.failure(NotImplementedError("Not implemented"))

        override suspend fun getJobById(jobId: String): Result<Job> =
            Result.failure(NotImplementedError("Not implemented"))

        override suspend fun getApplicationById(applicationId: String): Result<com.example.dwhubfix.domain.model.JobApplication> =
            Result.failure(NotImplementedError("Not implemented"))

        override suspend fun deleteJob(jobId: String): Result<kotlin.Unit> =
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
