package com.example.dwhubfix.data.repository.integration

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * Auth Repository Integration Tests
 *
 * Tests authentication operations against real Supabase backend.
 * Uses Supabase-KT 3.0.0 API.
 */
class AuthRepositoryIntegrationTest : BaseIntegrationTest() {

    // ==================== LOGIN TESTS ====================

    @Test
    fun `login with valid credentials returns success with user ID`() = runTest {
        // Act
        val userId = authenticateAsWorker()

        // Assert
        assertNotNull("User ID should not be null", userId)
        assertTrue("User ID should not be empty", userId.isNotEmpty())
        assertAuthenticated()
    }

    @Test
    fun `login with invalid password returns failure`() = runTest {
        // Act & Assert
        val exception = kotlin.runCatching {
            client.auth.signInWith(Email) {
                email = config.workerEmail
                password = "WrongPassword123!"
            }
        }.exceptionOrNull()

        // Assert
        assertNotNull("Login with wrong password should throw exception", exception)
    }

    @Test
    fun `login with non-existent email returns failure`() = runTest {
        // Act & Assert
        val exception = kotlin.runCatching {
            client.auth.signInWith(Email) {
                email = "nonexistent@example.com"
                password = config.workerPassword
            }
        }.exceptionOrNull()

        // Assert
        assertNotNull("Login with non-existent email should throw exception", exception)
    }

    @Test
    fun `login with empty email returns failure`() = runTest {
        // Act & Assert
        val exception = kotlin.runCatching {
            client.auth.signInWith(Email) {
                email = ""
                password = config.workerPassword
            }
        }.exceptionOrNull()

        // Assert
        assertNotNull("Login with empty email should throw exception", exception)
    }

    @Test
    fun `login with empty password returns failure`() = runTest {
        // Act & Assert
        val exception = kotlin.runCatching {
            client.auth.signInWith(Email) {
                email = config.workerEmail
                password = ""
            }
        }.exceptionOrNull()

        // Assert
        assertNotNull("Login with empty password should throw exception", exception)
    }

    // ==================== LOGOUT TESTS ====================

    @Test
    fun `logout clears session from Supabase`() = runTest {
        // Arrange
        authenticateAsWorker()
        assertAuthenticated()

        // Act
        client.auth.signOut()

        // Assert
        assertNotAuthenticated()
    }

    // ==================== REGISTRATION TESTS ====================

    @Test
    fun `register new worker creates user`() = runTest {
        // Act
        val userId = registerWorker()

        // Assert
        assertNotNull("User ID should not be null", userId)
        assertTrue("User ID should not be empty", userId.isNotEmpty())
    }

    @Test
    fun `register new business creates user`() = runTest {
        // Act
        val userId = registerBusiness()

        // Assert
        assertNotNull("User ID should not be null", userId)
        assertTrue("User ID should not be empty", userId.isNotEmpty())
    }

    @Test
    fun `register with duplicate email returns failure`() = runTest {
        // Arrange
        val testEmail = "test-worker-${testId}@example.com"
        registerWorker(testEmail, "TestPassword123!")

        // Act & Assert
        val exception = kotlin.runCatching {
            client.auth.signUpWith(Email) {
                email = testEmail
                password = "TestPassword123!"
            }
        }.exceptionOrNull()

        // Assert
        assertNotNull("Registration with duplicate email should throw exception", exception)
    }

    // ==================== MULTI-ROLE TESTS ====================

    @Test
    fun `worker and business can both authenticate`() = runTest {
        // Act - Authenticate as worker
        val workerId = authenticateAsWorker()
        assertNotNull("Worker ID should not be null", workerId)

        // Logout
        client.auth.signOut()
        assertNotAuthenticated()

        // Act - Authenticate as business
        val businessId = authenticateAsBusiness()
        assertNotNull("Business ID should not be null", businessId)
        assertAuthenticated()
    }

    // ==================== SESSION TESTS ====================

    @Test
    fun `session persists after login`() = runTest {
        // Arrange
        authenticateAsWorker()

        // Act
        val sessionInfo = client.auth.sessionManager.loadSession()

        // Assert
        assertNotNull("Session should persist after login", sessionInfo)
        assertEquals("User ID should match", currentUserId, sessionInfo?.user?.id?.toString())
    }

    @Test
    fun `get access token after login returns token`() = runTest {
        // Arrange
        authenticateAsWorker()

        // Act
        val sessionInfo = client.auth.sessionManager.loadSession()
        val token = sessionInfo?.accessToken

        // Assert
        assertNotNull("Access token should not be null", token)
        assertTrue("Access token should not be empty", token!!.isNotEmpty())
    }

    @Test
    fun `get user ID after login returns user ID`() = runTest {
        // Arrange
        val userId = authenticateAsWorker()

        // Act
        val sessionInfo = client.auth.sessionManager.loadSession()
        val currentId = sessionInfo?.user?.id?.toString()

        // Assert
        assertNotNull("User ID should be available", currentId)
        assertEquals("User ID should match", userId, currentId)
    }

    @Test
    fun `session persists across operations`() = runTest {
        // Arrange
        authenticateAsWorker()
        val initialSession = client.auth.sessionManager.loadSession()
        val initialToken = initialSession?.accessToken

        // Act - Perform some operation
        val userId = client.auth.sessionManager.loadSession()?.user?.id?.toString()

        // Assert
        assertNotNull("Session should still be valid", client.auth.sessionManager.loadSession())
        assertEquals("User ID should remain the same", currentUserId, userId)
        assertEquals("Token should remain the same", initialToken, client.auth.sessionManager.loadSession()?.accessToken)
    }

    @Test
    fun `session cleared after logout`() = runTest {
        // Arrange
        authenticateAsWorker()
        assertAuthenticated()

        // Act
        client.auth.signOut()

        // Assert
        val sessionInfo = client.auth.sessionManager.loadSession()
        assertNull("Session should be cleared after logout", sessionInfo)
    }
}
