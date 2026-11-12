package com.aungthiha.topup.feature.transactionhistory.presentation.states

import com.aungthiha.topup.feature.transactionhistory.presentation.model.HistoryFilterState
import com.aungthiha.topup.shared.transaction.domain.model.Transaction

data class TransactionHistoryUiState(
    val filters: HistoryFilterState = HistoryFilterState(),
    val transactions: List<Transaction> = emptyList(),
    val matchingCachedCount: Int = 0,
    val isInitialLoad: Boolean = true,
    val isSyncing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val showSyncWarning: Boolean = false,
    val isOnline: Boolean = true,
    val canLoadMore: Boolean = false,
)
