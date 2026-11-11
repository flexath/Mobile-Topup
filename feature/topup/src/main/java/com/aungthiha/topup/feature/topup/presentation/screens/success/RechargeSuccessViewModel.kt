package com.aungthiha.topup.feature.topup.presentation.screens.success

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.aungthiha.topup.feature.topup.presentation.navigation.TopupRoutes
import com.aungthiha.topup.shared.transaction.domain.model.Transaction
import com.aungthiha.topup.shared.transaction.domain.usecase.ObserveTransactionUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface RechargeSuccessUiState {
    data object Loading : RechargeSuccessUiState
    data class Ready(val transaction: Transaction) : RechargeSuccessUiState
}

/** Shows the backend-confirmed transaction that ProcessRechargeUseCase cached before navigating here. */
class RechargeSuccessViewModel(
    savedStateHandle: SavedStateHandle,
    observeTransaction: ObserveTransactionUseCase,
) : ViewModel() {

    private val transactionId = savedStateHandle.toRoute<TopupRoutes.RechargeSuccess>().transactionId

    val uiState: StateFlow<RechargeSuccessUiState> = observeTransaction(transactionId)
        .map { transaction -> transaction?.let(RechargeSuccessUiState::Ready) ?: RechargeSuccessUiState.Loading }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RechargeSuccessUiState.Loading)
}
