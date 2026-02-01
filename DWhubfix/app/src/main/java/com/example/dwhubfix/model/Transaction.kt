package com.example.dwhubfix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: String,
    @SerialName("job_application_id") val jobApplicationId: String,
    @SerialName("worker_id") val workerId: String,
    @SerialName("business_id") val businessId: String,
    
    // Amounts in IDR
    @SerialName("gross_amount") val grossAmount: Int,
    @SerialName("platform_commission") val platformCommission: Int,
    @SerialName("business_commission") val businessCommission: Int,
    @SerialName("worker_commission") val workerCommission: Int,
    @SerialName("net_worker_amount") val netWorkerAmount: Int,
    
    @SerialName("transaction_type") val transactionType: String, // job_payment, withdrawal, bonus, penalty
    val status: String, // pending, processing, completed, failed, refunded
    
    @SerialName("payment_method") val paymentMethod: String? = null,
    @SerialName("payment_reference") val paymentReference: String? = null,
    
    @SerialName("processed_at") val processedAt: String? = null,
    @SerialName("completed_at") val completedAt: String? = null,
    
    val notes: String? = null,
    val metadata: Map<String, String>? = null,
    
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String? = null
)

/**
 * Helper extension to get transaction type display text
 */
fun Transaction.getTypeText(): String = when (transactionType) {
    "job_payment" -> "Pembayaran Job"
    "withdrawal" -> "Penarikan Dana"
    "bonus" -> "Bonus"
    "penalty" -> "Penalti"
    else -> transactionType
}

/**
 * Helper extension to get status display text
 */
fun Transaction.getStatusText(): String = when (status) {
    "pending" -> "Menunggu"
    "processing" -> "Diproses"
    "completed" -> "Selesai"
    "failed" -> "Gagal"
    "refunded" -> "Dikembalikan"
    else -> status
}

/**
 * Helper extension to check if transaction is completed
 */
fun Transaction.isCompleted(): Boolean = status == "completed"
