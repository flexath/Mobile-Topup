package com.aungthiha.topup.shared.transaction.domain.usecase

import com.aungthiha.topup.shared.transaction.domain.model.Transaction
import com.aungthiha.topup.shared.transaction.domain.model.TransactionQuery
import com.aungthiha.topup.shared.transaction.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class ObserveTransactionHistoryUseCase(private val repository: TransactionRepository) {
    /** Emits the first [pageCount] pages of the filtered, sorted history from the local cache. */
    operator fun invoke(query: TransactionQuery, pageCount: Int): Flow<List<Transaction>> =
        repository.observeTransactions(query, limit = pageCount.coerceAtLeast(1) * TransactionQuery.PAGE_SIZE)
}
