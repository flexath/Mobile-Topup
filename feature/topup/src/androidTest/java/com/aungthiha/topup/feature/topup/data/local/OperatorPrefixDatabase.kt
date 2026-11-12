package com.aungthiha.topup.feature.topup.data.local

import com.aungthiha.topup.feature.topup.data.local.dao.OperatorPrefixDao
import com.aungthiha.topup.feature.topup.data.local.entity.OperatorPrefixEntity
import androidx.room.Database
import androidx.room.RoomDatabase

/** androidTest-only Room database so [OperatorPrefixDao] tests do not depend on `:app`. */
@Database(
    entities = [OperatorPrefixEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class OperatorPrefixDatabase : RoomDatabase() {
    abstract fun operatorPrefixDao(): OperatorPrefixDao
}
