package com.aungthiha.topup.feature.topup.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/** androidTest-only Room database so [CatalogDao] tests do not depend on `:app`. */
@Database(
    entities = [OperatorEntity::class, CatalogProductEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(CatalogConverters::class)
abstract class CatalogDatabase : RoomDatabase() {
    abstract fun catalogDao(): CatalogDao
}
