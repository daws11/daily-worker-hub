package com.example.dwhubfix.domain.usecase

import com.example.dwhubfix.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Login Use Case
 *
 * Handles user login with email and password.
 * Delegates to AuthRepository for the actual authentication.
 *
 * @property authRepository Repository for authentication operations
 */
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * Execute login
     *
     * @param email User's email address
     * @param password User's password
     * @return Result containing user ID on success
     */
    suspend operator fun invoke(
        email: String,
        password: String
    ): Result<String> {
        return authRepository.login(email, password)
    }

    /**
     * Execute logout
     *
     * @return Result indicating success or failure
     */
    suspend fun logout(): Result<Unit> {
        return authRepository.logout()
    }

    /**
     * Check if user is logged in
     *
     * @return true if access token exists
     */
    fun isLoggedIn(): Boolean {
        return authRepository.getAccessToken() != null
    }

    /**
     * Get current user ID
     *
     * @return User ID if logged in, null otherwise
     */
    fun getCurrentUserId(): String? {
        return authRepository.getUserId()
    }
}
