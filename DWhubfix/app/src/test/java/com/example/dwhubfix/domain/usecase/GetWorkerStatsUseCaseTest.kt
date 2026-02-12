package com.example.dwhubfix.domain.usecase

import com.example.dwhubfix.domain.model.WorkerStats
import com.example.dwhubfix.domain.repository.JobRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit Tests for GetWorkerStatsUseCase
 *
 * Tests retrieving worker statistics, including:
 * - Success scenarios
 * - Null stats handling (returns default WorkerStats)
 * - Error handling
 * - Edge cases
 */
class GetWorkerStatsUseCaseTest {

    private lateinit var useCase: GetWorkerStatsUseCase
    private lateinit var mockJobRepository: FakeJobRepository

    @Before
    fun setup() {
        mockJobRepository = FakeJobRepository()
        useCase = GetWorkerStatsUseCase(mockJobRepository)
    }

    // ==================== SUCCESS TESTS ====================

    @Test
    fun `returns worker stats successfully`() = runTest {
        // Arrange
        val stats = WorkerStats(
            totalShiftsCompleted = 25,
            totalEarnings = 5000000L,
            walletBalance = 2000000L,
            frozenAmount = 500000L,
            ratingAvg = 4.5,
            ratingCount = 20,
            reliabilityScore = 95.0,
            tier = "silver"
        )
        mockJobRepository.workerStats = stats

        // Act
        val result = useCase()

        // Assert
        assertTrue("Should return success", result.isSuccess)
        val returnedStats = result.getOrNull()
        assertNotNull("Stats should not be null", returnedStats)
        assertEquals("Total shifts should match", 25, returnedStats?.totalShiftsCompleted)
        assertEquals("Total earnings should match", 5000000L, returnedStats?.totalEarnings)
        assertEquals("Wallet balance should match", 2000000L, returnedStats?.walletBalance)
        assertEquals("Frozen amount should match", 500000L, returnedStats?.frozenAmount)
        assertEquals("Rating average should match", 4.5, returnedStats?.ratingAvg ?: 0.0, 0.01)
        assertEquals("Rating count should match", 20, returnedStats?.ratingCount)
        assertEquals("Reliability score should match", 95.0, returnedStats?.reliabilityScore ?: 0.0, 0.01)
        assertEquals("Tier should match", "silver", returnedStats?.tier)
    }

    @Test
    fun `returns worker stats with all zero values`() = runTest {
        // Arrange
        val stats = WorkerStats()
        mockJobRepository.workerStats = stats

        // Act
        val result = useCase()

        // Assert
        assertTrue("Should return success", result.isSuccess)
        val returnedStats = result.getOrNull()
        assertEquals("Total shifts should be 0", 0, returnedStats?.totalShiftsCompleted)
        assertEquals("Total earnings should be 0", 0L, returnedStats?.totalEarnings)
        assertEquals("Wallet balance should be 0", 0L, returnedStats?.walletBalance)
        assertEquals("Frozen amount should be 0", 0L, returnedStats?.frozenAmount)
        assertEquals("Rating average should be 0.0", 0.0, returnedStats?.ratingAvg ?: 0.0, 0.01)
        assertEquals("Rating count should be 0", 0, returnedStats?.ratingCount)
        assertEquals("Reliability score should be 100.0", 100.0, returnedStats?.reliabilityScore ?: 0.0, 0.01)
        assertEquals("Tier should be bronze", "bronze", returnedStats?.tier)
    }

    // ==================== NULL STATS HANDLING TESTS ====================

    @Test
    fun `returns default stats when repository returns null`() = runTest {
        // Arrange
        mockJobRepository.workerStats = null

        // Act
        val result = useCase()

        // Assert
        assertTrue("Should return success with null stats", result.isSuccess)
        val returnedStats = result.getOrNull()
        assertNotNull("Stats should default to non-null", returnedStats)
        assertEquals("Default total shifts should be 0", 0, returnedStats?.totalShiftsCompleted)
        assertEquals("Default total earnings should be 0", 0L, returnedStats?.totalEarnings)
        assertEquals("Default wallet balance should be 0", 0L, returnedStats?.walletBalance)
        assertEquals("Default frozen amount should be 0", 0L, returnedStats?.frozenAmount)
        assertEquals("Default tier should be bronze", "bronze", returnedStats?.tier)
    }

    // ==================== ERROR TESTS ====================

    @Test
    fun `fails when repository returns failure`() = runTest {
        // Arrange
        mockJobRepository.shouldFailGetWorkerStats = true

        // Act
        val result = useCase()

        // Assert
        assertTrue("Should fail when repository fails", result.isFailure)
        assertNotNull("Exception should be present", result.exceptionOrNull())
    }

