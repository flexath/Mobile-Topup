package com.aungthiha.topup.feature.transactionhistory.presentation.states

import com.aungthiha.topup.shared.transaction.domain.model.Transaction

sealed interface TransactionDetailUiState {
    data object Loading : TransactionDetailUiState
    data object NotFound : TransactionDetailUiState
    data class Ready(val transaction: Transaction) : TransactionDetailUiState
}
