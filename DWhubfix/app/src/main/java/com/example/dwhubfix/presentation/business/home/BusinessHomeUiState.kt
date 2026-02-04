package com.example.dwhubfix.presentation.business.home

/**
 * Business Statistics for UI display
 */
data class BusinessStatsUi(
    val activeShiftsToday: Int = 0,
    val pendingPatches: Int = 0,
    val workersHiredThisWeek: Int = 0,
    val totalSpendingThisMonth: Double = 0.0,
    val walletBalance: Double = 0.0
) {
    val formattedSpending: String
        get() = "Rp ${String.format("%,.0f", totalSpendingThisMonth)}"

    val formattedBalance: String
        get() = "Rp ${String.format("%,.0f", walletBalance)}"
}

/**
 * UI State for Business Home/Dashboard Screen
 *
 * Contains all the state needed for the business home screen UI.
 */
data class BusinessHomeUiState(
    val isLoading: Boolean = true,
    val stats: BusinessStatsUi? = null,
    val error: String? = null,
    val isRefreshing: Boolean = false
) {
    val hasStats: Boolean get() = stats != null
    val isEmpty: Boolean get() = stats == null && !isLoading
}

/**
 * UI Events for Business Home Screen
 *
 * Represents user actions that can trigger state changes.
 */
sealed class BusinessHomeUiEvent {
    object Refresh : BusinessHomeUiEvent()
    object ClearError : BusinessHomeUiEvent()
}
