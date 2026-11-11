package com.aungthiha.topup.feature.topup.presentation.states

import com.aungthiha.topup.shared.transaction.domain.model.Transaction

sealed interface RechargeSuccessUiState {
    data object Loading : RechargeSuccessUiState
    data class Ready(val transaction: Transaction) : RechargeSuccessUiState
}
