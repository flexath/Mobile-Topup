package com.aungthiha.topup.feature.topup.presentation.states

/** Wallet overview: balance plus the entry point into the recharge flow. */
data class TopupUiState(
    val wallet: WalletUiState = WalletUiState(),
    /** Every supported operator, enriched with catalog names and logos once the catalog is cached. */
    val operators: List<OperatorUiModel> = emptyList(),
    val isOnline: Boolean = true,
)
