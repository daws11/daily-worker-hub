package com.example.dwhubfix.data.repository.integration

import io.github.jan.supabase.SupabaseClient as SupabaseClientInstance
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Before
import org.junit.Assert.*

/**
 * Base Integration Test
 *
 * Provides common setup and teardown for all integration tests.
 * Uses Supabase-KT 3.0.0 API with sessionManager.loadSession()
 */
abstract class BaseIntegrationTest {

    protected lateinit var client: SupabaseClientInstance
        private set

    protected lateinit var config: TestDataManager.TestConfig
        private set

    protected lateinit var testId: String
        private set

    protected var currentUserId: String? = null

    @Before
    open fun setup() {
        config = TestDataManager.loadConfig()

        val validation = TestDataManager.validateTestUsers(config)
        if (validation is TestDataManager.ValidationResult.Invalid) {
            fail("Test user configuration invalid: ${validation.message}.")
        }

        client = TestDataManager.createTestClient(config)
        testId = TestDataManager.generateTestId()

        runBlocking { logoutIfNeeded() }
    }

    @After
    open fun tearDown() {
        runBlocking {
            try {
                if (::client.isInitialized && ::testId.isInitialized) {
                    TestDataManager.cleanupTestData(client, testId)
                }
            } catch (e: Exception) {
                println("Warning: Failed to clean up test data: ${e.message}")
            }

            try {
                if (::client.isInitialized) {
                    logoutIfNeeded()
                }
            } catch (e: Exception) {
                // Ignore errors during teardown
            }
        }
    }

    // ==================== HELPER METHODS ====================

    protected suspend fun authenticateAsWorker(): String = withContext(Dispatchers.IO) {
        client.auth.signInWith(Email) {
            email = config.workerEmail
            password = config.workerPassword
        }

        val sessionInfo = client.auth.sessionManager.loadSession()
        val userId = sessionInfo?.user?.id?.toString()
            ?: throw IllegalStateException("Failed to get user ID")

        currentUserId = userId
        userId
    }

    protected suspend fun authenticateAsBusiness(): String = withContext(Dispatchers.IO) {
        client.auth.signInWith(Email) {
            email = config.businessEmail
            password = config.businessPassword
        }

        val sessionInfo = client.auth.sessionManager.loadSession()
        val userId = sessionInfo?.user?.id?.toString()
            ?: throw IllegalStateException("Failed to get user ID")

        currentUserId = userId
        userId
    }

    protected suspend fun registerWorker(
        email: String = "test-worker-${testId}@example.com",
        password: String = "TestPassword123!"
    ): String = withContext(Dispatchers.IO) {
        client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }

        val sessionInfo = client.auth.sessionManager.loadSession()
        val userId = sessionInfo?.user?.id?.toString()
            ?: throw IllegalStateException("Failed to get user ID")

        currentUserId = userId
        userId
    }

    protected suspend fun registerBusiness(
        email: String = "test-business-${testId}@example.com",
        password: String = "TestPassword123!"
    ): String = withContext(Dispatchers.IO) {
        client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }

        val sessionInfo = client.auth.sessionManager.loadSession()
        val userId = sessionInfo?.user?.id?.toString()
            ?: throw IllegalStateException("Failed to get user ID")

        currentUserId = userId
        userId
    }

    protected fun assertAuthenticated() {
        runBlocking {
            val sessionInfo = client.auth.sessionManager.loadSession()
            assertTrue("User should be authenticated", sessionInfo != null)
            assertTrue("User ID should not be null", currentUserId != null)
        }
    }

    protected fun assertNotAuthenticated() {
        runBlocking {
            val sessionInfo = client.auth.sessionManager.loadSession()
            assertTrue("User should not be authenticated", sessionInfo == null)
        }
    }

    protected suspend fun logoutIfNeeded() {
        try {
            client.auth.signOut()
            currentUserId = null
        } catch (e: Exception) {
            // Ignore errors during teardown
        }
    }

    protected fun buildTestData(vararg pairs: Pair<String, Any?>): Map<String, Any?> {
        return TestDataManager.buildTestData(pairs = pairs, testId = testId)
    }
}
