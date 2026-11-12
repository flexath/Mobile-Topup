package com.aungthiha.topup.shared.transaction.data.local.cache

import com.aungthiha.topup.shared.transaction.data.local.dao.TransactionDao
import com.aungthiha.topup.shared.transaction.data.mapper.toDomain
import com.aungthiha.topup.shared.transaction.data.mapper.toEntity
import com.aungthiha.topup.shared.transaction.domain.cache.TransactionCache
import com.aungthiha.topup.shared.transaction.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class TransactionCacheImpl(
    private val dao: TransactionDao,
) : TransactionCache {

    override fun observeNewestFirst(
        userId: String,
        search: String,
        filterByOperator: Boolean,
        operatorCodes: List<String>,
        fromMillis: Long,
        toMillis: Long,
        limit: Int,
    ): Flow<List<Transaction>> =
        dao.observeNewestFirst(userId, search, filterByOperator, operatorCodes, fromMillis, toMillis, limit)
            .map { rows -> rows.map { it.toDomain() } }

    override fun observeOldestFirst(
        userId: String,
        search: String,
        filterByOperator: Boolean,
        operatorCodes: List<String>,
        fromMillis: Long,
        toMillis: Long,
        limit: Int,
    ): Flow<List<Transaction>> =
        dao.observeOldestFirst(userId, search, filterByOperator, operatorCodes, fromMillis, toMillis, limit)
            .map { rows -> rows.map { it.toDomain() } }

    override fun observeCount(
        userId: String,
        search: String,
        filterByOperator: Boolean,
        operatorCodes: List<String>,
        fromMillis: Long,
        toMillis: Long,
    ): Flow<Int> =
        dao.observeCount(userId, search, filterByOperator, operatorCodes, fromMillis, toMillis)

    override fun observeById(userId: String, transactionId: String): Flow<Transaction?> =
        dao.observeById(userId, transactionId).map { it?.toDomain() }

    override suspend fun upsertAll(userId: String, transactions: List<Transaction>) {
        dao.upsertAll(transactions.map { it.toEntity(userId) })
    }

    override suspend fun clear() {
        dao.deleteAll()
    }
}
