package com.aungthiha.topup.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.aungthiha.topup.BuildConfig
import com.aungthiha.topup.core.common.navigation.AppRoutes
import com.aungthiha.topup.feature.auth.presentation.navigation.AuthNavCallbacks
import com.aungthiha.topup.feature.auth.presentation.navigation.AuthRoutes
import com.aungthiha.topup.feature.auth.presentation.navigation.authNavGraph
import com.aungthiha.topup.feature.settings.presentation.navigation.SettingsNavCallbacks
import com.aungthiha.topup.feature.settings.presentation.navigation.SettingsRoutes
import com.aungthiha.topup.feature.settings.presentation.navigation.settingsNavGraph
import com.aungthiha.topup.feature.topup.presentation.navigation.TopupNavCallbacks
import com.aungthiha.topup.feature.topup.presentation.navigation.TopupRoutes
import com.aungthiha.topup.feature.topup.presentation.navigation.topupNavGraph
import com.aungthiha.topup.feature.transactionhistory.presentation.navigation.TransactionHistoryNavCallbacks
import com.aungthiha.topup.feature.transactionhistory.presentation.navigation.TransactionHistoryRoutes
import com.aungthiha.topup.feature.transactionhistory.presentation.navigation.transactionHistoryNavGraph

/** The only cross-feature router: features emit callbacks, this maps them to navigation. */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: AppRoutes,
    onSessionExpired: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination,
        enterTransition = { appEnterTransition() },
        exitTransition = { appExitTransition() },
        popEnterTransition = { appPopEnterTransition() },
        popExitTransition = { appPopExitTransition() },
    ) {
        authNavGraph(
            modifier = Modifier.fillMaxSize(),
            onNavRoute = {
                when (it) {
                    AuthNavCallbacks.NavigateUp -> navController.navigateUp()
                    AuthNavCallbacks.LoginScreen.ToRegister -> navController.navigate(AuthRoutes.RegisterScreen) {
                        launchSingleTop = true
                    }
                    AuthNavCallbacks.RegisterScreen.ToLogin -> {
                        if (!navController.popBackStack<AuthRoutes.LoginScreen>(inclusive = false)) {
                            navController.navigate(AuthRoutes.LoginScreen) { launchSingleTop = true }
                        }
                    }
                }
            },
        )

        topupNavGraph(
            modifier = Modifier.fillMaxSize(),
            onNavRoute = {
                when (it) {
                    TopupNavCallbacks.NavigateUp -> navController.navigateUp()
                    TopupNavCallbacks.TopupScreen.ToSettings -> navController.navigate(SettingsRoutes.GraphRoute) {
                        launchSingleTop = true
                    }
                    TopupNavCallbacks.TopupScreen.ToRecharge -> navController.navigate(TopupRoutes.RechargeScreen) {
                        launchSingleTop = true
                    }
                    is TopupNavCallbacks.RechargeScreen.ToProcessing -> navController.navigate(it.route) {
                        launchSingleTop = true
                    }
                    is TopupNavCallbacks.ProcessingScreen.ToSuccess ->
                        navController.navigate(TopupRoutes.RechargeSuccess(it.transactionId)) {
                            popUpTo<TopupRoutes.RechargeProcessing> { inclusive = true }
                        }
                    TopupNavCallbacks.ProcessingScreen.BackToRecharge -> {
                        if (!navController.popBackStack<TopupRoutes.RechargeScreen>(inclusive = false)) {
                            navController.popBackStack<TopupRoutes.TopupScreen>(inclusive = false)
                        }
                    }
                    TopupNavCallbacks.SuccessScreen.BackToTopup ->
                        navController.popBackStack<TopupRoutes.TopupScreen>(inclusive = false)
                    TopupNavCallbacks.ProcessingScreen.SessionExpired -> onSessionExpired()
                    TopupNavCallbacks.SuccessScreen.ToHistory -> {
                        // Drop the finished flow first so the Topup tab restores to its root screen.
                        navController.popBackStack<TopupRoutes.TopupScreen>(inclusive = false)
                        navController.navigateToTopLevel(TopLevelDestination.History)
                    }
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
                    is TransactionHistoryNavCallbacks.TransactionHistoryScreen.ToDetail ->
                        navController.navigate(TransactionHistoryRoutes.TransactionDetailScreen(it.transactionId)) {
                            launchSingleTop = true
                        }
                }
            },
        )

        settingsNavGraph(
            appVersion = BuildConfig.VERSION_NAME,
            modifier = Modifier.fillMaxSize(),
            onNavRoute = {
                when (it) {
                    SettingsNavCallbacks.NavigateUp -> navController.navigateUp()
                }
            },
        )
    }
}
