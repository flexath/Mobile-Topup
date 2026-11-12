package com.aungthiha.topup.feature.topup.data.local.dao

import com.aungthiha.topup.feature.topup.data.local.entity.OperatorPrefixEntity
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface OperatorPrefixDao {

    @Query("SELECT * FROM operator_prefixes ORDER BY prefix ASC")
    fun observeAll(): Flow<List<OperatorPrefixEntity>>

    @Query("SELECT * FROM operator_prefixes ORDER BY prefix ASC")
    suspend fun getAll(): List<OperatorPrefixEntity>

    @Query("SELECT COUNT(*) FROM operator_prefixes")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(prefixes: List<OperatorPrefixEntity>)

    @Query("DELETE FROM operator_prefixes")
    suspend fun deleteAll()

    /** Atomically swaps the whole prefix cache. */
    @Transaction
    suspend fun replaceAll(prefixes: List<OperatorPrefixEntity>) {
        deleteAll()
        insertAll(prefixes)
    }
}
