package com.example.dwhubfix.data.repository.integration

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.example.dwhubfix.data.*
import io.github.jan.supabase.SupabaseClient as SupabaseClientInstance
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Before
import org.junit.Assert.*

/**
 * Base Integration Test for Instrumentation Tests
 *
 * Provides common setup and teardown for all integration tests.
 * Handles Supabase client initialization, test authentication, and data cleanup.
 *
 * Setup:
 * - Initializes TestDataManager with Android Context
 * - Loads test configuration from test-config.properties
 * - Creates Supabase client with Android Context support
 * - Provides test SharedPreferences for session storage
 *
 * Teardown:
 * - Logs out any authenticated user
 * - Cleans up test data tagged with test_id
 * - Clears SharedPreferences
 */
abstract class BaseIntegrationTest {

    /**
     * Android Context for instrumentation tests
     */
    protected val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    /**
     * Supabase client instance for tests
     */
    protected lateinit var client: SupabaseClientInstance
        private set

    /**
     * Test configuration
     */
    protected lateinit var config: TestDataManager.TestConfig
        private set

    /**
     * Test SharedPreferences provider for session storage
     */
    protected lateinit var testPrefsProvider: TestSharedPreferencesProvider
        private set

    /**
     * Unique test ID for data isolation
     */
    protected lateinit var testId: String
        private set

    /**
     * Current authenticated user ID (if any)
     */
    protected var currentUserId: String? = null

    @Before
    open fun setup() {
        // Initialize TestDataManager with Android Context
        TestDataManager.init(context)

        // Load test configuration
        config = TestDataManager.loadConfig()

        // Validate test user configuration
        val validation = TestDataManager.validateTestUsers(config)
        if (validation is TestDataManager.ValidationResult.Invalid) {
            fail("Test user configuration invalid: ${validation.message}. " +
                    "Please configure test-config.properties with valid test user credentials.")
        }

        // Create Supabase client
        client = TestDataManager.createTestClient(config)

        // Create test SharedPreferences provider
        testPrefsProvider = TestSharedPreferencesProvider()

        // Generate unique test ID for this test
        testId = TestDataManager.generateTestId()

        // Log out any existing session
        runBlocking { logoutIfNeeded() }
    }

