package com.aungthiha.topup.shared.transaction.domain.usecase

import com.aungthiha.topup.shared.transaction.domain.model.Transaction
import com.aungthiha.topup.shared.transaction.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class ObserveTransactionUseCase(private val repository: TransactionRepository) {
    operator fun invoke(transactionId: String): Flow<Transaction?> = repository.observeTransaction(transactionId)
}
