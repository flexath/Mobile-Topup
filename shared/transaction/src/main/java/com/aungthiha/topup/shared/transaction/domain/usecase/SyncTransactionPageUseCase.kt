package com.aungthiha.topup.shared.transaction.domain.usecase

import com.aungthiha.topup.core.common.result.DataError
import com.aungthiha.topup.core.common.result.DataResult
import com.aungthiha.topup.shared.transaction.domain.model.TransactionQuery
import com.aungthiha.topup.shared.transaction.domain.repository.TransactionPageSync
import com.aungthiha.topup.shared.transaction.domain.repository.TransactionRepository

class SyncTransactionPageUseCase(private val repository: TransactionRepository) {
    suspend operator fun invoke(query: TransactionQuery, pageIndex: Int): DataResult<TransactionPageSync, DataError> =
        repository.syncPage(query, pageIndex.coerceAtLeast(0))
}
