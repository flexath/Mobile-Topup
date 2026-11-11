package com.aungthiha.topup.feature.topup.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogDao {

    @Transaction
    @Query("SELECT * FROM operators ORDER BY sort_order ASC, code ASC")
    fun observeCatalog(): Flow<List<OperatorWithProducts>>

    @Query("SELECT COUNT(*) FROM operators")
    suspend fun operatorCount(): Int

    @Insert
    suspend fun insertOperators(operators: List<OperatorEntity>)

    @Insert
    suspend fun insertProducts(products: List<CatalogProductEntity>)

    @Query("DELETE FROM operators")
    suspend fun deleteAllOperators()

    /** Atomically swaps the whole catalog (products cascade with their operator). */
    @Transaction
    suspend fun replaceCatalog(operators: List<OperatorEntity>, products: List<CatalogProductEntity>) {
        deleteAllOperators()
        insertOperators(operators)
        insertProducts(products)
    }
}
