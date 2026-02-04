package com.example.dwhubfix.domain.model

/**
 * Constants for Job-Worker Matching Algorithm
 *
 * Contains all thresholds, weights, and scores used in the matching system.
 * These values are based on the matching-algorithm.md specification.
 */
object MatchingConstants {

    // ========================================
    // DISTANCE THRESHOLDS (in kilometers)
    // ========================================
    const val DISTANCE_VERY_CLOSE_MAX = 2.0
    const val DISTANCE_CLOSE_MAX = 5.0
    const val DISTANCE_MEDIUM_MAX = 10.0
    const val DISTANCE_FAR_MAX = 20.0
    const val DISTANCE_VERY_FAR_MAX = 30.0

    // ========================================
    // DISTANCE SCORES
    // ========================================
    const val SCORE_DISTANCE_VERY_CLOSE = 30.0
    const val SCORE_DISTANCE_CLOSE = 25.0
    const val SCORE_DISTANCE_MEDIUM = 15.0
    const val SCORE_DISTANCE_FAR = 5.0
    const val SCORE_DISTANCE_VERY_FAR = 2.0
    const val SCORE_DISTANCE_OUT_OF_RANGE = 0.0

    // ========================================
    // SCORE WEIGHTS
    // ========================================
    const val WEIGHT_DISTANCE = 30.0
    const val WEIGHT_SKILL = 25.0
    const val WEIGHT_RATING = 20.0
    const val WEIGHT_RELIABILITY = 15.0
    const val WEIGHT_URGENCY = 10.0

    // Maximum possible total score
    const val MAX_TOTAL_SCORE = 100.0

    // ========================================
    // COMPLIANCE RULES (21 Days Rule - PP 35/2021)
    // ========================================
    const val MAX_DAYS_PER_CLIENT = 20  // Maximum days per client in 30 days
    const val COMPLIANCE_WINDOW_DAYS = 30  // Look back period in days

    // ========================================
    // MATCH QUALITY THRESHOLDS
    // ========================================
    const val EXCELLENT_MATCH_THRESHOLD = 85.0
    const val GOOD_MATCH_THRESHOLD = 70.0
    const val ACCEPTABLE_MATCH_THRESHOLD = 50.0

    // ========================================
    // RATING SCORE CALCULATION
    // ========================================
    const val MAX_RATING = 5.0
    const val RATING_SCORE_WEIGHT = 20.0

    // ========================================
    // RELIABILITY SCORE CALCULATION
    // ========================================
    const val RELIABILITY_SCORE_WEIGHT = 15.0
    const val DEFAULT_NO_SHOW_RATE = 0.05  // 5% default no-show rate

    // ========================================
    // URGENCY SCORE
    // ========================================
    const val URGENCY_SCORE_WEIGHT = 10.0
}

/**
 * Map-related constants
 */
object MapConstants {
    const val DEFAULT_MAP_ZOOM = 15.0
    const val DEFAULT_MAP_CENTER_LAT = -8.5069  // Bali center
    const val DEFAULT_MAP_CENTER_LNG = 115.2625
    const val MIN_MAP_ZOOM = 10.0
    const val MAX_MAP_ZOOM = 20.0

    // Location accuracy thresholds (in meters)
    const val LOCATION_ACCURACY_EXCELLENT = 10.0
    const val LOCATION_ACCURACY_GOOD = 50.0
    const val LOCATION_ACCURACY_FAIR = 100.0
}

/**
 * Session-related constants
 */
object SessionConstants {
    const val PREF_NAME = "user_session"
    const val KEY_ACCESS_TOKEN = "access_token"
    const val KEY_REFRESH_TOKEN = "refresh_token"
    const val KEY_USER_ID = "user_id"
    const val KEY_SELECTED_ROLE = "selected_role"
    const val KEY_CURRENT_STEP = "current_step"
    const val KEY_PENDING_ROLE = "pending_role"
    const val KEY_PHONE_NUMBER = "phone_number"

    // Token expiration buffer (5 minutes)
    const val TOKEN_EXPIRATION_BUFFER_MS = 5 * 60 * 1000L
}

/**
 * Job status constants
 */
object JobStatus {
    const val OPEN = "open"
    const val FILLED = "filled"
    const val IN_PROGRESS = "in_progress"
    const val COMPLETED = "completed"
    const val CANCELLED = "cancelled"
}

/**
 * Job application status constants
 */
object ApplicationStatus {
    const val PENDING = "pending"
    const val ACCEPTED = "accepted"
    const val REJECTED = "rejected"
    const val COMPLETED = "completed"
    const val CANCELLED = "cancelled"
}

/**
 * User role constants
 */
object UserRole {
    const val WORKER = "worker"
    const val BUSINESS = "business"
}
