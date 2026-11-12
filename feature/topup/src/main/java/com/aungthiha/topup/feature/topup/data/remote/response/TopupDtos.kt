package com.aungthiha.topup.feature.topup.data.remote.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class OperatorDto(
    @SerialName("code") val code: String,
    @SerialName("name") val name: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("logo_bucket") val logoBucket: String,
    @SerialName("logo_path") val logoPath: String,
    @SerialName("custom_amount_min_mmk") val customAmountMinMmk: Long,
    @SerialName("custom_amount_max_mmk") val customAmountMaxMmk: Long,
    @SerialName("custom_amount_step_mmk") val customAmountStepMmk: Long,
    @SerialName("sort_order") val sortOrder: Int,
    @SerialName("catalog_products") val products: List<CatalogProductDto> = emptyList(),
) {
    companion object {
        /** One request returns every operator with its embedded products (the unified catalog). */
        const val UNIFIED_CATALOG_SELECT =
            "code,name,display_name,logo_bucket,logo_path," +
                "custom_amount_min_mmk,custom_amount_max_mmk,custom_amount_step_mmk,sort_order," +
                "catalog_products(id,sku,operator_code,product_type,name,price_mmk,data_allowance_mb," +
                "call_minutes,services,validity_days,is_demo,sort_order)"
    }
}

@Serializable
internal data class CatalogProductDto(
    @SerialName("id") val id: String,
    @SerialName("sku") val sku: String,
    @SerialName("operator_code") val operatorCode: String,
    @SerialName("product_type") val productType: String,
    @SerialName("name") val name: String,
    @SerialName("price_mmk") val priceMmk: Long,
    @SerialName("data_allowance_mb") val dataAllowanceMb: Int? = null,
    @SerialName("call_minutes") val callMinutes: Int? = null,
    @SerialName("services") val services: List<String> = emptyList(),
    @SerialName("validity_days") val validityDays: Int? = null,
    @SerialName("is_demo") val isDemo: Boolean = true,
    @SerialName("sort_order") val sortOrder: Int = 0,
)

@Serializable
internal data class OperatorPrefixDto(
    @SerialName("prefix") val prefix: String,
    @SerialName("operator_code") val operatorCode: String,
)

@Serializable
internal data class WalletDto(
    @SerialName("balance_mmk") val balanceMmk: Long,
    @SerialName("currency") val currency: String,
)

/** Body for the `process-recharge` Edge Function. Deliberately carries no user id or price. */
@Serializable
internal data class RechargeRequestDto(
    @SerialName("phone_number") val phoneNumber: String,
    @SerialName("operator_code") val operatorCode: String,
    @SerialName("product_id") val productId: String? = null,
    @SerialName("custom_amount_mmk") val customAmountMmk: Long? = null,
    @SerialName("idempotency_key") val idempotencyKey: String,
)

@Serializable
internal data class RechargeSuccessDto(
    @SerialName("success") val success: Boolean,
    @SerialName("replayed") val replayed: Boolean = false,
    @SerialName("transaction") val transaction: RechargeTransactionDto,
    @SerialName("wallet") val wallet: WalletDto,
)

@Serializable
internal data class RechargeTransactionDto(
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
)

@Serializable
internal data class RechargeFailureDto(
    @SerialName("success") val success: Boolean = false,
    @SerialName("error") val error: RechargeFailureBodyDto,
)

@Serializable
internal data class RechargeFailureBodyDto(
    @SerialName("code") val code: String,
    @SerialName("message") val message: String? = null,
    @SerialName("wallet_balance_mmk") val walletBalanceMmk: Long? = null,
    @SerialName("amount_mmk") val amountMmk: Long? = null,
)
