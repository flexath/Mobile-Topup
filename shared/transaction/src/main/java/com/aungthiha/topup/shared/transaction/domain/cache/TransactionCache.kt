package com.aungthiha.topup.shared.transaction.domain.cache

import com.aungthiha.topup.shared.transaction.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionCache {
    fun observeNewestFirst(
        userId: String,
        search: String,
        filterByOperator: Boolean,
        operatorCodes: List<String>,
        fromMillis: Long,
        toMillis: Long,
        limit: Int,
    ): Flow<List<Transaction>>

    fun observeOldestFirst(
        userId: String,
        search: String,
        filterByOperator: Boolean,
        operatorCodes: List<String>,
        fromMillis: Long,
        toMillis: Long,
        limit: Int,
    ): Flow<List<Transaction>>

    fun observeCount(
        userId: String,
        search: String,
        filterByOperator: Boolean,
        operatorCodes: List<String>,
        fromMillis: Long,
        toMillis: Long,
    ): Flow<Int>

    fun observeById(userId: String, transactionId: String): Flow<Transaction?>

    suspend fun upsertAll(userId: String, transactions: List<Transaction>)

    suspend fun clear()
}
