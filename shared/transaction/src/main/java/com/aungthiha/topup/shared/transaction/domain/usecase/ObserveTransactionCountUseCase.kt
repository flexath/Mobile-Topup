package com.aungthiha.topup.shared.transaction.domain.usecase

import com.aungthiha.topup.shared.transaction.domain.model.TransactionQuery
import com.aungthiha.topup.shared.transaction.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class ObserveTransactionCountUseCase(private val repository: TransactionRepository) {
    operator fun invoke(query: TransactionQuery): Flow<Int> = repository.observeTransactionCount(query)
}
