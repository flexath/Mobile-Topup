package com.aungthiha.topup.feature.topup.data.local

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "operators")
data class OperatorEntity(
    @PrimaryKey
    @ColumnInfo(name = "code") val code: String,
    @ColumnInfo(name = "display_name") val displayName: String,
    @ColumnInfo(name = "full_name") val fullName: String,
    @ColumnInfo(name = "logo_url") val logoUrl: String?,
    @ColumnInfo(name = "custom_amount_min_mmk") val customAmountMinMmk: Long,
    @ColumnInfo(name = "custom_amount_max_mmk") val customAmountMaxMmk: Long,
    @ColumnInfo(name = "custom_amount_step_mmk") val customAmountStepMmk: Long,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
)

@Entity(
    tableName = "catalog_products",
    foreignKeys = [
        ForeignKey(
            entity = OperatorEntity::class,
            parentColumns = ["code"],
            childColumns = ["operator_code"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["operator_code", "product_type", "sort_order"]),
        Index(value = ["sku"], unique = true),
    ],
)
data class CatalogProductEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "sku") val sku: String,
    @ColumnInfo(name = "operator_code") val operatorCode: String,
    @ColumnInfo(name = "product_type") val productType: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "price_mmk") val priceMmk: Long,
    @ColumnInfo(name = "data_allowance_mb") val dataAllowanceMb: Int?,
    @ColumnInfo(name = "call_minutes") val callMinutes: Int?,
    @ColumnInfo(name = "services") val services: List<String>,
    @ColumnInfo(name = "validity_days") val validityDays: Int?,
    @ColumnInfo(name = "is_demo") val isDemo: Boolean,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
)

data class OperatorWithProducts(
    @Embedded val operator: OperatorEntity,
    @Relation(parentColumn = "code", entityColumn = "operator_code")
    val products: List<CatalogProductEntity>,
)
