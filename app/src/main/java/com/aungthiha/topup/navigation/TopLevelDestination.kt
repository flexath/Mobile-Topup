package com.aungthiha.topup.navigation

import androidx.annotation.DrawableRes
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import com.aungthiha.topup.core.common.navigation.AppRoutes
import com.aungthiha.topup.core.designsystem.R
import com.aungthiha.topup.feature.topup.presentation.navigation.TopupRoutes
import com.aungthiha.topup.feature.transactionhistory.presentation.navigation.TransactionHistoryRoutes
import kotlin.reflect.KClass

/**
 * Destinations reachable from the app-level navigation chrome.
 *
 * Each owns a feature graph so switching tabs preserves that feature's back stack.
 */
enum class TopLevelDestination(
    val label: String,
    @DrawableRes val icon: Int,
    val graphRoute: AppRoutes,
    val graphRouteClass: KClass<out AppRoutes>,
    val rootRouteClass: KClass<out AppRoutes>,
) {
    Topup(
        label = "Topup",
        icon = R.drawable.ic_topup,
        graphRoute = TopupRoutes.GraphRoute,
        graphRouteClass = TopupRoutes.GraphRoute::class,
        rootRouteClass = TopupRoutes.TopupScreen::class,
    ),
    History(
        label = "History",
        icon = R.drawable.ic_transaction_history,
        graphRoute = TransactionHistoryRoutes.GraphRoute,
        graphRouteClass = TransactionHistoryRoutes.GraphRoute::class,
        rootRouteClass = TransactionHistoryRoutes.TransactionHistoryScreen::class,
    ),
}

/** The tab a destination belongs to, or `null` while a full-screen flow is open. */
fun NavDestination?.toTopLevelDestination(): TopLevelDestination? {
    val destination = this ?: return null
    return TopLevelDestination.entries.firstOrNull { topLevel ->
        destination.hierarchy.any { it.hasRoute(topLevel.graphRouteClass) }
    }
}

/** Whether the app-level navigation chrome should be visible for the current destination. */
fun NavDestination?.showsAppNavigation(): Boolean {
    val destination = this ?: return false
    return TopLevelDestination.entries.any { topLevel ->
        destination.hasRoute(topLevel.rootRouteClass)
    }
}

/**
 * Keeps TopupScreen as Android's visible task root. Anchoring at the nested Topup graph would pop
 * its TopupScreen child, leaving History as the first screen system Back can reach.
 */
fun NavHostController.navigateToTopLevel(destination: TopLevelDestination) {
    if (currentDestination.toTopLevelDestination() == destination) return

    navigate(destination.graphRoute) {
        popUpTo<TopupRoutes.TopupScreen> { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
