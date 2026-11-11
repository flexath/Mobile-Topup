package com.aungthiha.topup.shared.transaction.domain.usecase

import com.aungthiha.topup.shared.transaction.domain.model.Transaction
import com.aungthiha.topup.shared.transaction.domain.repository.TransactionRepository

class SaveConfirmedTransactionUseCase(private val repository: TransactionRepository) {
    suspend operator fun invoke(transaction: Transaction) = repository.saveConfirmedTransaction(transaction)
}
