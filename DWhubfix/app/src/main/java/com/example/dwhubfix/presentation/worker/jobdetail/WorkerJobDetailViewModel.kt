package com.example.dwhubfix.presentation.worker.jobdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dwhubfix.domain.usecase.AcceptJobUseCase
import com.example.dwhubfix.domain.usecase.GetJobsForWorkerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Worker Job Detail Screen
 *
 * Manages the state and business logic for the job detail screen.
 * Handles loading job details and accepting jobs.
 *
 * @property getJobsForWorkerUseCase Use case for fetching jobs (to get job details)
 * @property acceptJobUseCase Use case for accepting jobs
 */
@HiltViewModel
class WorkerJobDetailViewModel @Inject constructor(
    private val acceptJobUseCase: AcceptJobUseCase,
    private val getJobsForWorkerUseCase: GetJobsForWorkerUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkerJobDetailUiState())
    val uiState: StateFlow<WorkerJobDetailUiState> = _uiState.asStateFlow()

    /**
     * Handle UI events
     */
    fun onEvent(event: WorkerJobDetailUiEvent) {
        when (event) {
            is WorkerJobDetailUiEvent.LoadJob -> loadJob(event.jobId)
            is WorkerJobDetailUiEvent.ShowAcceptDialog -> showAcceptDialog(event.show)
            is WorkerJobDetailUiEvent.AcceptJob -> acceptJob(event.jobId)
            is WorkerJobDetailUiEvent.ClearError -> clearError()
        }
    }

    /**
     * Load job details
     */
    private fun loadJob(jobId: String) {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true, error = null) }

            // Fetch jobs and find the specific job
            getJobsForWorkerUseCase(null)
                .onSuccess { jobs ->
                    val job = jobs.find { it.job.id == jobId }?.job
                    if (job != null) {
                        updateState {
                            it.copy(
                                isLoading = false,
                                job = job,
                                error = null
                            )
                        }
                    } else {
                        updateState {
                            it.copy(
                                isLoading = false,
                                error = "Pekerjaan tidak ditemukan"
                            )
                        }
                    }
                }
                .onFailure { exception ->
                    updateState {
                        it.copy(
                            isLoading = false,
                            error = "Gagal memuat pekerjaan: ${exception.message}"
                        )
                    }
                }
        }
    }

    /**
     * Show/hide accept dialog
     */
    private fun showAcceptDialog(show: Boolean) {
        updateState { it.copy(showAcceptDialog = show) }
    }

    /**
     * Accept a job
     */
    private fun acceptJob(jobId: String) {
        viewModelScope.launch {
            updateState { it.copy(isAccepting = true) }

            acceptJobUseCase(jobId)
                .onSuccess {
                    updateState {
                        it.copy(
                            isAccepting = false,
                            showAcceptDialog = false,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    updateState {
                        it.copy(
                            isAccepting = false,
                            error = "Gagal menerima pekerjaan: ${exception.message}"
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
    private fun updateState(update: (WorkerJobDetailUiState) -> WorkerJobDetailUiState) {
        _uiState.value = update(_uiState.value)
    }

    /**
     * Get current state synchronously
     */
    val currentState: WorkerJobDetailUiState
        get() = _uiState.value
}
