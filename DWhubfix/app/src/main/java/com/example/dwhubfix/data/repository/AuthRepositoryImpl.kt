package com.example.dwhubfix.data.repository

import android.content.Context
import com.example.dwhubfix.core.network.SupabaseClient
import com.example.dwhubfix.data.SessionManager
import com.example.dwhubfix.domain.repository.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Authentication Repository Implementation
 *
 * Handles authentication operations using Supabase Auth.
 * Uses injected dependencies for better testability.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    @ApplicationContext private val context: Context
) : AuthRepository {

    private val client get() = supabaseClient.client

    override suspend fun login(email: String, password: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                client.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }

                val sessionInfo = client.auth.sessionManager.loadSession()
                val userId = sessionInfo?.user?.id ?: ""
                val accessToken = sessionInfo?.accessToken ?: ""

                SessionManager.saveSession(context, accessToken, userId)

                Result.success(userId)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun registerWorker(
        fullName: String,
        email: String,
        password: String,
        phoneNumber: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }

            val sessionInfo = client.auth.sessionManager.loadSession()
            val userId = sessionInfo?.user?.id ?: ""
            val accessToken = sessionInfo?.accessToken ?: ""

            SessionManager.saveSession(context, accessToken, userId)
            SessionManager.saveSelectedRole(context, "worker")

            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun registerBusiness(
        businessName: String,
        email: String,
        password: String,
        phoneNumber: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }

            val sessionInfo = client.auth.sessionManager.loadSession()
            val userId = sessionInfo?.user?.id ?: ""
            val accessToken = sessionInfo?.accessToken ?: ""

            SessionManager.saveSession(context, accessToken, userId)
            SessionManager.saveSelectedRole(context, "business")

            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            client.auth.signOut()
            SessionManager.clearSession(context)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAccessToken(): String? {
        return SessionManager.getAccessToken(context)
    }

    override fun getUserId(): String? {
        return SessionManager.getUserId(context)
    }
}
