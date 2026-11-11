package com.aungthiha.topup.shared.transaction.domain.usecase

import com.aungthiha.topup.core.common.result.DataError
import com.aungthiha.topup.core.common.result.DataResult
import com.aungthiha.topup.shared.transaction.domain.model.Transaction
import com.aungthiha.topup.shared.transaction.domain.model.TransactionQuery
import com.aungthiha.topup.shared.transaction.domain.repository.TransactionPageSync
import com.aungthiha.topup.shared.transaction.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class ObserveTransactionHistoryUseCase(private val repository: TransactionRepository) {
    /** Emits the first [pageCount] pages of the filtered, sorted history from the local cache. */
    operator fun invoke(query: TransactionQuery, pageCount: Int): Flow<List<Transaction>> =
        repository.observeTransactions(query, limit = pageCount.coerceAtLeast(1) * TransactionQuery.PAGE_SIZE)
}

class ObserveTransactionCountUseCase(private val repository: TransactionRepository) {
    operator fun invoke(query: TransactionQuery): Flow<Int> = repository.observeTransactionCount(query)
}

class ObserveTransactionUseCase(private val repository: TransactionRepository) {
    operator fun invoke(transactionId: String): Flow<Transaction?> = repository.observeTransaction(transactionId)
}

class SyncTransactionPageUseCase(private val repository: TransactionRepository) {
    suspend operator fun invoke(query: TransactionQuery, pageIndex: Int): DataResult<TransactionPageSync, DataError> =
        repository.syncPage(query, pageIndex.coerceAtLeast(0))
}

class SaveConfirmedTransactionUseCase(private val repository: TransactionRepository) {
    suspend operator fun invoke(transaction: Transaction) = repository.saveConfirmedTransaction(transaction)
}
