package com.aungthiha.topup.feature.topup.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.serialization.json.Json

/** Shared, non-personal catalog cache. Intentionally kept on sign-out. */
@Database(
    entities = [OperatorEntity::class, CatalogProductEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(CatalogConverters::class)
internal abstract class CatalogDatabase : RoomDatabase() {
    abstract fun catalogDao(): CatalogDao

    companion object {
        const val NAME = "topup_catalog.db"
    }
}

internal class CatalogConverters {
    @TypeConverter
    fun fromList(values: List<String>): String = Json.encodeToString(values)

    @TypeConverter
    fun toList(value: String): List<String> = Json.decodeFromString(value)
}
