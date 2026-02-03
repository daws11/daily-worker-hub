package com.example.dwhubfix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * BUSINESS FLOW MODELS
 * 
 * Based on business-model.md:
 * 1. Rate Bali (UMK-based pricing)
 * 2. Closed-Loop Wallet (Deposit system)
 * 3. Platform Commission (6%)
 * 4. Worker Selection (View candidates with scores)
 */

@Serializable
data class BusinessStats(
    val activeShiftsToday: Int = 0,
    val workersHiredThisWeek: Int = 0,
    val totalSpendingThisMonth: Double = 0.0,
    val pendingPatches: Int = 0 // Number of workers awaiting acceptance
)

@Serializable
data class WorkerCandidate(
    val workerId: String,
    val workerName: String,
    val avatarUrl: String?,
    val skills: List<String>,
    val rating: Double = 0.0,
    val matchScore: Double = 0.0, // From matching algorithm
    val distance: String = "0.0 km",
    val reliabilityScore: Double = 0.0,
    val noShowRate: Double = 0.0,
    val isCompliant: Boolean = true,
    val totalShiftsCompleted: Int = 0,
    val hourlyRate: Double? = null
)

@Serializable
data class BusinessWallet(
    val balance: Double = 0.0,
    val currency: String = "IDR",
    val isVerified: Boolean = false,
    val lastTransaction: Transaction? = null,
    val availableForWithdrawal: Double = 0.0
)

@Serializable
data class JobPostingForm(
    val title: String = "",
    val description: String = "",
    val workerCount: Int = 1,
    val startTime: String = "08:00",
    val endTime: String = "17:00",
    val shiftDate: String = LocalDate.now().toString().substring(0, 10),
    val wage: Double = 150000.0,
    val category: String = "Server",
    val location: String = "Bali, Indonesia",
    val isUrgent: Boolean = false,
    val useRateBali: Boolean = true, // If true, wage is auto-calculated based on UMK
    val region: String = "Badung" // For UMK calculation
)

/**
 * Rate Bali Wage Suggestions
 * Reference: business-model.md Section 2.1
 * 
 * Based on UMK 2025:
 * - Badung: Rp 3.534.339 → Rp 168.302/day (÷21 days for daily workers)
 * - Denpasar: Rp 3.298.117 → Rp 157.053/day
 * - Gianyar: Rp 3.119.080 → Rp 148.527/day
 * 
 * Daily Worker Calculation: UMK ÷ 21 days (as per PP 35/2021 for PKHL)
 */
@Serializable
data class RateBaliSuggestion(
    val region: String, // Badung, Denpasar, Gianyar
    val umk: Double,
    val dailyWage: Double, // UMK ÷ 21 days
    val description: String
)

// Keep old models for compatibility
// Already defined: Job, UserProfile, WorkerProfile, BusinessProfile, JobApplication, etc.
