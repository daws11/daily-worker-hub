package com.example.dwhubfix.presentation.business.jobs

import com.example.dwhubfix.domain.model.Job

/**
 * UI State for Business Job List Screen
 *
 * Contains all the state needed for the business job list screen UI.
 */
data class BusinessJobListUiState(
    val isLoading: Boolean = true,
    val jobs: List<Job> = emptyList(),
    val filteredJobs: List<Job> = emptyList(),
    val selectedJob: Job? = null,
    val showFilterSheet: Boolean = false,
    val searchQuery: String = "",
    val statusFilter: JobStatusFilter = JobStatusFilter.ALL,
    val error: String? = null,
    val isDeleting: Boolean = false
) {
    val hasJobs: Boolean get() = jobs.isNotEmpty()
    val isEmpty: Boolean get() = jobs.isEmpty() && !isLoading
}

/**
 * Job status filter options
 */
enum class JobStatusFilter {
    ALL, OPEN, FILLED, CANCELLED
}

/**
 * UI Events for Business Job List Screen
 *
 * Represents user actions that can trigger state changes.
 */
sealed class BusinessJobListUiEvent {
    data class SearchQueryChanged(val query: String) : BusinessJobListUiEvent()
    data class StatusFilterChanged(val filter: JobStatusFilter) : BusinessJobListUiEvent()
    data class ToggleFilterSheet(val show: Boolean) : BusinessJobListUiEvent()
    data class JobSelected(val job: Job?) : BusinessJobListUiEvent()
    data class DeleteJob(val jobId: String) : BusinessJobListUiEvent()
    object Refresh : BusinessJobListUiEvent()
    object ClearError : BusinessJobListUiEvent()
}
