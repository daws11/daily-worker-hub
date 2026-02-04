package com.example.dwhubfix.data.repository

/**
 * Clock-in error types
 */
sealed class ClockInError {
    data object NoSession : ClockInError()
    data object BookingNotFound : ClockInError()
    data object AlreadyClockedIn : ClockInError()
    data object BookingNotConfirmed : ClockInError()
    data class NetworkError(val message: String) : ClockInError()
}

/**
 * Clock-out error types
 */
sealed class ClockOutError {
    data object NoSession : ClockOutError()
    data object BookingNotFound : ClockOutError()
    data object NotClockedIn : ClockOutError()
    data object AlreadyClockedOut : ClockOutError()
    data class NetworkError(val message: String) : ClockOutError()
}
