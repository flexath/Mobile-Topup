package com.aungthiha.topup.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.aungthiha.topup.core.common.navigation.AppRoutes
import com.aungthiha.topup.feature.topup.presentation.navigation.TopupNavCallbacks
import com.aungthiha.topup.feature.topup.presentation.navigation.topupNavGraph
import com.aungthiha.topup.feature.transactionhistory.presentation.navigation.TransactionHistoryNavCallbacks
import com.aungthiha.topup.feature.transactionhistory.presentation.navigation.transactionHistoryNavGraph

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: AppRoutes,
    modifier: Modifier = Modifier,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination,
    ) {
        topupNavGraph(
            modifier = Modifier.fillMaxSize(),
            onNavRoute = {
                when (it) {
                    TopupNavCallbacks.NavigateUp -> navController.navigateUp()
                    TopupNavCallbacks.TopupScreen.ToHistory ->
                        navController.navigateToTopLevel(TopLevelDestination.History)
                }
            },
        )

        transactionHistoryNavGraph(
            modifier = Modifier.fillMaxSize(),
            onNavRoute = {
                when (it) {
                    TransactionHistoryNavCallbacks.NavigateUp -> navController.navigateUp()
                    TransactionHistoryNavCallbacks.TransactionHistoryScreen.ToTopup ->
                        navController.navigateToTopLevel(TopLevelDestination.Topup)
                }
            },
        )
    }
}
