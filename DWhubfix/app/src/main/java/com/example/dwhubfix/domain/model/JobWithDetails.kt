package com.example.dwhubfix.domain.model

/**
 * Job with Details
 *
 * Extended job model with additional information for detailed view.
 * Includes job, business, and application status for the current worker.
 */
data class JobWithDetails(
    val job: Job,
    val businessName: String?,
    val businessLocation: String?,
    val businessRating: Double?,
    val applicationStatus: String? = null, // For the current worker
    val applicationId: String? = null // Application ID if worker has applied
) {
    val isApplied: Boolean
        get() = applicationStatus != null

    val isAccepted: Boolean
        get() = applicationStatus == "accepted"

    val isPending: Boolean
        get() = applicationStatus == "pending"

    val isRejected: Boolean
        get() = applicationStatus == "rejected"

    companion object {
        fun fromJob(job: Job): JobWithDetails {
            return JobWithDetails(
                job = job,
                businessName = job.businessName,
                businessLocation = job.location,
                businessRating = null,
                applicationStatus = null,
                applicationId = null
            )
        }

        fun fromJobWithApplication(job: Job, application: JobApplication, workerId: String): JobWithDetails {
            return JobWithDetails(
                job = job,
                businessName = job.businessName,
                businessLocation = job.location,
                businessRating = null,
                applicationStatus = application.status,
                applicationId = application.id
            )
        }
    }
}
