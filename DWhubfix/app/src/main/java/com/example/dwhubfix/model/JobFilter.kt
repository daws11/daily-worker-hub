package com.example.dwhubfix.model

/**
 * Job Filters data class
 * Used for filtering jobs in worker dashboard
 */
data class JobFilters(
    val categories: Set<String> = emptySet(),
    val wageRange: String = "Semua",
    val distanceRange: String = "Semua",
    val timeSlot: String = "Semua"
)
