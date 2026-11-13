package com.aungthiha.topup.feature.transactionhistory.presentation.states

import com.aungthiha.topup.shared.transaction.domain.model.TransactionQuery

data class HistorySyncState(
    val isSyncing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val lastSyncFailed: Boolean = false,
    /** Whether the last remote page for [query] was full (more rows may exist on the server). */
    val remoteHasMore: Boolean = false,
    val query: TransactionQuery? = null,
)
