package com.aungthiha.topup.feature.topup.presentation.states

data class WalletUiState(
    val balanceMmk: Long? = null,
    val isRefreshing: Boolean = false,
    /** The last refresh failed; the shown balance is the cached value. */
    val isStale: Boolean = false,
)
