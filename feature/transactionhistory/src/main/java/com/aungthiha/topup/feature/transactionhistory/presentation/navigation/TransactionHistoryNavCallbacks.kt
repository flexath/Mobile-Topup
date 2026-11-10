package com.aungthiha.topup.feature.transactionhistory.presentation.navigation

sealed interface TransactionHistoryNavCallbacks {
    data object NavigateUp : TransactionHistoryNavCallbacks

    sealed interface TransactionHistoryScreen : TransactionHistoryNavCallbacks {
        data object ToTopup : TransactionHistoryScreen
    }
}
