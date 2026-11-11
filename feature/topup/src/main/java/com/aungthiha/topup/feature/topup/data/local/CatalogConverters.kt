package com.aungthiha.topup.feature.topup.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

class CatalogConverters {
    @TypeConverter
    fun fromList(values: List<String>): String = Json.encodeToString(values)

    @TypeConverter
    fun toList(value: String): List<String> = Json.decodeFromString(value)
}
