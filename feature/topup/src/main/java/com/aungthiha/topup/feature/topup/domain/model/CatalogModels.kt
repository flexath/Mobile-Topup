package com.aungthiha.topup.feature.topup.domain.model

import com.aungthiha.topup.core.common.result.DataError

/** Backend-published bounds for custom top-up amounts (demo rule enforced by process_recharge). */
data class CustomAmountRule(
    val minMmk: Long,
    val maxMmk: Long,
    val stepMmk: Long,
)

data class TelecomOperator(
    val code: OperatorCode,
    val displayName: String,
    val fullName: String,
    val logoUrl: String?,
    val customAmountRule: CustomAmountRule,
)

enum class CatalogProductType { DENOMINATION, DATA_PACKAGE }

data class CatalogProduct(
    val id: String,
    val sku: String,
    val operatorCode: OperatorCode,
    val type: CatalogProductType,
    val name: String,
    val priceMmk: Long,
    val dataAllowanceMb: Int?,
    val callMinutes: Int?,
    val services: List<String>,
    val validityDays: Int?,
    val isDemo: Boolean,
)

data class OperatorCatalog(
    val operator: TelecomOperator,
    val denominations: List<CatalogProduct>,
    val packages: List<CatalogProduct>,
)

sealed interface CatalogSyncStatus {
    data object Idle : CatalogSyncStatus
    data object Syncing : CatalogSyncStatus
    data object Synced : CatalogSyncStatus
    data class Failed(val error: DataError) : CatalogSyncStatus
}

data class WalletBalance(
    val balanceMmk: Long,
    val syncedAtMillis: Long,
)
