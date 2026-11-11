package com.aungthiha.topup.shared.transaction.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.serialization.json.Json

@Database(
    entities = [TransactionEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(StringListConverter::class)
internal abstract class TransactionDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        const val NAME = "topup_transactions.db"
    }
}

internal class StringListConverter {
    @TypeConverter
    fun fromList(values: List<String>): String = Json.encodeToString(values)

    @TypeConverter
    fun toList(value: String): List<String> = Json.decodeFromString(value)
}
