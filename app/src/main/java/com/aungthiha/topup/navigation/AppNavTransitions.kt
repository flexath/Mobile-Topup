package com.aungthiha.topup.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavBackStackEntry

private const val TRANSITION_MILLIS = 260
private const val SLIDE_FRACTION = 8

/** Default enter: fade for tab switches, fade + subtle slide for push navigation. */
fun AnimatedContentTransitionScope<NavBackStackEntry>.appEnterTransition(): EnterTransition =
    if (isTabSwitch()) fadeIn(tween(TRANSITION_MILLIS)) else pushEnter()

/** Default exit: fade out so the incoming screen remains the focus. */
fun AnimatedContentTransitionScope<NavBackStackEntry>.appExitTransition(): ExitTransition =
    fadeOut(tween(TRANSITION_MILLIS))

/** Default pop-enter: fade the restored destination back in. */
fun AnimatedContentTransitionScope<NavBackStackEntry>.appPopEnterTransition(): EnterTransition =
    fadeIn(tween(TRANSITION_MILLIS))

/** Default pop-exit: fade for tab switches, fade + subtle slide for back navigation. */
fun AnimatedContentTransitionScope<NavBackStackEntry>.appPopExitTransition(): ExitTransition =
    if (isTabSwitch()) fadeOut(tween(TRANSITION_MILLIS)) else popExit()

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
