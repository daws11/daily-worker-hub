package com.example.dwhubfix.presentation.business.jobs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dwhubfix.domain.model.Job
import com.example.dwhubfix.domain.usecase.GetBusinessJobsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Business Job List Screen
 *
 * Manages the state and business logic for the business job list screen.
 * Handles fetching, filtering, searching, and deleting jobs.
 *
 * @property getBusinessJobsUseCase Use case for fetching business jobs
 */
@HiltViewModel
class BusinessJobListViewModel @Inject constructor(
    private val getBusinessJobsUseCase: GetBusinessJobsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BusinessJobListUiState())
    val uiState: StateFlow<BusinessJobListUiState> = _uiState.asStateFlow()

    init {
        refreshJobs()
    }

    /**
     * Handle UI events
     */
    fun onEvent(event: BusinessJobListUiEvent) {
        when (event) {
            is BusinessJobListUiEvent.SearchQueryChanged -> handleSearchQuery(event.query)
            is BusinessJobListUiEvent.StatusFilterChanged -> handleStatusFilter(event.filter)
            is BusinessJobListUiEvent.ToggleFilterSheet -> toggleFilterSheet(event.show)
            is BusinessJobListUiEvent.JobSelected -> selectJob(event.job)
            is BusinessJobListUiEvent.DeleteJob -> deleteJob(event.jobId)
            is BusinessJobListUiEvent.Refresh -> refreshJobs()
            is BusinessJobListUiEvent.ClearError -> clearError()
        }
    }

    /**
     * Refresh jobs from server
     */
    private fun refreshJobs() {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true, error = null) }

            getBusinessJobsUseCase()
                .onSuccess { jobs ->
                    updateState { state ->
                        state.copy(
                            isLoading = false,
                            jobs = jobs,
                            filteredJobs = applyFilters(jobs, state.searchQuery, state.statusFilter),
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    updateState {
                        it.copy(
                            isLoading = false,
                            error = "Gagal memuat lowongan: ${exception.message}"
                        )
                    }
                }
        }
    }

    /**
     * Handle search query change
     */
    private fun handleSearchQuery(query: String) {
        updateState { state ->
            state.copy(
                searchQuery = query,
                filteredJobs = applyFilters(state.jobs, query, state.statusFilter)
            )
        }
    }

    /**
     * Handle status filter change
     */
    private fun handleStatusFilter(filter: JobStatusFilter) {
        updateState { state ->
            state.copy(
                statusFilter = filter,
                filteredJobs = applyFilters(state.jobs, state.searchQuery, filter)
            )
        }
    }

    /**
     * Apply filters to job list
     */
    private fun applyFilters(
        jobs: List<Job>,
        searchQuery: String,
        statusFilter: JobStatusFilter
    ): List<Job> {
        var filtered = jobs

        // Apply status filter
        filtered = when (statusFilter) {
            JobStatusFilter.ALL -> filtered
            JobStatusFilter.OPEN -> filtered.filter { it.status == "open" }
            JobStatusFilter.FILLED -> filtered.filter { it.status == "filled" }
            JobStatusFilter.CANCELLED -> filtered.filter { it.status == "cancelled" }
        }

        // Apply search filter
        if (searchQuery.isNotBlank()) {
            filtered = filtered.filter { job ->
                job.title.contains(searchQuery, ignoreCase = true) ||
                job.category?.contains(searchQuery, ignoreCase = true) == true
            }
        }

        return filtered
    }

    /**
     * Toggle filter sheet visibility
     */
    private fun toggleFilterSheet(show: Boolean) {
        updateState { it.copy(showFilterSheet = show) }
    }

    /**
     * Select a job
     */
    private fun selectJob(job: Job?) {
        updateState { it.copy(selectedJob = job) }
    }

    /**
     * Delete a job (placeholder - would need proper implementation)
     */
    private fun deleteJob(jobId: String) {
        // TODO: Implement delete functionality
        // For now, just show a message
        updateState { it.copy(error = "Hapus lowongan belum diimplementasikan") }
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
    private fun updateState(update: (BusinessJobListUiState) -> BusinessJobListUiState) {
        _uiState.value = update(_uiState.value)
    }

    /**
     * Get current state synchronously
     */
    val currentState: BusinessJobListUiState
        get() = _uiState.value
}
