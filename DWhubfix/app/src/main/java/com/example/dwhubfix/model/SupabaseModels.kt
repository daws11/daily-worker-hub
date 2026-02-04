package com.example.dwhubfix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.osmdroid.util.GeoPoint
import java.time.LocalDate
import java.time.LocalTime

@Serializable
data class Job(
    val id: String,
    @SerialName("business_id") val businessId: String,
    val title: String,
    val description: String? = null,
    val wage: Double? = null,
    @SerialName("wage_type") val wageType: String? = null,
    val location: String? = null,
    val category: String? = null,
    val status: String = "open",
    val created_at: String? = null,
    val updated_at: String? = null,

    // Time fields
    @SerialName("start_time") val startTime: String? = null,
    @SerialName("end_time") val endTime: String? = null,
    @SerialName("shift_date") val shiftDate: String? = null,

    // Urgency flag
    val isUrgent: Boolean = false,

    // Compliance flag (21 Days Rule - PP 35/2021)
    // This is calculated by backend, can be null if not checked yet
    val isCompliant: Boolean? = null,

    // Worker count (number of workers needed)
    @SerialName("worker_count") val workerCount: Int? = null,

    // We might want to join business info
    @SerialName("profiles") val businessInfo: UserProfile? = null
)

// Keep old compatibility aliases
typealias DailyWorkerJob = Job
typealias JobListing = Job
