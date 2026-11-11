package com.aungthiha.topup.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
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

private const val TRANSITION_MILLIS = 260
private const val SLIDE_FRACTION = 8

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
        enterTransition = { if (isTabSwitch()) fadeIn(tween(TRANSITION_MILLIS)) else pushEnter() },
        exitTransition = { fadeOut(tween(TRANSITION_MILLIS)) },
        popEnterTransition = { fadeIn(tween(TRANSITION_MILLIS)) },
        popExitTransition = { if (isTabSwitch()) fadeOut(tween(TRANSITION_MILLIS)) else popExit() },
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

private fun AnimatedContentTransitionScope<NavBackStackEntry>.isTabSwitch(): Boolean {
    val from = initialState.destination.toTopLevelDestination()
    val to = targetState.destination.toTopLevelDestination()
    return from != null && to != null && from != to &&
        initialState.destination.showsAppNavigation() && targetState.destination.showsAppNavigation()
}

private fun pushEnter(): EnterTransition =
    fadeIn(tween(TRANSITION_MILLIS)) + slideInHorizontally(tween(TRANSITION_MILLIS)) { it / SLIDE_FRACTION }

private fun popExit(): ExitTransition =
    fadeOut(tween(TRANSITION_MILLIS)) + slideOutHorizontally(tween(TRANSITION_MILLIS)) { it / SLIDE_FRACTION }
