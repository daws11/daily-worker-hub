package com.example.dwhubfix.presentation.business.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dwhubfix.domain.usecase.GetBusinessStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Business Home/Dashboard Screen
 *
 * Manages the state and business logic for the business dashboard screen.
 * Handles fetching business statistics and metrics.
 *
 * @property getBusinessStatsUseCase Use case for fetching business stats
 */
@HiltViewModel
class BusinessHomeViewModel @Inject constructor(
    private val getBusinessStatsUseCase: GetBusinessStatsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BusinessHomeUiState())
    val uiState: StateFlow<BusinessHomeUiState> = _uiState.asStateFlow()

    init {
        refreshStats()
    }

    /**
     * Handle UI events
     */
    fun onEvent(event: BusinessHomeUiEvent) {
        when (event) {
            is BusinessHomeUiEvent.Refresh -> refreshStats()
            is BusinessHomeUiEvent.ClearError -> clearError()
        }
    }

    /**
     * Refresh business stats
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

            try {
                val stats = getBusinessStatsUseCase()
                updateState {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        stats = BusinessStatsUi(
                            activeShiftsToday = stats.activeShiftsToday,
                            pendingPatches = stats.pendingPatches,
                            workersHiredThisWeek = stats.workersHiredThisWeek,
                            totalSpendingThisMonth = stats.totalSpendingThisMonth,
                            walletBalance = stats.walletBalance
                        ),
                        error = null
                    )
                }
            } catch (e: Exception) {
                updateState {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = "Gagal memuat statistik: ${e.message}"
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
    private fun updateState(update: (BusinessHomeUiState) -> BusinessHomeUiState) {
        _uiState.value = update(_uiState.value)
    }

    /**
     * Get current state synchronously
     */
    val currentState: BusinessHomeUiState
        get() = _uiState.value
}
