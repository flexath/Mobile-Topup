package com.aungthiha.topup.feature.topup.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "operator_prefixes")
data class OperatorPrefixEntity(
    @PrimaryKey
    @ColumnInfo(name = "prefix") val prefix: String,
    @ColumnInfo(name = "operator_code") val operatorCode: String,
)
