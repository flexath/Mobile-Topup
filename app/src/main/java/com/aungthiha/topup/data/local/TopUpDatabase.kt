package com.aungthiha.topup.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aungthiha.topup.feature.topup.data.local.converter.CatalogConverters
import com.aungthiha.topup.feature.topup.data.local.dao.CatalogDao
import com.aungthiha.topup.feature.topup.data.local.entity.CatalogProductEntity
import com.aungthiha.topup.feature.topup.data.local.entity.OperatorEntity
import com.aungthiha.topup.feature.topup.data.local.dao.OperatorPrefixDao
import com.aungthiha.topup.feature.topup.data.local.entity.OperatorPrefixEntity
import com.aungthiha.topup.shared.transaction.data.local.dao.TransactionDao
import com.aungthiha.topup.shared.transaction.data.local.entity.TransactionEntity

/**
 * Single app Room database. Catalog tables are shared cache (kept on sign-out);
 * transaction rows are cleared via [com.aungthiha.topup.shared.transaction.domain.repository.TransactionRepository.clearCache].
 *
 * Encryption: release builds encrypt the entire database file with SQLCipher via
 * [net.zetetic.database.sqlcipher.SupportOpenHelperFactory] in app DI (`openHelperFactory`).
 * Debug builds use a plain Room database. All tables are covered at the file level.
 */
@Database(
    entities = [
        OperatorEntity::class,
        CatalogProductEntity::class,
        OperatorPrefixEntity::class,
        TransactionEntity::class,
    ],
    version = 1,
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
