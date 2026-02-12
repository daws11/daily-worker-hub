package com.example.dwhubfix.presentation.worker.stats

import com.example.dwhubfix.domain.model.WorkerStats

/**
 * UI State for Worker Stats/Profile Screen
 *
 * Contains all the state needed for the worker stats screen UI.
 */
data class WorkerStatsUiState(
    val isLoading: Boolean = true,
    val stats: WorkerStats? = null,
    val error: String? = null,
    val isRefreshing: Boolean = false
) {
    val hasStats: Boolean get() = stats != null
    val isEmpty: Boolean get() = stats == null && !isLoading
}

/**
 * UI Events for Worker Stats Screen
 *
 * Represents user actions that can trigger state changes.
 */
sealed class WorkerStatsUiEvent {
    object Refresh : WorkerStatsUiEvent()
    object ClearError : WorkerStatsUiEvent()
}
