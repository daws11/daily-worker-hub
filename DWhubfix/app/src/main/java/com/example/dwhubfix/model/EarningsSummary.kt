package com.example.dwhubfix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EarningsSummary(
    @SerialName("totalEarnings") val totalEarnings: Int,
    @SerialName("pendingEarnings") val pendingEarnings: Int,
    @SerialName("availableBalance") val availableBalance: Int,
    @SerialName("totalJobs") val totalJobs: Int,
    @SerialName("totalCommission") val totalCommission: Int,
    val transactions: List<Transaction> = emptyList()
)



/**
 * Helper function to format currency in IDR
 */
fun formatCurrency(amount: Int): String {
    return String.format("%,d", amount).replace(',', '.')
}

/**
 * Helper extension to get formatted total earnings
 */
fun EarningsSummary.getFormattedTotalEarnings(): String = formatCurrency(totalEarnings)

/**
 * Helper extension to get formatted pending earnings
 */
fun EarningsSummary.getFormattedPendingEarnings(): String = formatCurrency(pendingEarnings)

/**
 * Helper extension to get formatted available balance
 */
fun EarningsSummary.getFormattedAvailableBalance(): String = formatCurrency(availableBalance)
