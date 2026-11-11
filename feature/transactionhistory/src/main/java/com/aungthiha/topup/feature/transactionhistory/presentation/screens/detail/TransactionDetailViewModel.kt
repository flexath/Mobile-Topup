package com.aungthiha.topup.feature.transactionhistory.presentation.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.aungthiha.topup.feature.transactionhistory.presentation.navigation.TransactionHistoryRoutes
import com.aungthiha.topup.shared.transaction.domain.model.Transaction
import com.aungthiha.topup.shared.transaction.domain.usecase.ObserveTransactionUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface TransactionDetailUiState {
    data object Loading : TransactionDetailUiState
    data object NotFound : TransactionDetailUiState
    data class Ready(val transaction: Transaction) : TransactionDetailUiState
}

/** Reads the persisted purchase snapshot, never the live catalog. */
class TransactionDetailViewModel(
    savedStateHandle: SavedStateHandle,
    observeTransaction: ObserveTransactionUseCase,
) : ViewModel() {

    private val transactionId = savedStateHandle.toRoute<TransactionHistoryRoutes.TransactionDetailScreen>().transactionId

    val uiState: StateFlow<TransactionDetailUiState> = observeTransaction(transactionId)
        .map { transaction -> transaction?.let(TransactionDetailUiState::Ready) ?: TransactionDetailUiState.NotFound }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TransactionDetailUiState.Loading)
}
