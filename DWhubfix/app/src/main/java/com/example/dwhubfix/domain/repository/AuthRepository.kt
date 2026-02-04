package com.example.dwhubfix.domain.repository

/**
 * Authentication Repository Interface
 *
 * Defines the contract for authentication operations.
 * Implementations should handle login, registration, and session management.
 */
interface AuthRepository {

    /**
     * Sign in with email and password
     *
     * @param email User's email address
     * @param password User's password
     * @return Result containing user ID on success
     */
    suspend fun login(email: String, password: String): Result<String>

    /**
     * Register a new worker account
     *
     * @param fullName Worker's full name
     * @param email Worker's email address
     * @param password Worker's password
     * @param phoneNumber Worker's phone number
     * @return Result containing user ID on success
     */
    suspend fun registerWorker(
        fullName: String,
        email: String,
        password: String,
        phoneNumber: String
    ): Result<String>

    /**
     * Register a new business account
     *
     * @param businessName Business name
     * @param email Business email address
     * @param password Business password
     * @param phoneNumber Business phone number
     * @return Result containing user ID on success
     */
    suspend fun registerBusiness(
        businessName: String,
        email: String,
        password: String,
        phoneNumber: String
    ): Result<String>

    /**
     * Sign out current user
     *
     * @return Result indicating success or failure
     */
    suspend fun logout(): Result<Unit>

    /**
     * Get current access token
     *
     * @return Access token if available, null otherwise
     */
    fun getAccessToken(): String?

    /**
     * Get current user ID
     *
     * @return User ID if available, null otherwise
     */
    fun getUserId(): String?
}
