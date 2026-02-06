package com.example.dwhubfix.domain.usecase

import com.example.dwhubfix.domain.repository.AuthRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit Tests for LoginUseCase
 *
 * Tests the business logic for user authentication, including:
 * - Login with valid credentials
 * - Login with invalid credentials
 * - Logout functionality
 * - Session state checks (isLoggedIn, getCurrentUserId)
 */
class LoginUseCaseTest {

    private lateinit var useCase: LoginUseCase
    private lateinit var mockAuthRepository: FakeAuthRepository

    @Before
    fun setup() {
        // Using a fake repository instead of mock for cleaner tests
        mockAuthRepository = FakeAuthRepository()
        useCase = LoginUseCase(mockAuthRepository)
    }

    // ==================== LOGIN TESTS ====================

    @Test
    fun `login with valid credentials returns success with user ID`() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = "correctPassword"

        // Act
        val result = useCase(email, password)

        // Assert
        assertTrue("Login should succeed with valid credentials", result.isSuccess)
        assertEquals("user123", result.getOrNull())
    }

    @Test
    fun `login with invalid password returns failure`() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = "wrongPassword"

        // Act
        val result = useCase(email, password)

        // Assert
        assertTrue("Login should fail with invalid password", result.isFailure)
        assertNotNull("Exception should be present", result.exceptionOrNull())
    }

    @Test
    fun `login with non-existent email returns failure`() = runTest {
        // Arrange
        val email = "nonexistent@example.com"
        val password = "password"

        // Act
        val result = useCase(email, password)

        // Assert
        assertTrue("Login should fail with non-existent email", result.isFailure)
        assertNotNull("Exception should be present", result.exceptionOrNull())
    }

    @Test
    fun `login with empty email returns failure`() = runTest {
        // Arrange
        val email = ""
        val password = "password"

        // Act
        val result = useCase(email, password)

        // Assert
        assertTrue("Login should fail with empty email", result.isFailure)
    }

    @Test
    fun `login with empty password returns failure`() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = ""

        // Act
        val result = useCase(email, password)

        // Assert
        assertTrue("Login should fail with empty password", result.isFailure)
    }

    @Test
    fun `login with null password returns failure`() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = "" // Treat as empty

        // Act
        val result = useCase(email, password)

        // Assert
        assertTrue("Login should fail with null password", result.isFailure)
    }

    @Test
    fun `login sets access token in repository`() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = "correctPassword"

        // Act
        useCase(email, password)

        // Assert
        assertNotNull("Access token should be set after login", mockAuthRepository.getAccessToken())
        assertEquals("Access token should match", "test-token-user123", mockAuthRepository.getAccessToken())
    }

    @Test
    fun `login sets user ID in repository`() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = "correctPassword"

        // Act
        useCase(email, password)

        // Assert
        assertNotNull("User ID should be set after login", mockAuthRepository.getUserId())
        assertEquals("User ID should match", "user123", mockAuthRepository.getUserId())
    }

    // ==================== LOGOUT TESTS ====================

    @Test
    fun `logout returns success`() = runTest {
        // Arrange - Login first
        useCase("test@example.com", "correctPassword")

        // Act
        val result = useCase.logout()

        // Assert
        assertTrue("Logout should succeed", result.isSuccess)
    }

    @Test
    fun `logout clears access token`() = runTest {
        // Arrange - Login first
        useCase("test@example.com", "correctPassword")
        assertNotNull("Access token should be set before logout", mockAuthRepository.getAccessToken())

        // Act
        useCase.logout()

        // Assert
        assertNull("Access token should be cleared after logout", mockAuthRepository.getAccessToken())
    }

    @Test
    fun `logout clears user ID`() = runTest {
        // Arrange - Login first
        useCase("test@example.com", "correctPassword")
        assertNotNull("User ID should be set before logout", mockAuthRepository.getUserId())

        // Act
        useCase.logout()

        // Assert
        assertNull("User ID should be cleared after logout", mockAuthRepository.getUserId())
    }

    // ==================== SESSION STATE TESTS ====================

    @Test
    fun `isLoggedIn returns true after successful login`() = runTest {
        // Arrange
        assertFalse("User should not be logged in initially", useCase.isLoggedIn())

        // Act
        useCase("test@example.com", "correctPassword")

        // Assert
        assertTrue("isLoggedIn should return true after login", useCase.isLoggedIn())
    }

    @Test
    fun `isLoggedIn returns false initially`() {
        // Act & Assert
        assertFalse("isLoggedIn should return false when not logged in", useCase.isLoggedIn())
    }

    @Test
    fun `isLoggedIn returns false after logout`() = runTest {
        // Arrange - Login first
        useCase("test@example.com", "correctPassword")
        assertTrue("User should be logged in before logout", useCase.isLoggedIn())

        // Act
        useCase.logout()

        // Assert
        assertFalse("isLoggedIn should return false after logout", useCase.isLoggedIn())
    }

    @Test
    fun `getCurrentUserId returns null initially`() {
        // Act & Assert
        assertNull("getCurrentUserId should return null when not logged in", useCase.getCurrentUserId())
    }

    @Test
    fun `getCurrentUserId returns user ID after login`() = runTest {
        // Arrange
        useCase("test@example.com", "correctPassword")

        // Act
        val userId = useCase.getCurrentUserId()

        // Assert
        assertNotNull("getCurrentUserId should return user ID after login", userId)
        assertEquals("User ID should match", "user123", userId)
    }

    @Test
    fun `getCurrentUserId returns null after logout`() = runTest {
        // Arrange - Login first
        useCase("test@example.com", "correctPassword")
        assertNotNull("User ID should be set before logout", useCase.getCurrentUserId())

        // Act
        useCase.logout()
        val userId = useCase.getCurrentUserId()

        // Assert
        assertNull("getCurrentUserId should return null after logout", userId)
    }

    // ==================== EDGE CASES ====================

    @Test
    fun `multiple logins override previous session`() = runTest {
        // Arrange - First login
        useCase("test@example.com", "correctPassword")
        val firstUserId = useCase.getCurrentUserId()

        // Act - Second login with different user
        useCase("another@example.com", "anotherPassword")
        val secondUserId = useCase.getCurrentUserId()

        // Assert
        assertEquals("First user ID", "user123", firstUserId)
        assertEquals("Second user ID should override", "user456", secondUserId)
    }

    @Test
    fun `login case sensitivity - email`() = runTest {
        // Arrange
        val emailUpperCase = "TEST@EXAMPLE.COM"

        // Act
        val result = useCase(emailUpperCase, "correctPassword")

        // Assert - Should fail because we're case-sensitive in this fake implementation
        assertTrue("Login should fail with uppercase email in this implementation", result.isFailure)
    }

    // ==================== FAKE REPOSITORY ====================

    /**
     * Fake AuthRepository for testing
     *
     * Provides predictable behavior without real network calls.
     * Simulates success/failure scenarios based on input.
     */
    private class FakeAuthRepository : AuthRepository {
        private var accessToken: String? = null
        private var currentUserId: String? = null

        // Simulated user database
        private val users = mapOf(
            "test@example.com" to UserCredential("user123", "correctPassword"),
            "another@example.com" to UserCredential("user456", "anotherPassword")
        )

        override suspend fun login(email: String, password: String): Result<String> {
            // Validate inputs
            if (email.isBlank()) {
                return Result.failure(IllegalArgumentException("Email cannot be blank"))
            }
            if (password.isBlank()) {
                return Result.failure(IllegalArgumentException("Password cannot be blank"))
            }

            // Check credentials
            val user = users[email]
            return if (user != null && user.password == password) {
                accessToken = "test-token-${user.id}"
                currentUserId = user.id
                Result.success(user.id)
            } else {
                Result.failure(Exception("Invalid credentials"))
            }
        }

        override suspend fun registerWorker(
            fullName: String,
            email: String,
            password: String,
            phoneNumber: String
        ): Result<String> {
            // Not implemented for LoginUseCase tests
            return Result.failure(NotImplementedError("registerWorker not implemented in fake"))
        }

        override suspend fun registerBusiness(
            businessName: String,
            email: String,
            password: String,
            phoneNumber: String
        ): Result<String> {
            // Not implemented for LoginUseCase tests
            return Result.failure(NotImplementedError("registerBusiness not implemented in fake"))
        }

        override suspend fun logout(): Result<Unit> {
            accessToken = null
            currentUserId = null
            return Result.success(Unit)
        }

        override fun getAccessToken(): String? = accessToken

        override fun getUserId(): String? = currentUserId
    }

    /**
     * Helper data class for user credentials
     */
    private data class UserCredential(
        val id: String,
        val password: String
    )
}
