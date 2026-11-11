package com.aungthiha.topup.feature.transactionhistory.presentation.navigation

import com.aungthiha.topup.core.common.navigation.AppRoutes
import kotlinx.serialization.Serializable

sealed class TransactionHistoryRoutes : AppRoutes {
    @Serializable
    data object GraphRoute : TransactionHistoryRoutes()

    @Serializable
    data object TransactionHistoryScreen : TransactionHistoryRoutes()

    @Serializable
    data class TransactionDetailScreen(val transactionId: String) : TransactionHistoryRoutes()
}
