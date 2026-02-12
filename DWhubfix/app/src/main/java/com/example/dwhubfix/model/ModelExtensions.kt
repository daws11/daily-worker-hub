package com.example.dwhubfix.model

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Extension functions to convert Map<String, Any?> to typed models
 * Used for converting Supabase responses to strongly-typed models
 */

private val json = Json { ignoreUnknownKeys = true }

/**
 * Convert Map<String, Any?> to Job
 */
fun Map<String, Any?>.toJob(): Job {
    return Job(
        id = this["id"] as? String ?: "",
        businessId = this["business_id"] as? String ?: "",
        title = this["title"] as? String ?: "",
        description = this["description"] as? String,
        wage = (this["wage"] as? Number)?.toDouble(),
        wageType = this["wage_type"] as? String,
        location = this["location"] as? String,
        category = this["category"] as? String,
        status = this["status"] as? String ?: "open",
        created_at = this["created_at"] as? String,
        updated_at = this["updated_at"] as? String,
        startTime = this["start_time"] as? String,
        endTime = this["end_time"] as? String,
        shiftDate = this["shift_date"] as? String,
        isUrgent = this["is_urgent"] as? Boolean ?: false,
        isCompliant = this["is_compliant"] as? Boolean,
        workerCount = this["worker_count"] as? Int,
        businessInfo = (this["profiles"] as? Map<String, Any?>)?.toUserProfile()
    )
}

/**
 * Convert Map<String, Any?> to UserProfile
 */
fun Map<String, Any?>.toUserProfile(): UserProfile? {
    return try {
        UserProfile(
            id = this["id"] as? String ?: "",
            fullName = this["full_name"] as? String,
            email = this["email"] as? String,
            phoneNumber = this["phone_number"] as? String,
            avatarUrl = this["avatar_url"] as? String,
            role = this["role"] as? String,
            createdAt = this["created_at"] as? String,
            updatedAt = this["updated_at"] as? String,
            businessProfile = (this["business_profile"] as? Map<String, Any?>)?.toBusinessProfile(),
            workerProfile = (this["worker_profile"] as? Map<String, Any?>)?.toWorkerProfileNested()
        )
    } catch (e: Exception) {
        null
    }
}

/**
 * Convert Map<String, Any?> to BusinessProfile
 */
fun Map<String, Any?>.toBusinessProfile(): BusinessProfile? {
    return try {
        BusinessProfile(
            id = this["id"] as? String ?: "",
            userId = this["user_id"] as? String ?: "",
            businessName = this["business_name"] as? String,
            fullName = this["full_name"] as? String,
            avatarUrl = this["avatar_url"] as? String,
            businessType = this["business_type"] as? String,
            location = this["location"] as? String,
            address = this["address"] as? String,
            latitude = (this["latitude"] as? Number)?.toDouble(),
            longitude = (this["longitude"] as? Number)?.toDouble(),
            rating = (this["rating"] as? Number)?.toDouble(),
            createdAt = this["created_at"] as? String,
            updatedAt = this["updated_at"] as? String
        )
    } catch (e: Exception) {
        null
    }
}

/**
 * Convert Map<String, Any?> to WorkerProfileNested
 */
fun Map<String, Any?>.toWorkerProfileNested(): WorkerProfileNested? {
    return try {
        WorkerProfileNested(
            id = this["id"] as? String ?: "",
            rating = (this["rating"] as? Number)?.toDouble()
        )
    } catch (e: Exception) {
        null
    }
}

/**
 * Convert List<Map<String, Any?>> to List<Job>
 */
fun List<Map<String, Any?>>.toJobList(): List<Job> {
    return mapNotNull { it.toJob() }
}

/**
 * Convert Map<String, Any?> to BusinessStats
 */
fun Map<String, Any?>.toBusinessStats(): BusinessStats {
    return BusinessStats(
        activeShiftsToday = (this["active_shifts_today"] as? Number)?.toInt() ?: 0,
        workersHiredThisWeek = (this["workers_hired_this_week"] as? Number)?.toInt() ?: 0,
        totalSpendingThisMonth = (this["total_spending_this_month"] as? Number)?.toDouble() ?: 0.0,
        pendingPatches = (this["pending_patches"] as? Number)?.toInt() ?: 0,
        walletBalance = (this["wallet_balance"] as? Number)?.toDouble(),
        rateBaliSuggestion = (this["rate_bali_suggestion"] as? Map<String, Any?>)?.toRateBaliSuggestion()
    )
}

/**
 * Convert Map<String, Any?> to RateBaliSuggestion
 */
fun Map<String, Any?>.toRateBaliSuggestion(): RateBaliSuggestion? {
    return try {
        RateBaliSuggestion(
            region = this["region"] as? String ?: "",
            umk = (this["umk"] as? Number)?.toDouble() ?: 0.0,
            dailyWage = (this["daily_wage"] as? Number)?.toDouble() ?: 0.0,
            description = this["description"] as? String ?: ""
        )
    } catch (e: Exception) {
        null
    }
}

/**
 * Convert Map<String, Any?> to WorkerCandidate
 */
fun Map<String, Any?>.toWorkerCandidate(): WorkerCandidate {
    val breakdown = this["breakdown"] as? Map<String, Any?>
    return WorkerCandidate(
        workerId = this["worker_id"] as? String ?: "",
        workerName = this["worker_name"] as? String ?: "",
        avatarUrl = this["avatar_url"] as? String,
        skills = (this["skills"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
        rating = (this["rating"] as? Number)?.toDouble() ?: 0.0,
        matchScore = (this["match_score"] as? Number)?.toDouble() ?: 0.0,
        breakdown = WorkerScoreBreakdown(
            distanceScore = breakdown?.get("distance_score") as? Double ?: 0.0,
            skillScore = breakdown?.get("skill_score") as? Double ?: 0.0,
            ratingScore = breakdown?.get("rating_score") as? Double ?: 0.0,
            reliabilityScore = breakdown?.get("reliability_score") as? Double ?: 0.0,
            availabilityScore = breakdown?.get("availability_score") as? Double ?: 0.0
        ),
        distance = this["distance"] as? String ?: "0.0 km",
        reliabilityScore = (this["reliability_score"] as? Number)?.toDouble() ?: 0.0,
        noShowRate = (this["no_show_rate"] as? Number)?.toDouble() ?: 0.0,
        isCompliant = this["is_compliant"] as? Boolean ?: true,
        totalShiftsCompleted = (this["total_shifts_completed"] as? Number)?.toInt() ?: 0,
        hourlyRate = (this["hourly_rate"] as? Number)?.toDouble(),
        hourlyRateString = this["hourly_rate_string"] as? String,
        availabilityStatus = this["availability_status"] as? String ?: "Tersedia",
        lastActiveDate = this["last_active_date"] as? String,
        distanceValue = (this["distance_value"] as? Number)?.toDouble() ?: 0.0
    )
}

/**
 * Convert List<Map<String, Any?>> to List<WorkerCandidate>
 */
fun List<Map<String, Any?>>.toWorkerCandidateList(): List<WorkerCandidate> {
    return mapNotNull {
        try { it.toWorkerCandidate() } catch (e: Exception) { null }
    }
}
