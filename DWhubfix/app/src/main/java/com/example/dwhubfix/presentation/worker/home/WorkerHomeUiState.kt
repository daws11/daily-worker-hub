package com.example.dwhubfix.presentation.worker.home

import com.example.dwhubfix.domain.model.Job
import com.example.dwhubfix.domain.model.JobWithScore

/**
 * UI State for Worker Home Screen
 *
 * Contains all the state needed for the worker home screen UI.
 */
data class WorkerHomeUiState(
    val isLoading: Boolean = true,
    val jobs: List<JobWithScore> = emptyList(),
    val displayedJobs: List<JobWithScore> = emptyList(),
    val searchQuery: String = "",
    val showFilterSheet: Boolean = false,
    val showMap: Boolean = false,
    val selectedJob: Job? = null,
    val isAcceptingJob: Boolean = false,
    val error: String? = null,
    val snackbarMessage: String? = null
) {
    val hasJobs: Boolean get() = jobs.isNotEmpty()
    val isEmpty: Boolean get() = jobs.isEmpty() && !isLoading
    val isRefreshing: Boolean get() = isLoading && jobs.isNotEmpty()
}

/**
 * UI Events for Worker Home Screen
 *
 * Represents user actions that can trigger state changes.
 */
sealed class WorkerHomeUiEvent {
    data class RefreshJobs(val workerLocation: org.osmdroid.util.GeoPoint?) : WorkerHomeUiEvent()
    data class SearchQueryChanged(val query: String) : WorkerHomeUiEvent()
    data class ToggleFilterSheet(val show: Boolean) : WorkerHomeUiEvent()
    data class ToggleMapView(val show: Boolean) : WorkerHomeUiEvent()
    data class AcceptJob(val jobId: String) : WorkerHomeUiEvent()
    data class JobSelected(val job: Job) : WorkerHomeUiEvent()
    object ClearError : WorkerHomeUiEvent()
    object ClearSnackbar : WorkerHomeUiEvent()
    object Refresh : WorkerHomeUiEvent()
}