    @After
    open fun tearDown() {
        // Clean up test data and logout using runBlocking
        runBlocking {
            try {
                if (::client.isInitialized && ::testId.isInitialized) {
                    TestDataManager.cleanupTestData(client, testId)
                }
            } catch (e: Exception) {
                // Log warning but don't fail tests
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

        // Clear preferences
        if (::testPrefsProvider.isInitialized) {
            testPrefsProvider.clearPreferences()
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Authenticate as worker test user
     *
     * @return User ID of authenticated worker
     */
    protected suspend fun authenticateAsWorker(): String = withContext(Dispatchers.IO) {
        client.auth.signInWith(Email) {
            email = config.workerEmail
            password = config.workerPassword
        }

        // Get session after sign in
        val sessionInfo = client.auth.sessionManager.loadSession()
        val accessToken = sessionInfo?.accessToken ?: throw IllegalStateException("No access token")
        // For now, use a fallback approach - extract from JWT or use session user
        val userId = sessionInfo?.user?.id?.toString()
            ?: throw IllegalStateException("Failed to get user ID")

        currentUserId = userId

        // Save session to test SharedPreferences
        val prefs = testPrefsProvider.getSharedPreferences()
        saveSessionToPreferences(prefs, accessToken, userId)
        userId
    }

    /**
     * Authenticate as business test user
     *
     * @return User ID of authenticated business
     */
    protected suspend fun authenticateAsBusiness(): String = withContext(Dispatchers.IO) {
        client.auth.signInWith(Email) {
            email = config.businessEmail
            password = config.businessPassword
        }

        // Get session after sign in
        val sessionInfo = client.auth.sessionManager.loadSession()
        val accessToken = sessionInfo?.accessToken ?: throw IllegalStateException("No access token")
        val userId = sessionInfo?.user?.id?.toString()
            ?: throw IllegalStateException("Failed to get user ID")

        currentUserId = userId

        // Save session to test SharedPreferences
        val prefs = testPrefsProvider.getSharedPreferences()
        saveSessionToPreferences(prefs, accessToken, userId)
        userId
    }

    /**
     * Register a new worker user (useful for testing registration)
     *
     * @param email Unique email for test
     * @param password Password for test user
     * @return User ID of newly registered user
     */
    protected suspend fun registerWorker(
        email: String = "test-worker-${testId}@example.com",
        password: String = "TestPassword123!"
    ): String = withContext(Dispatchers.IO) {
        client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }

        // Get session after sign up
        val sessionInfo = client.auth.sessionManager.loadSession()
        val accessToken = sessionInfo?.accessToken ?: throw IllegalStateException("No access token")
        val userId = sessionInfo?.user?.id?.toString()
            ?: throw IllegalStateException("Failed to get user ID")

        currentUserId = userId

        // Save session to test SharedPreferences
        val prefs = testPrefsProvider.getSharedPreferences()
        saveSessionToPreferences(prefs, accessToken, userId)
        userId
    }

    /**
     * Register a new business user (useful for testing registration)
     *
     * @param email Unique email for test
     * @param password Password for test user
     * @return User ID of newly registered user
     */
    protected suspend fun registerBusiness(
        email: String = "test-business-${testId}@example.com",
        password: String = "TestPassword123!"
    ): String = withContext(Dispatchers.IO) {
        client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }

        // Get session after sign up
        val sessionInfo = client.auth.sessionManager.loadSession()
        val accessToken = sessionInfo?.accessToken ?: throw IllegalStateException("No access token")
        val userId = sessionInfo?.user?.id?.toString()
            ?: throw IllegalStateException("Failed to get user ID")

        currentUserId = userId

        // Save session to test SharedPreferences
        val prefs = testPrefsProvider.getSharedPreferences()
        saveSessionToPreferences(prefs, accessToken, userId)
        userId
    }

    /**
     * Get current access token from SharedPreferences
     */
    protected fun getAccessToken(): String? {
        return if (::testPrefsProvider.isInitialized) {
            getAccessTokenFromPreferences(testPrefsProvider.getSharedPreferences())
        } else {
            null
        }
    }

    /**
     * Get current user ID from SharedPreferences
     */
    protected fun getStoredUserId(): String? {
        return if (::testPrefsProvider.isInitialized) {
            fetchUserIdFromPreferences(testPrefsProvider.getSharedPreferences())
        } else {
            null
        }
    }

    /**
     * Assert that user is authenticated
     */
    protected fun assertAuthenticated() {
        val token = getAccessToken()
        val userId = getStoredUserId()
        assertTrue(
            "User should be authenticated. Token: $token, UserId: $userId",
            token != null && userId != null
        )
    }

    /**
     * Assert that user is NOT authenticated
     */
    protected fun assertNotAuthenticated() {
        val token = getAccessToken()
        val userId = getStoredUserId()
        assertTrue(
            "User should not be authenticated. Token: $token, UserId: $userId",
            token == null && userId == null
        )
    }

    // ==================== PROTECTED METHODS ====================

    protected suspend fun logoutIfNeeded() {
        try {
            client.auth.signOut()
            if (::testPrefsProvider.isInitialized) {
                clearSessionFromPreferences(testPrefsProvider.getSharedPreferences())
            }
            currentUserId = null
        } catch (e: Exception) {
            // Ignore errors during teardown
        }
    }

    /**
     * Helper to build test data with test_id tagging
     */
    protected fun buildTestData(vararg pairs: Pair<String, Any?>): Map<String, Any?> {
        return TestDataManager.buildTestData(pairs = pairs, testId = testId)
    }
}
