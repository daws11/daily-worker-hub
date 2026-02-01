package com.example.dwhubfix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JobApplication(
    val id: String,
    @SerialName("job_id") val jobId: String,
    @SerialName("worker_id") val workerId: String,
    val status: String, // pending, interview, accepted, rejected, ongoing, completed, rated, cancelled
    val message: String? = null,
    @SerialName("applied_at") val appliedAt: String? = null,
    @SerialName("accepted_at") val acceptedAt: String? = null,
    @SerialName("started_at") val startedAt: String? = null,
    @SerialName("completed_at") val completedAt: String? = null,
    @SerialName("worker_rating") val workerRating: Int? = null,
    @SerialName("business_rating") val businessRating: Int? = null,
    @SerialName("worker_review") val workerReview: String? = null,
    @SerialName("business_review") val businessReview: String? = null,
    @SerialName("cancellation_reason") val cancellationReason: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    
    // Nested job data
    val job: Job? = null
)

/**
 * Helper extension to get status display text
 */
fun JobApplication.getStatusText(): String = when (status) {
    "pending" -> "Menunggu"
    "interview" -> "Interview"
    "accepted" -> "Diterima"
    "rejected" -> "Ditolak"
    "ongoing" -> "Sedang Berlangsung"
    "completed" -> "Selesai"
    "rated" -> "Dinilai"
    "cancelled" -> "Dibatalkan"
    else -> status
}

/**
 * Helper extension to check if job is active
 */
fun JobApplication.isActive(): Boolean = status in listOf("accepted", "ongoing")

/**
 * Helper extension to check if job is completed
 */
fun JobApplication.isCompleted(): Boolean = status in listOf("completed", "rated")
