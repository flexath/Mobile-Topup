package com.aungthiha.topup.shared.transaction.domain.model

enum class RechargeProductType {
    DENOMINATION,
    DATA_PACKAGE,
    CUSTOM_AMOUNT,
}

enum class TransactionStatus { SUCCESS }

data class TransactionOperator(
    val code: String,
    val name: String,
    val logoUrl: String?,
)

/** Package details as they were when the recharge happened (not the live catalog). */
data class PurchasedProduct(
    val id: String?,
    val sku: String?,
    val type: RechargeProductType,
    val name: String,
    val dataAllowanceMb: Int?,
    val callMinutes: Int?,
    val services: List<String>,
    val validityDays: Int?,
)

data class Transaction(
    val id: String,
    val phoneNumber: String,
    val operator: TransactionOperator,
    val product: PurchasedProduct,
    val amountMmk: Long,
    val currency: String,
    val status: TransactionStatus,
    val balanceAfterMmk: Long,
    val createdAtMillis: Long,
)
