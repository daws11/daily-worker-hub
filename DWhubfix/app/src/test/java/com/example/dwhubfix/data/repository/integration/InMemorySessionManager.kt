package com.example.dwhubfix.data.repository.integration

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.SessionStatus
import io.github.jan.supabase.auth.UserSession
import io.github.jan.supabase.auth.session.SessionSource
import io.github.jan.supabase.auth.session.SessionType
import io.github.jan.supabase.auth.session.Token
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * In-memory Session Manager for JVM Integration Testing
 *
 * This implementation doesn't require Android Context or Settings,
 * making it suitable for JVM-based unit and integration tests.
 */
class InMemorySessionManager : io.github.jan.supabase.auth.SessionManager {

    private val _sessionStatus = MutableStateFlow<SessionStatus>(
        SessionStatus.NotAuthenticated
    )

    override val sessionStatus: StateFlow<SessionStatus>
        get() = _sessionStatus

    override suspend fun updateSession(
        status: SessionStatus,
        source: SessionSource
    ) {
        _sessionStatus.value = status
    }

    override suspend fun loadSession(): SessionStatus {
        return _sessionStatus.value
    }

    override suspend fun deleteSession() {
        _sessionStatus.value = SessionStatus.NotAuthenticated
    }

    override suspend fun saveSession(
        session: UserSession,
        type: SessionType
    ) {
        _sessionStatus.value = SessionStatus.Authenticated(
            session = session,
            source = SessionSource.Restore
        )
    }

    override suspend fun canRefreshToken(): Boolean {
        return false
    }

    override suspend fun getToken(): Token? {
        return (_sessionStatus.value as? SessionStatus.Authenticated)?.session?.accessToken
    }

    override suspend fun retrieveSession(
        code: String,
        redirectUrl: String?
    ): UserSession? {
        return null
    }

    override suspend fun getSession(): UserSession? {
        return (_sessionStatus.value as? SessionStatus.Authenticated)?.session
    }
}
