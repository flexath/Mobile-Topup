package com.aungthiha.topup.feature.transactionhistory.presentation.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import com.aungthiha.topup.feature.transactionhistory.presentation.screens.detail.transactionDetailScreen
import com.aungthiha.topup.feature.transactionhistory.presentation.screens.history.transactionHistoryScreen

fun NavGraphBuilder.transactionHistoryNavGraph(
    modifier: Modifier = Modifier,
    onNavRoute: (TransactionHistoryNavCallbacks) -> Unit,
) {
    navigation<TransactionHistoryRoutes.GraphRoute>(
        startDestination = TransactionHistoryRoutes.TransactionHistoryScreen,
    ) {
        transactionHistoryScreen(modifier = modifier, onNavRoute = onNavRoute)
        transactionDetailScreen(modifier = modifier, onNavRoute = onNavRoute)
    }
}
