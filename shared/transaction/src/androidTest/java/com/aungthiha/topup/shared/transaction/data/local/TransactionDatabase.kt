package com.aungthiha.topup.shared.transaction.data.local

import com.aungthiha.topup.shared.transaction.data.local.converter.StringListConverter
import com.aungthiha.topup.shared.transaction.data.local.dao.TransactionDao
import com.aungthiha.topup.shared.transaction.data.local.entity.TransactionEntity
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/** androidTest-only Room database so [TransactionDao] tests do not depend on `:app`. */
@Database(
    entities = [TransactionEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(StringListConverter::class)
abstract class TransactionDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
}
