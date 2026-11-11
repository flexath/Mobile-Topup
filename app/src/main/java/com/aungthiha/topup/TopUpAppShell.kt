package com.aungthiha.topup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aungthiha.topup.core.designsystem.components.navigation.AppFloatingNavBar
import com.aungthiha.topup.core.designsystem.components.navigation.AppNavItem
import com.aungthiha.topup.core.designsystem.components.navigation.AppNavRail
import com.aungthiha.topup.core.designsystem.theme.appColorScheme
import com.aungthiha.topup.core.designsystem.theme.appSpacing
import com.aungthiha.topup.core.designsystem.theme.deviceType
import com.aungthiha.topup.core.designsystem.utils.DeviceWidthClass
import com.aungthiha.topup.feature.auth.presentation.navigation.AuthRoutes
import com.aungthiha.topup.feature.topup.presentation.navigation.TopupRoutes
import com.aungthiha.topup.navigation.AppNavGraph
import com.aungthiha.topup.navigation.TopLevelDestination
import com.aungthiha.topup.navigation.navigateToTopLevel
import com.aungthiha.topup.navigation.showsAppNavigation
import com.aungthiha.topup.navigation.toTopLevelDestination
import com.aungthiha.topup.feature.settings.domain.model.AuthState

private const val SIGNED_OUT_KEY = "signed-out"

/**
 * Session gate + app shell.
 *
 * Each session gets its own navigation state: signing in or out swaps the whole graph, so
 * authenticated destinations are unreachable without a session and a new account never inherits
 * the previous account's back stack.
 */
@Composable
fun TopUpAppShell(
    authState: AuthState,
    onSessionExpired: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sessionKey = when (authState) {
        AuthState.Initializing -> null
        is AuthState.Authenticated -> authState.user.id
        AuthState.Unauthenticated -> SIGNED_OUT_KEY
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.appColorScheme.background),
    ) {
        Crossfade(targetState = sessionKey, label = "session") { key ->
            when (key) {
                null -> Unit
                SIGNED_OUT_KEY -> SignedOutShell(onSessionExpired = onSessionExpired)
                else -> AuthenticatedShell(onSessionExpired = onSessionExpired)
            }
        }
    }
}

@Composable
private fun SignedOutShell(onSessionExpired: () -> Unit) {
    val navController = rememberNavController()
    AppNavGraph(
        navController = navController,
        startDestination = AuthRoutes.GraphRoute,
        onSessionExpired = onSessionExpired,
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    )
}

/**
 * Compact windows get a floating bar over the content; anything wider switches to a rail so the
 * extra width is spent on content instead of a stretched bottom bar.
 */
@Composable
private fun AuthenticatedShell(onSessionExpired: () -> Unit) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentTopLevel = backStackEntry?.destination.toTopLevelDestination()
    val showsNavigation = backStackEntry?.destination.showsAppNavigation()

    val navItems = TopLevelDestination.entries.map { destination ->
        AppNavItem(
            key = destination.name,
            label = destination.label,
            icon = destination.icon,
        )
    }
    val onSelect: (String) -> Unit = { key ->
        navController.navigateToTopLevel(TopLevelDestination.valueOf(key))
    }

    val usesRail = MaterialTheme.deviceType.widthClass >= DeviceWidthClass.Medium
    val spacing = MaterialTheme.appSpacing

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        if (usesRail) {
            Row(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(
                    visible = showsNavigation,
                    enter = fadeIn() + slideInHorizontally { -it },
                    exit = fadeOut() + slideOutHorizontally { -it },
                ) {
                    AppNavRail(
                        items = navItems,
                        selectedKey = currentTopLevel?.name.orEmpty(),
                        onSelect = onSelect,
                        modifier = Modifier.navigationBarsPadding(),
                    )
                }

                AppNavGraph(
                    navController = navController,
                    startDestination = TopupRoutes.GraphRoute,
                    onSessionExpired = onSessionExpired,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
        } else {
            AppNavGraph(
                navController = navController,
                startDestination = TopupRoutes.GraphRoute,
                onSessionExpired = onSessionExpired,
                modifier = Modifier.fillMaxSize(),
            )

            AnimatedVisibility(
                visible = showsNavigation,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = spacing.grand, vertical = spacing.large),
            ) {
                AppFloatingNavBar(
                    items = navItems,
                    selectedKey = currentTopLevel?.name.orEmpty(),
                    onSelect = onSelect,
                )
            }
        }
    }
}
