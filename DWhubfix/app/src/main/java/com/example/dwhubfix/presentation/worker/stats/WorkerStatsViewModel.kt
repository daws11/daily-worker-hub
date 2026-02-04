package com.example.dwhubfix.presentation.worker.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dwhubfix.domain.usecase.GetWorkerStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Worker Stats/Profile Screen
 *
 * Manages the state and business logic for the worker statistics screen.
 * Handles fetching worker stats including earnings, shifts, ratings.
 *
 * @property getWorkerStatsUseCase Use case for fetching worker stats
 */
@HiltViewModel
class WorkerStatsViewModel @Inject constructor(
    private val getWorkerStatsUseCase: GetWorkerStatsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkerStatsUiState())
    val uiState: StateFlow<WorkerStatsUiState> = _uiState.asStateFlow()

    init {
        refreshStats()
    }

    /**
     * Handle UI events
     */
    fun onEvent(event: WorkerStatsUiEvent) {
        when (event) {
            is WorkerStatsUiEvent.Refresh -> refreshStats()
            is WorkerStatsUiEvent.ClearError -> clearError()
        }
    }

    /**
     * Refresh worker stats
     */
    private fun refreshStats() {
        viewModelScope.launch {
            val currentState = _uiState.value
            updateState {
                it.copy(
                    isLoading = currentState.stats == null,
                    isRefreshing = currentState.stats != null,
                    error = null
                )
            }

            getWorkerStatsUseCase()
                .onSuccess { stats ->
                    updateState {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            stats = stats,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    updateState {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = "Gagal memuat statistik: ${exception.message}"
                        )
                    }
                }
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
    private fun updateState(update: (WorkerStatsUiState) -> WorkerStatsUiState) {
        _uiState.value = update(_uiState.value)
    }

    /**
     * Get current state synchronously
     */
    val currentState: WorkerStatsUiState
        get() = _uiState.value
}
