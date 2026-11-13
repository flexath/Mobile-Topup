package com.aungthiha.topup.shared.transaction.domain.repository

import com.aungthiha.topup.core.common.result.DataError
import com.aungthiha.topup.core.common.result.DataResult
import com.aungthiha.topup.shared.transaction.domain.model.Transaction
import com.aungthiha.topup.shared.transaction.domain.model.TransactionQuery
import kotlinx.coroutines.flow.Flow

/** Result of synchronizing one remote history page into the local cache. */
data class TransactionPageSync(
    val fetchedCount: Int,
    val hasMore: Boolean,
)

/**
 * The signed-in user's successful transactions. Room is the source of truth for the UI; remote
 * reads are scoped by the session + RLS and never pass a user id.
 */
interface TransactionRepository {
    fun observeTransactions(query: TransactionQuery, limit: Int): Flow<List<Transaction>>

    fun observeTransactionCount(query: TransactionQuery): Flow<Int>

    fun observeTransaction(transactionId: String): Flow<Transaction?>

    /** Fetches page [pageIndex] (0-based, [TransactionQuery.PAGE_SIZE] rows) and caches it. */
    suspend fun syncPage(query: TransactionQuery, pageIndex: Int): DataResult<TransactionPageSync, DataError>

    /** Caches a transaction confirmed by the recharge backend. */
    suspend fun saveConfirmedTransaction(transaction: Transaction)

    suspend fun clearCache()
}
