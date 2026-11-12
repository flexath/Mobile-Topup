package com.aungthiha.topup.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aungthiha.topup.feature.topup.data.local.CatalogConverters
import com.aungthiha.topup.feature.topup.data.local.CatalogDao
import com.aungthiha.topup.feature.topup.data.local.CatalogProductEntity
import com.aungthiha.topup.feature.topup.data.local.OperatorEntity
import com.aungthiha.topup.feature.topup.data.local.OperatorPrefixDao
import com.aungthiha.topup.feature.topup.data.local.OperatorPrefixEntity
import com.aungthiha.topup.shared.transaction.data.local.TransactionDao
import com.aungthiha.topup.shared.transaction.data.local.TransactionEntity

/**
 * Single app Room database. Catalog tables are shared cache (kept on sign-out);
 * transaction rows are cleared via [com.aungthiha.topup.shared.transaction.domain.repository.TransactionRepository.clearCache].
 */
@Database(
    entities = [
        OperatorEntity::class,
        CatalogProductEntity::class,
        OperatorPrefixEntity::class,
        TransactionEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
@TypeConverters(CatalogConverters::class)
abstract class TopUpDatabase : RoomDatabase() {
    abstract fun catalogDao(): CatalogDao
    abstract fun operatorPrefixDao(): OperatorPrefixDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        const val NAME = "topup.db"
    }
}
