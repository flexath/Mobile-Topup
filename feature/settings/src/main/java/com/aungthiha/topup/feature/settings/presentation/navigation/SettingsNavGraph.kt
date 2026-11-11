package com.aungthiha.topup.feature.settings.presentation.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import com.aungthiha.topup.feature.settings.presentation.screens.settingsScreen

fun NavGraphBuilder.settingsNavGraph(
    appVersion: String,
    modifier: Modifier = Modifier,
    onNavRoute: (SettingsNavCallbacks) -> Unit,
) {
    navigation<SettingsRoutes.GraphRoute>(startDestination = SettingsRoutes.SettingsScreen) {
        settingsScreen(appVersion = appVersion, modifier = modifier, onNavRoute = onNavRoute)
    }
}
