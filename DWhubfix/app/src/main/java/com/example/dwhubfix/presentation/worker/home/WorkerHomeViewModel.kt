package com.example.dwhubfix.presentation.worker.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dwhubfix.domain.model.Job
import com.example.dwhubfix.domain.model.JobWithScore
import com.example.dwhubfix.domain.usecase.AcceptJobUseCase
import com.example.dwhubfix.domain.usecase.GetJobsForWorkerUseCase
import com.example.dwhubfix.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

/**
 * ViewModel for Worker Home Screen
 *
 * Manages the state and business logic for the worker home screen.
 * Handles job fetching, filtering, search, and job acceptance.
 *
 * @property getJobsForWorkerUseCase Use case for fetching jobs
 * @property acceptJobUseCase Use case for accepting jobs
 * @property loginUseCase Use case for authentication
 */
@HiltViewModel
class WorkerHomeViewModel @Inject constructor(
    private val getJobsForWorkerUseCase: GetJobsForWorkerUseCase,
    private val acceptJobUseCase: AcceptJobUseCase,
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkerHomeUiState())
    val uiState: StateFlow<WorkerHomeUiState> = _uiState.asStateFlow()

    init {
        // Refresh jobs on init
        refreshJobs()
    }

    /**
     * Handle UI events
     */
    fun onEvent(event: WorkerHomeUiEvent) {
        when (event) {
            is WorkerHomeUiEvent.RefreshJobs -> refreshJobs(event.workerLocation)
            is WorkerHomeUiEvent.SearchQueryChanged -> handleSearchQuery(event.query)
            is WorkerHomeUiEvent.ToggleFilterSheet -> toggleFilterSheet(event.show)
            is WorkerHomeUiEvent.ToggleMapView -> toggleMapView(event.show)
            is WorkerHomeUiEvent.AcceptJob -> acceptJob(event.jobId)
            is WorkerHomeUiEvent.JobSelected -> selectJob(event.job)
            WorkerHomeUiEvent.ClearError -> clearError()
            WorkerHomeUiEvent.ClearSnackbar -> clearSnackbar()
            WorkerHomeUiEvent.Refresh -> refreshJobs()
        }
    }

    /**
     * Refresh jobs from server
     */
    private fun refreshJobs(workerLocation: GeoPoint? = null) {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true, error = null) }

            getJobsForWorkerUseCase(workerLocation)
                .onSuccess { jobs ->
                    updateState { state ->
                        state.copy(
                            isLoading = false,
                            jobs = jobs,
                            displayedJobs = jobs,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    updateState { state ->
                        state.copy(
                            isLoading = false,
                            error = "Gagal memuat jobs: ${exception.message}"
                        )
                    }
                }
        }
    }

    /**
     * Handle search query change
     */
    private fun handleSearchQuery(query: String) {
        updateState { it.copy(searchQuery = query) }
        filterJobs()
    }

    /**
     * Filter jobs based on search query
     */
    private fun filterJobs() {
        val currentState = _uiState.value

        val filtered = if (currentState.searchQuery.isBlank()) {
            currentState.jobs
        } else {
            currentState.jobs.filter { job ->
                job.job.title.contains(currentState.searchQuery, ignoreCase = true) ||
                job.job.category?.contains(currentState.searchQuery, ignoreCase = true) == true
            }
        }

        updateState { it.copy(displayedJobs = filtered) }
    }

    /**
     * Toggle filter sheet visibility
     */
    private fun toggleFilterSheet(show: Boolean) {
        updateState { it.copy(showFilterSheet = show) }
    }

    /**
     * Toggle map view
     */
    private fun toggleMapView(show: Boolean) {
        updateState { it.copy(showMap = show) }
    }

    /**
     * Accept a job
     */
    private fun acceptJob(jobId: String) {
        viewModelScope.launch {
            updateState { it.copy(isAcceptingJob = true) }

            acceptJobUseCase(jobId)
                .onSuccess {
                    updateState { state ->
                        state.copy(
                            isAcceptingJob = false,
                            snackbarMessage = "Pekerjaan berhasil diterima!"
                        )
                    }
                    // Refresh jobs after accepting
                    refreshJobs()
                }
                .onFailure { exception ->
                    updateState { state ->
                        state.copy(
                            isAcceptingJob = false,
                            error = "Gagal menerima pekerjaan: ${exception.message}"
                        )
                    }
                }
        }
    }

    /**
     * Select a job for details
     */
    private fun selectJob(job: Job) {
        updateState { it.copy(selectedJob = job) }
    }

    /**
     * Clear error message
     */
    private fun clearError() {
        updateState { it.copy(error = null) }
    }

    /**
     * Clear snackbar message
     */
    private fun clearSnackbar() {
        updateState { it.copy(snackbarMessage = null) }
    }

    /**
     * Update current state
     */
    private fun updateState(update: (WorkerHomeUiState) -> WorkerHomeUiState) {
        _uiState.value = update(_uiState.value)
    }

    /**
     * Get current state synchronously (for UI that needs immediate access)
     */
    val currentState: WorkerHomeUiState
        get() = _uiState.value

    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean = loginUseCase.isLoggedIn()

    /**
     * Get current user ID
     */
    fun getCurrentUserId(): String? = loginUseCase.getCurrentUserId()
}
