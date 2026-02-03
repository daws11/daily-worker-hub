package com.example.dwhubfix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WalletBalance(
    val id: String,
    @SerialName("user_id") val userId: String,
    val balance: Double = 0.0,
    @SerialName("currency") val currency: String = "IDR",
    @SerialName("available_balance") val availableBalance: Double = 0.0, // After subtracting pending withdrawals
    @SerialName("pending_withdrawal") val pendingWithdrawal: Double = 0.0,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)
