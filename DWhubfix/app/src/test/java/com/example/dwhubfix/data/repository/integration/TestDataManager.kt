package com.example.dwhubfix.data.repository.integration

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.SupabaseClient as SupabaseClientInstance
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import java.io.FileInputStream
import java.io.File
import java.util.UUID

/**
 * Test Data Manager for JVM-based Testing
 *
 * Uses Supabase-KT 3.0.0 API with sessionManager.loadSession()
 */
object TestDataManager {

    private const val CONFIG_FILE = "test-config.properties"
    private const val DEFAULT_WORKER_EMAIL = "integration-test-worker@example.com"
    private const val DEFAULT_BUSINESS_EMAIL = "integration-test-business@example.com"

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
     * Load configuration from test-config.properties file
     */
    fun loadConfig(): TestConfig {
        val properties = Properties()

        val configUrl = javaClass.classLoader?.getResource(CONFIG_FILE)
        if (configUrl != null) {
            properties.load(configUrl.openStream())
        } else {
            val configFile = File("app/src/test/resources/$CONFIG_FILE")
            if (configFile.exists()) {
                properties.load(FileInputStream(configFile))
            }
        }

        val url = properties.getProperty("supabase.test.url", "")
        val key = properties.getProperty("supabase.test.key", "")

        require(url.isNotBlank()) { "Supabase URL must be configured in test-config.properties" }
        require(key.isNotBlank()) { "Supabase Anon Key must be configured in test-config.properties" }

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
     * Create a Supabase client for JVM-based testing
     * Uses a dummy session manager to avoid Android Context dependency
     */
    fun createTestClient(config: TestConfig = loadConfig()): SupabaseClientInstance {
        return createSupabaseClient(
            supabaseUrl = config.supabaseUrl,
            supabaseKey = config.supabaseKey
        ) {
            install(Auth) {
                // Use default session manager - JVM tests don't need persistent storage
                // Session will be in-memory only
            }
            install(Postgrest)
            install(Storage)
        }
    }

    /**
     * Generate a unique test ID for data isolation
     */
    fun generateTestId(): String = "test-${UUID.randomUUID()}"

    /**
     * Clean up test data from Supabase
     */
    suspend fun cleanupTestData(
        client: SupabaseClientInstance,
        testId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
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
     * Validate test users configuration
     */
    fun validateTestUsers(config: TestConfig = loadConfig()): ValidationResult {
        val errors = mutableListOf<String>()

        if (config.workerEmail.isBlank()) errors.add("Worker email not configured")
        if (config.workerPassword.isBlank()) errors.add("Worker password not configured")
        if (config.businessEmail.isBlank()) errors.add("Business email not configured")
        if (config.businessPassword.isBlank()) errors.add("Business password not configured")

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
