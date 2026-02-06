package com.example.dwhubfix.domain.model

/**
 * Create Job Request
 *
 * Request model for creating a new job posting by business.
 */
data class CreateJobRequest(
    val title: String,
    val description: String,
    val wage: Double,
    val wageType: String,
    val location: String,
    val category: String,
    val shiftDate: java.time.LocalDate,
    val startTime: String,
    val endTime: String,
    val workerCount: Int,
    val isUrgent: Boolean = false
) {
    init {
        // Validate wage type
        require(wageType in listOf("per_shift", "per_hour", "per_day")) {
            "Invalid wage type: must be per_shift, per_hour, or per_day"
        }

        // Validate worker count
        require(workerCount in 1..10) {
            "Worker count must be between 1 and 10"
        }

        // Validate time format (HH:MM 24-hour format)
        val timeRegex = Regex("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")
        require(timeRegex.matches(startTime)) {
            "Invalid start time format: must be HH:MM (24-hour)"
        }
        require(timeRegex.matches(endTime)) {
            "Invalid end time format: must be HH:MM (24-hour)"
        }
    }
}

/**
 * Apply for Job Request
 *
 * Request model for worker applying to a job.
 */
data class ApplyForJobRequest(
    val jobId: String,
    val coverLetter: String? = null
)

/**
 * Complete Job Request
 *
 * Request model for worker marking a job as completed.
 */
data class CompleteJobRequest(
    val applicationId: String,
    val completedAt: java.time.LocalDateTime = java.time.LocalDateTime.now()
)
