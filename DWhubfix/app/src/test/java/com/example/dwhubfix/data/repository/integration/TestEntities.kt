package com.example.dwhubfix.data.repository.integration

import kotlinx.serialization.Serializable

/**
 * Serializable test data entities for Supabase integration tests
 *
 * Using @Serializable data classes instead of Map<String, Any?> to avoid
 * kotlinx.serialization.SerializationException: "Serializer for class 'Any' is not found"
 */

@Serializable
data class TestJobData(
    val business_id: String,
    val title: String,
    val description: String,
    val wage: Double,
    val wage_type: String,
    val location: String,
    val category: String,
    val start_time: String,
    val end_time: String,
    val shift_date: String,
    val is_urgent: Boolean,
    val worker_count: Int,
    val status: String = "open",
    val test_id: String? = null,
    val is_test_data: Boolean = true
)

@Serializable
data class TestJobApplicationData(
    val job_id: String,
    val worker_id: String,
    val status: String = "pending",
    val message: String? = null,
    val test_id: String? = null,
    val is_test_data: Boolean = true,
    val created_at: String? = null,
    val accepted_at: String? = null,
    val completed_at: String? = null,
    val started_at: String? = null
)

@Serializable
data class TestBookingData(
    val job_id: String,
    val worker_id: String,
    val business_id: String,
    val booking_date: String,
    val start_time: String,
    val end_time: String,
    val status: String = "pending",
    val test_id: String? = null,
    val is_test_data: Boolean = true
)

@Serializable
data class TestShiftData(
    val booking_id: String,
    val worker_id: String,
    val shift_date: String,
    val start_time: String,
    val end_time: String,
    val status: String = "scheduled",
    val test_id: String? = null,
    val is_test_data: Boolean = true
)

/**
 * Helper function to create test job data with default values
 */
fun createTestJobData(
    businessId: String,
    title: String = "Test Job",
    description: String = "Test job description",
    wage: Double = 50000.0,
    wageType: String = "per_hour",
    location: String = "Jakarta, Indonesia",
    category: String = "hospitality",
    startTime: String = "09:00",
    endTime: String = "17:00",
    shiftDate: String,
    isUrgent: Boolean = false,
    workerCount: Int = 1,
    status: String = "open",
    testId: String? = null
): TestJobData {
    return TestJobData(
        business_id = businessId,
        title = title,
        description = description,
        wage = wage,
        wage_type = wageType,
        location = location,
        category = category,
        start_time = startTime,
        end_time = endTime,
        shift_date = shiftDate,
        is_urgent = isUrgent,
        worker_count = workerCount,
        status = status,
        test_id = testId,
        is_test_data = true
    )
}

/**
 * Helper function to create test job application data
 */
fun createTestJobApplicationData(
    jobId: String,
    workerId: String,
    status: String = "pending",
    message: String? = null,
    testId: String? = null
): TestJobApplicationData {
    return TestJobApplicationData(
        job_id = jobId,
        worker_id = workerId,
        status = status,
        message = message,
        test_id = testId,
        is_test_data = true
    )
}

/**
 * Helper function to create test booking data
 */
fun createTestBookingData(
    jobId: String,
    workerId: String,
    businessId: String,
    bookingDate: String,
    startTime: String = "09:00",
    endTime: String = "17:00",
    status: String = "pending",
    testId: String? = null
): TestBookingData {
    return TestBookingData(
        job_id = jobId,
        worker_id = workerId,
        business_id = businessId,
        booking_date = bookingDate,
        start_time = startTime,
        end_time = endTime,
        status = status,
        test_id = testId,
        is_test_data = true
    )
}
