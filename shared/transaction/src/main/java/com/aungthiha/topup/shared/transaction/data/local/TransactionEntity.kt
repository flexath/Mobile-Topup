package com.aungthiha.topup.shared.transaction.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["user_id", "created_at", "id"]),
        Index(value = ["user_id", "operator_code"]),
    ],
)
internal data class TransactionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "phone_number") val phoneNumber: String,
    @ColumnInfo(name = "operator_code") val operatorCode: String,
    @ColumnInfo(name = "operator_name") val operatorName: String,
    @ColumnInfo(name = "operator_logo_url") val operatorLogoUrl: String?,
    @ColumnInfo(name = "product_id") val productId: String?,
    @ColumnInfo(name = "product_sku") val productSku: String?,
    @ColumnInfo(name = "product_type") val productType: String,
    @ColumnInfo(name = "product_name") val productName: String,
    @ColumnInfo(name = "data_allowance_mb") val dataAllowanceMb: Int?,
    @ColumnInfo(name = "call_minutes") val callMinutes: Int?,
    @ColumnInfo(name = "services") val services: List<String>,
    @ColumnInfo(name = "validity_days") val validityDays: Int?,
    @ColumnInfo(name = "amount_mmk") val amountMmk: Long,
    @ColumnInfo(name = "currency") val currency: String,
    @ColumnInfo(name = "status") val status: String,
    @ColumnInfo(name = "balance_after_mmk") val balanceAfterMmk: Long,
    @ColumnInfo(name = "created_at") val createdAtMillis: Long,
)
