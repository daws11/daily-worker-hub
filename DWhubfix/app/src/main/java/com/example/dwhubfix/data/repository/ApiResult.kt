package com.example.dwhubfix.data.repository

/**
 * Custom Result type for API responses
 *
 * Type-safe wrapper for Success/Error pattern without unchecked casts.
 */
sealed class ApiResult<out S, out E> {
    data class Success<S>(val data: S) : ApiResult<S, Nothing>()
    data class Failure<E>(val error: E) : ApiResult<Nothing, E>()

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure

    fun getOrNull(): S? = when (this) {
        is Success -> data
        is Failure -> null
    }

    inline fun <R> map(transform: (S) -> R): ApiResult<R, E> = when (this) {
        is Success -> Success(transform(data))
        is Failure -> this
    }

    inline fun onError(action: (E) -> Unit): ApiResult<S, E> {
        if (this is Failure) action(error)
        return this
    }
}