    @Test
    fun `exception message is propagated from repository`() = runTest {
        // Arrange
        mockJobRepository.shouldFailGetWorkerStats = true
        val expectedMessage = "Failed to fetch worker stats"

        // Act
        val result = useCase()

        // Assert
        assertTrue("Should fail", result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue("Exception message should contain error", exception?.message?.contains("Failed") ?: false)
    }

    // ==================== WORKER STATS DATA CLASS TESTS ====================

    @Test
    fun `worker stats handles all tier levels`() = runTest {
        // Arrange & Act - Test each tier level
        val bronzeStats = WorkerStats(tier = "bronze")
        val silverStats = WorkerStats(tier = "silver")
        val goldStats = WorkerStats(tier = "gold")
        val platinumStats = WorkerStats(tier = "platinum")

        // Assert
        assertEquals("Bronze tier should be preserved", "bronze", bronzeStats.tier)
        assertEquals("Silver tier should be preserved", "silver", silverStats.tier)
        assertEquals("Gold tier should be preserved", "gold", goldStats.tier)
        assertEquals("Platinum tier should be preserved", "platinum", platinumStats.tier)
    }

    @Test
    fun `worker stats calculates available balance correctly`() = runTest {
        // Arrange
        val stats = WorkerStats(
            walletBalance = 2000000L,
            frozenAmount = 500000L
        )

        // Act
        val availableBalance = stats.availableBalance

        // Assert
        assertEquals("Available balance should be wallet balance minus frozen amount", 1500000L, availableBalance)
    }

    @Test
    fun `worker stats available balance is zero when all frozen`() = runTest {
        // Arrange
        val stats = WorkerStats(
            walletBalance = 1000000L,
            frozenAmount = 1000000L
        )

        // Act
        val availableBalance = stats.availableBalance

        // Assert
        assertEquals("Available balance should be 0 when all frozen", 0L, availableBalance)
    }

    @Test
    fun `worker stats available balance equals wallet balance when nothing frozen`() = runTest {
        // Arrange
        val stats = WorkerStats(
            walletBalance = 3000000L,
            frozenAmount = 0L
        )

        // Act
        val availableBalance = stats.availableBalance

        // Assert
        assertEquals("Available balance should equal wallet balance when nothing frozen", 3000000L, availableBalance)
    }

    @Test
    fun `worker stats formats balance correctly`() = runTest {
        // Arrange
        val stats = WorkerStats(walletBalance = 1500000L)

        // Act
        val formattedBalance = stats.formattedBalance

        // Assert
        assertTrue("Formatted balance should contain 'Rp'", formattedBalance.contains("Rp"))
        assertTrue("Formatted balance should contain number", formattedBalance.contains("1,500,000"))
    }

    @Test
    fun `worker stats formats earnings correctly`() = runTest {
        // Arrange
        val stats = WorkerStats(totalEarnings = 10000000L)

        // Act
        val formattedEarnings = stats.formattedEarnings

        // Assert
        assertTrue("Formatted earnings should contain 'Rp'", formattedEarnings.contains("Rp"))
        assertTrue("Formatted earnings should contain number", formattedEarnings.contains("10,000,000"))
    }

    // ==================== EDGE CASES ====================

    @Test
    fun `worker stats handles maximum values`() = runTest {
        // Arrange
        val maxStats = WorkerStats(
            totalShiftsCompleted = Int.MAX_VALUE,
            totalEarnings = Long.MAX_VALUE,
            walletBalance = Long.MAX_VALUE,
            frozenAmount = Long.MAX_VALUE,
            ratingAvg = 5.0,
            ratingCount = Int.MAX_VALUE,
            reliabilityScore = 100.0,
            tier = "platinum"
        )
        mockJobRepository.workerStats = maxStats

        // Act
        val result = useCase()

        // Assert
        assertTrue("Should return success with max values", result.isSuccess)
        val returnedStats = result.getOrNull()
        assertEquals("Should handle max shifts", Int.MAX_VALUE, returnedStats?.totalShiftsCompleted)
        assertEquals("Should handle max earnings", Long.MAX_VALUE, returnedStats?.totalEarnings)
        assertEquals("Should handle max rating count", Int.MAX_VALUE, returnedStats?.ratingCount)
    }

    @Test
    fun `worker stats handles decimal precision for ratings`() = runTest {
        // Arrange
        val stats = WorkerStats(
            ratingAvg = 4.75,
            reliabilityScore = 98.5
        )
        mockJobRepository.workerStats = stats

        // Act
        val result = useCase()

        // Assert
        assertTrue("Should return success", result.isSuccess)
        val returnedStats = result.getOrNull()
        assertEquals("Should preserve rating decimal precision", 4.75, returnedStats?.ratingAvg ?: 0.0, 0.001)
        assertEquals("Should preserve reliability decimal precision", 98.5, returnedStats?.reliabilityScore ?: 0.0, 0.001)
    }

    @Test
    fun `handles multiple sequential calls consistently`() = runTest {
        // Arrange
        val stats = WorkerStats(totalShiftsCompleted = 10)
        mockJobRepository.workerStats = stats

        // Act
        val result1 = useCase()
        val result2 = useCase()
        val result3 = useCase()

        // Assert
        assertTrue("First call should succeed", result1.isSuccess)
        assertTrue("Second call should succeed", result2.isSuccess)
        assertTrue("Third call should succeed", result3.isSuccess)
        assertEquals("All calls should return same value", 10, result1.getOrNull()?.totalShiftsCompleted)
        assertEquals("All calls should return same value", 10, result2.getOrNull()?.totalShiftsCompleted)
        assertEquals("All calls should return same value", 10, result3.getOrNull()?.totalShiftsCompleted)
    }

    @Test
    fun `worker stats is a data class with correct equals behavior`() = runTest {
        // Arrange & Act
        val stats1 = WorkerStats(totalShiftsCompleted = 5)
        val stats2 = WorkerStats(totalShiftsCompleted = 5)
        val stats3 = WorkerStats(totalShiftsCompleted = 10)

        // Assert - Data class equality
        assertEquals("Identical stats should be equal", stats1, stats2)
        assertNotEquals("Different stats should not be equal", stats1, stats3)
    }

    @Test
    fun `worker stats data class has correct copy functionality`() = runTest {
        // Arrange
        val originalStats = WorkerStats(
            totalShiftsCompleted = 25,
            totalEarnings = 5000000L,
            walletBalance = 2000000L,
            frozenAmount = 500000L,
            ratingAvg = 4.5,
            ratingCount = 20,
            reliabilityScore = 95.0,
            tier = "silver"
        )

        // Act - Copy and modify one field
        val modifiedStats = originalStats.copy(tier = "gold")

        // Assert
        assertEquals("Original should be unchanged", "silver", originalStats.tier)
        assertEquals("Copy should have modified value", "gold", modifiedStats.tier)
        assertEquals("Copy should preserve other fields", 25, modifiedStats.totalShiftsCompleted)
        assertEquals("Copy should preserve other fields", 5000000L, modifiedStats.totalEarnings)
    }

    // ==================== HELPER FUNCTIONS ====================

    private fun createWorkerStats(
        totalShiftsCompleted: Int = 0,
        totalEarnings: Long = 0L,
        walletBalance: Long = 0L,
        frozenAmount: Long = 0L,
        ratingAvg: Double = 0.0,
        ratingCount: Int = 0,
        reliabilityScore: Double = 100.0,
        tier: String = "bronze"
    ): WorkerStats {
        return WorkerStats(
            totalShiftsCompleted = totalShiftsCompleted,
            totalEarnings = totalEarnings,
            walletBalance = walletBalance,
            frozenAmount = frozenAmount,
            ratingAvg = ratingAvg,
            ratingCount = ratingCount,
            reliabilityScore = reliabilityScore,
            tier = tier
        )
    }

    // ==================== FAKE REPOSITORY ====================

    /**
     * Fake JobRepository for testing GetWorkerStatsUseCase
     *
     * Provides predictable behavior without real network calls.
     */
    private class FakeJobRepository : JobRepository {

        var workerStats: WorkerStats? = WorkerStats()
        var shouldFailGetWorkerStats: Boolean = false

        override suspend fun getWorkerStats(): Result<WorkerStats?> {
            if (shouldFailGetWorkerStats) {
                return Result.failure(Exception("Failed to fetch worker stats"))
            }
            return Result.success(workerStats)
        }

        // Not implemented for GetWorkerStatsUseCase tests
        override suspend fun getWorkerProfile(): Result<com.example.dwhubfix.domain.model.UserProfile> =
            Result.failure(NotImplementedError("Not implemented"))

        override suspend fun getWorkerHistory(): Result<List<com.example.dwhubfix.domain.model.JobApplication>> =
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

        override suspend fun getJobDetails(jobId: String): Result<com.example.dwhubfix.domain.model.JobWithDetails> =
            Result.failure(NotImplementedError("Not implemented"))
    }
}
