package com.example.dwhubfix.presentation.worker.jobdetail

import com.example.dwhubfix.domain.model.Job

/**
 * UI State for Worker Job Detail Screen
 *
 * Contains all the state needed for the job detail screen UI.
 */
data class WorkerJobDetailUiState(
    val isLoading: Boolean = true,
    val job: Job? = null,
    val isAccepting: Boolean = false,
    val error: String? = null,
    val showAcceptDialog: Boolean = false
) {
    val hasJob: Boolean get() = job != null
    val isEmpty: Boolean get() = job == null && !isLoading
}

/**
 * UI Events for Worker Job Detail Screen
 *
 * Represents user actions that can trigger state changes.
 */
sealed class WorkerJobDetailUiEvent {
    data class LoadJob(val jobId: String) : WorkerJobDetailUiEvent()
    data class ShowAcceptDialog(val show: Boolean) : WorkerJobDetailUiEvent()
    data class AcceptJob(val jobId: String) : WorkerJobDetailUiEvent()
    object ClearError : WorkerJobDetailUiEvent()
}
