package com.example.dwhubfix.presentation.auth

/**
 * UI State for Authentication Screens
 *
 * Contains all the state needed for the authentication screens UI.
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val isLoginMode: Boolean = true, // true = login, false = register
    val email: String = "",
    val password: String = "",
    val fullName: String = "",
    val phoneNumber: String = "",
    val error: String? = null,
    val userId: String? = null
) {
    val isFormValid: Boolean
        get() = when {
            isLoginMode -> email.isNotBlank() && password.isNotBlank()
            else -> email.isNotBlank() && password.isNotBlank() &&
                    fullName.isNotBlank() && phoneNumber.isNotBlank()
        }

    val canSubmit: Boolean get() = isFormValid && !isLoading
}

/**
 * UI Events for Authentication Screens
 *
 * Represents user actions that can trigger state changes.
 */
sealed class AuthUiEvent {
    data class EmailChanged(val email: String) : AuthUiEvent()
    data class PasswordChanged(val password: String) : AuthUiEvent()
    data class FullNameChanged(val fullName: String) : AuthUiEvent()
    data class PhoneNumberChanged(val phoneNumber: String) : AuthUiEvent()
    data class ToggleMode(val isLoginMode: Boolean) : AuthUiEvent()
    object Submit : AuthUiEvent()
    object ClearError : AuthUiEvent()
    object CheckAuthStatus : AuthUiEvent()
}
