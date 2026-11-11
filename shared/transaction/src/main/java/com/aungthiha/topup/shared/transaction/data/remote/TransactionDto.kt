package com.aungthiha.topup.shared.transaction.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class TransactionDto(
    @SerialName("id") val id: String,
    @SerialName("phone_number") val phoneNumber: String,
    @SerialName("operator_code") val operatorCode: String,
    @SerialName("operator_name") val operatorName: String,
    @SerialName("operator_logo_bucket") val operatorLogoBucket: String,
    @SerialName("operator_logo_path") val operatorLogoPath: String,
    @SerialName("product_id") val productId: String? = null,
    @SerialName("product_sku") val productSku: String? = null,
    @SerialName("product_type") val productType: String,
    @SerialName("product_name") val productName: String,
    @SerialName("data_allowance_mb") val dataAllowanceMb: Int? = null,
    @SerialName("call_minutes") val callMinutes: Int? = null,
    @SerialName("services") val services: List<String> = emptyList(),
    @SerialName("validity_days") val validityDays: Int? = null,
    @SerialName("amount_mmk") val amountMmk: Long,
    @SerialName("currency") val currency: String,
    @SerialName("status") val status: String,
    @SerialName("balance_after_mmk") val balanceAfterMmk: Long,
    @SerialName("created_at") val createdAt: String,
) {
    companion object {
        /** Explicit column list: never pulls internal columns such as the idempotency fingerprint. */
        val COLUMNS = listOf(
            "id", "phone_number", "operator_code", "operator_name", "operator_logo_bucket",
            "operator_logo_path", "product_id", "product_sku", "product_type", "product_name",
            "data_allowance_mb", "call_minutes", "services", "validity_days", "amount_mmk",
            "currency", "status", "balance_after_mmk", "created_at",
        )
    }
}
