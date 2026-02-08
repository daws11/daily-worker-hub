package com.example.dwhubfix.data.repository.integration

import com.example.dwhubfix.data.*
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * Auth Repository Integration Tests
 *
 * Tests authentication operations against real Supabase backend.
 * Requires test-config.properties with valid Supabase credentials.
 *
 * Prerequisites:
 * 1. Configure test-config.properties with your Supabase dev/staging credentials
 * 2. Create test users in your Supabase project:
 *    - Worker: integration-test-worker@example.com / TestWorker123!
 *    - Business: integration-test-business@example.com / TestBusiness123!
 */
class AuthRepositoryIntegrationTest : BaseIntegrationTest() {

    // ==================== LOGIN TESTS ====================

    @Test
    fun login_with_valid_credentials_returns_success() = runTest {
        // Act
        val userId = authenticateAsWorker()

        // Assert
        assertNotNull("User ID should not be null", userId)
        assertTrue("User ID should not be empty", userId.isNotEmpty())
        assertAuthenticated()
    }

    @Test
    fun login_with_invalid_password_returns_failure() = runTest {
        // Act & Assert
        val exception = kotlin.runCatching {
            client.auth.signInWith(Email) {
                email = config.workerEmail
                password = "WrongPassword123!"
            }
        }.exceptionOrNull()

        // Assert
        assertNotNull("Login with wrong password should throw exception", exception)
        assertNotAuthenticated()
    }

    @Test
    fun login_with_nonexistent_email_returns_failure() = runTest {
        // Act & Assert
        val exception = kotlin.runCatching {
            client.auth.signInWith(Email) {
                email = "nonexistent-${testId}@example.com"
                password = "SomePassword123!"
            }
        }.exceptionOrNull()

        // Assert
        assertNotNull("Login with non-existent email should throw exception", exception)
        assertNotAuthenticated()
    }

    @Test
    fun login_sets_access_token_in_shared_prefs() = runTest {
        // Act
        authenticateAsWorker()

        // Assert
        val token = getAccessToken()
        assertNotNull("Access token should be set after login", token)
        assertTrue("Access token should not be empty", token?.isNotEmpty() ?: false)
    }

    @Test
    fun login_sets_user_id_in_shared_prefs() = runTest {
        // Act
        val userId = authenticateAsWorker()

        // Assert
        val storedUserId = getStoredUserId()
        assertEquals("Stored user ID should match authenticated user ID", userId, storedUserId)
    }

    // ==================== REGISTRATION TESTS ====================

    @Test
    fun register_new_worker_creates_user() = runTest {
        // Arrange - Unique email for this test
        val email = "new-worker-$testId@example.com"
        val password = "NewWorker123!"

        // Act - Register
        val userId = registerWorker(email, password)

        // Assert
        assertNotNull("User ID should not be null after registration", userId)

        // Verify can login
        logoutIfNeeded()
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        val sessionInfo = client.auth.sessionManager.loadSession()
        assertNotNull("Should be able to login after registration", sessionInfo?.user?.id)
    }

    @Test
    fun register_with_duplicate_email_returns_failure() = runTest {
        // Arrange - Register first user
        val email = "duplicate-$testId@example.com"
        registerWorker(email, "FirstPassword123!")
        logoutIfNeeded()

        // Act & Assert - Try to register with same email
        val exception = kotlin.runCatching {
            registerWorker(email, "SecondPassword123!")
        }.exceptionOrNull()

        assertNotNull("Registering with duplicate email should fail", exception)
    }

    @Test
    fun register_new_business_creates_user() = runTest {
        // Arrange - Unique email for this test
        val email = "new-business-$testId@example.com"
        val password = "NewBusiness123!"

        // Act
        val userId = registerBusiness(email, password)

        // Assert
        assertNotNull("Business user ID should not be null after registration", userId)
        assertAuthenticated()
    }

    // ==================== LOGOUT TESTS ====================

    @Test
    fun logout_clears_session_from_supabase() = runTest {
        // Arrange - Login first
        authenticateAsWorker()
        assertAuthenticated()

        // Act
        client.auth.signOut()
        clearSessionFromPreferences(testPrefsProvider.getSharedPreferences())
        currentUserId = null

        // Assert
        val session = client.auth.sessionManager.loadSession()
        assertNull("Supabase session should be cleared after logout", session)
        assertNotAuthenticated()
    }

    @Test
    fun logout_clears_access_token_from_shared_prefs() = runTest {
        // Arrange - Login first
        authenticateAsWorker()
        assertNotNull("Access token should be set before logout", getAccessToken())

        // Act
        client.auth.signOut()
        clearSessionFromPreferences(testPrefsProvider.getSharedPreferences())
        currentUserId = null

        // Assert
        assertNull("Access token should be cleared after logout", getAccessToken())
    }

    @Test
    fun logout_clears_user_id_from_shared_prefs() = runTest {
        // Arrange - Login first
        val userId = authenticateAsWorker()
        assertNotNull("User ID should be set before logout", getStoredUserId())

        // Act
        client.auth.signOut()
        clearSessionFromPreferences(testPrefsProvider.getSharedPreferences())
        currentUserId = null

        // Assert
        assertNull("User ID should be cleared after logout", getStoredUserId())
    }

    // ==================== SESSION PERSISTENCE TESTS ====================

    @Test
    fun session_persists_access_token_across_operations() = runTest {
        // Arrange - Login
        authenticateAsWorker()
        val initialToken = getAccessToken()

        // Act - Perform some operations (should not clear token)
        val session = client.auth.sessionManager.loadSession()

        // Assert
        assertEquals("Access token should persist", initialToken, session?.accessToken)
    }

    @Test
    fun get_access_token_after_login_returns_token() = runTest {
        // Act
        authenticateAsWorker()

        // Assert
        val token = getAccessToken()
        assertNotNull("getAccessToken should return token after login", token)
        assertTrue("Token should not be empty", token!!.isNotEmpty())
    }

    @Test
    fun get_user_id_after_login_returns_user_id() = runTest {
        // Act
        val expectedUserId = authenticateAsWorker()

        // Assert
        val userId = getStoredUserId()
        assertEquals("getUserId should return correct user ID after login", expectedUserId, userId)
    }

    // ==================== CROSS-USER TESTS ====================

    @Test
    fun worker_and_business_can_both_authenticate() = runTest {
        // Act - Authenticate as worker
        val workerId = authenticateAsWorker()
        assertNotNull("Worker should authenticate successfully", workerId)

        // Logout
        client.auth.signOut()
        clearSessionFromPreferences(testPrefsProvider.getSharedPreferences())
        currentUserId = null

        // Act - Authenticate as business
        val businessId = authenticateAsBusiness()
        assertNotNull("Business should authenticate successfully", businessId)

        // Assert - Different IDs
        assertNotEquals("Worker and Business should have different user IDs", workerId, businessId)
    }
}
