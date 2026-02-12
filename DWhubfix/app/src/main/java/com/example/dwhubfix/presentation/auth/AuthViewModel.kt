package com.example.dwhubfix.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dwhubfix.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Authentication Screens
 *
 * Manages the state and business logic for login and registration screens.
 * Handles form input, validation, login, and registration.
 *
 * @property loginUseCase Use case for authentication
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkAuthStatus()
    }

    /**
     * Handle UI events
     */
    fun onEvent(event: AuthUiEvent) {
        when (event) {
            is AuthUiEvent.EmailChanged -> updateEmail(event.email)
            is AuthUiEvent.PasswordChanged -> updatePassword(event.password)
            is AuthUiEvent.FullNameChanged -> updateFullName(event.fullName)
            is AuthUiEvent.PhoneNumberChanged -> updatePhoneNumber(event.phoneNumber)
            is AuthUiEvent.ToggleMode -> toggleMode(event.isLoginMode)
            is AuthUiEvent.Submit -> submit()
            is AuthUiEvent.ClearError -> clearError()
            is AuthUiEvent.CheckAuthStatus -> checkAuthStatus()
        }
    }

    /**
     * Update email field
     */
    private fun updateEmail(email: String) {
        updateState { it.copy(email = email, error = null) }
    }

    /**
     * Update password field
     */
    private fun updatePassword(password: String) {
        updateState { it.copy(password = password, error = null) }
    }

    /**
     * Update full name field
     */
    private fun updateFullName(fullName: String) {
        updateState { it.copy(fullName = fullName, error = null) }
    }

    /**
     * Update phone number field
     */
    private fun updatePhoneNumber(phoneNumber: String) {
        updateState { it.copy(phoneNumber = phoneNumber, error = null) }
    }

    /**
     * Toggle between login and register mode
     */
    private fun toggleMode(isLoginMode: Boolean) {
        updateState {
            it.copy(
                isLoginMode = isLoginMode,
                error = null,
                email = "",
                password = "",
                fullName = "",
                phoneNumber = ""
            )
        }
    }

    /**
     * Submit form (login or register)
     */
    private fun submit() {
        val currentState = _uiState.value

        if (!currentState.isFormValid) {
            updateState { it.copy(error = "Mohon lengkapi semua field yang diperlukan") }
            return
        }

        if (currentState.isLoginMode) {
            login()
        } else {
            // For now, register just logs in (would be expanded to full registration)
            login()
        }
    }

    /**
     * Perform login
     */
    private fun login() {
        val currentState = _uiState.value

        viewModelScope.launch {
            updateState { it.copy(isLoading = true, error = null) }

            loginUseCase(currentState.email, currentState.password)
                .onSuccess { userId ->
                    updateState {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            userId = userId,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    updateState {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = false,
                            error = "Login gagal: ${exception.message}"
                        )
                    }
                }
        }
    }

    /**
     * Perform logout
     */
    fun logout() {
        viewModelScope.launch {
            loginUseCase.logout()
                .onSuccess {
                    updateState {
                        AuthUiState(
                            isLoading = false,
                            isLoggedIn = false,
                            isLoginMode = true
                        )
                    }
                }
                .onFailure { exception ->
                    updateState { it.copy(error = "Logout gagal: ${exception.message}") }
                }
        }
    }

    /**
     * Check if user is logged in
     */
    private fun checkAuthStatus() {
        val isLoggedIn = loginUseCase.isLoggedIn()
        val userId = loginUseCase.getCurrentUserId()

        updateState {
            it.copy(
                isLoggedIn = isLoggedIn,
                userId = userId
            )
        }
    }

    /**
     * Clear error message
     */
    private fun clearError() {
        updateState { it.copy(error = null) }
    }

    /**
     * Update current state
     */
    private fun updateState(update: (AuthUiState) -> AuthUiState) {
        _uiState.value = update(_uiState.value)
    }

    /**
     * Get current state synchronously
     */
    val currentState: AuthUiState
        get() = _uiState.value
}
