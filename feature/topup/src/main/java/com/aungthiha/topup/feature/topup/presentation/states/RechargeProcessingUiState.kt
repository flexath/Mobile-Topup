package com.aungthiha.topup.feature.topup.presentation.states

import com.aungthiha.topup.feature.topup.domain.model.RechargeError

sealed interface RechargeProcessingUiState {
    val phoneNumber: String
    val amountMmk: Long

    data class Processing(override val phoneNumber: String, override val amountMmk: Long) : RechargeProcessingUiState

    data class Failed(
        override val phoneNumber: String,
        override val amountMmk: Long,
        val error: RechargeError,
    ) : RechargeProcessingUiState
}

sealed interface RechargeProcessingEvent {
    data class Succeeded(val transactionId: String) : RechargeProcessingEvent
}
