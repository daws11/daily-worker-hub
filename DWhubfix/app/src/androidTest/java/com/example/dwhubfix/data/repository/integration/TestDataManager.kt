package com.example.dwhubfix.data.repository.integration

import android.content.Context
import io.github.jan.supabase.SupabaseClient as SupabaseClientInstance
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import java.io.FileInputStream
import java.io.File
import java.util.UUID

/**
 * Test Data Manager for Instrumentation Tests
 *
 * Manages test configuration, Supabase client creation, test user management,
 * and test data cleanup for integration tests running on Android devices/emulators.
 */
object TestDataManager {

    private const val CONFIG_FILE = "test-config.properties"
    private const val DEFAULT_WORKER_EMAIL = "integration-test-worker@example.com"
    private const val DEFAULT_BUSINESS_EMAIL = "integration-test-business@example.com"

    /**
     * Test configuration data class
     */
    data class TestConfig(
        val supabaseUrl: String,
        val supabaseKey: String,
        val workerEmail: String,
        val workerPassword: String,
        val businessEmail: String,
        val businessPassword: String,
        val serviceRoleKey: String? = null
    )

    /**
     * Application context reference (will be set during test initialization)
     */
    private var appContext: Context? = null

    /**
     * Set the application context for testing
     */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    /**
     * Get the application context
     */
    fun getContext(): Context {
        return appContext ?: throw IllegalStateException("TestDataManager not initialized. Call init(context) first.")
    }

    /**
     * Load configuration from test-config.properties file
     *
     * @return TestConfig with loaded values or defaults
     */
    fun loadConfig(): TestConfig {
        val properties = Properties()

        // Try to load from test resources
        val configUrl = javaClass.classLoader?.getResource(CONFIG_FILE)
        if (configUrl != null) {
            properties.load(configUrl.openStream())
        } else {
            // Try to load from file system
            val configFile = File("app/src/androidTest/resources/$CONFIG_FILE")
            if (configFile.exists()) {
                properties.load(FileInputStream(configFile))
            } else {
                // Try environment variables as fallback
                properties.setProperty("supabase.test.url", System.getenv("SUPABASE_DEV_URL") ?: "")
                properties.setProperty("supabase.test.key", System.getenv("SUPABASE_DEV_KEY") ?: "")
                properties.setProperty("test.user.worker.email", System.getenv("TEST_WORKER_EMAIL") ?: DEFAULT_WORKER_EMAIL)
                properties.setProperty("test.user.worker.password", System.getenv("TEST_WORKER_PASSWORD") ?: "TestWorker123!")
                properties.setProperty("test.user.business.email", System.getenv("TEST_BUSINESS_EMAIL") ?: DEFAULT_BUSINESS_EMAIL)
                properties.setProperty("test.user.business.password", System.getenv("TEST_BUSINESS_PASSWORD") ?: "TestBusiness123!")
            }
        }

        val url = properties.getProperty("supabase.test.url")
        val key = properties.getProperty("supabase.test.key")

        require(url.isNotBlank()) { "Supabase URL must be configured in test-config.properties or SUPABASE_DEV_URL environment variable" }
        require(key.isNotBlank()) { "Supabase Anon Key must be configured in test-config.properties or SUPABASE_DEV_KEY environment variable" }

        return TestConfig(
            supabaseUrl = url,
            supabaseKey = key,
            workerEmail = properties.getProperty("test.user.worker.email", DEFAULT_WORKER_EMAIL),
            workerPassword = properties.getProperty("test.user.worker.password", "TestWorker123!"),
            businessEmail = properties.getProperty("test.user.business.email", DEFAULT_BUSINESS_EMAIL),
            businessPassword = properties.getProperty("test.user.business.password", "TestBusiness123!"),
            serviceRoleKey = properties.getProperty("supabase.test.service.role.key")?.takeIf { it.isNotBlank() }
        )
    }

    /**
     * Create a Supabase client for instrumentation testing
     * Uses the Android Context for session management
     *
     * @param config Test configuration
     * @return Configured SupabaseClientInstance
     */
    fun createTestClient(config: TestConfig = loadConfig()): SupabaseClientInstance {
        val context = getContext()

        return createSupabaseClient(
            supabaseUrl = config.supabaseUrl,
            supabaseKey = config.supabaseKey
        ) {
            // Install Auth - will use default SettingsSessionManager with Android Context
            install(Auth)
            install(Postgrest)
        }
    }

    /**
     * Generate a unique test ID for data isolation
     * Each test gets a unique UUID to tag its data
     *
     * @return Unique test identifier
     */
    fun generateTestId(): String = "test-${UUID.randomUUID()}"

    /**
     * Clean up test data from Supabase
     * Deletes all rows tagged with the given test_id
     *
     * @param client Supabase client
     * @param testId Unique test identifier to clean up
     */
    suspend fun cleanupTestData(
        client: SupabaseClientInstance,
        testId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Clean up in reverse dependency order
            cleanupTable(client, "shifts", testId)
            cleanupTable(client, "bookings", testId)
            cleanupTable(client, "job_applications", testId)
            cleanupTable(client, "jobs", testId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Clean up a specific table by test_id
     */
    private suspend fun cleanupTable(
        client: SupabaseClientInstance,
        table: String,
        testId: String
    ) {
        try {
            client.from(table).delete {
                filter {
                    eq("test_id", testId)
                }
            }
        } catch (e: Exception) {
            // Log but don't fail - table might not have test_id column yet
            println("Warning: Could not clean up table $table: ${e.message}")
        }
    }

    /**
     * Helper to build test data with test_id tagging
     */
    fun buildTestData(vararg pairs: Pair<String, Any?>, testId: String? = null): Map<String, Any?> {
        return buildMap {
            putAll(pairs)
            testId?.let { put("test_id", it) }
            put("is_test_data", true)
        }
    }

    /**
     * Assert that test users exist or provide instructions for creating them
     * This doesn't create users automatically, just validates configuration
     */
    fun validateTestUsers(config: TestConfig = loadConfig()): ValidationResult {
        val errors = mutableListOf<String>()

        if (config.workerEmail.isBlank()) {
            errors.add("Worker email not configured")
        }
        if (config.workerPassword.isBlank()) {
            errors.add("Worker password not configured")
        }
        if (config.businessEmail.isBlank()) {
            errors.add("Business email not configured")
        }
        if (config.businessPassword.isBlank()) {
            errors.add("Business password not configured")
        }

        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Invalid(errors.joinToString("; "))
        }
    }

    sealed class ValidationResult {
        data object Success : ValidationResult()
        data class Invalid(val message: String) : ValidationResult()
    }
}
