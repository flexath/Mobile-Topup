package com.aungthiha.topup.feature.topup.presentation.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import com.aungthiha.topup.feature.topup.presentation.screens.topupScreen

fun NavGraphBuilder.topupNavGraph(
    modifier: Modifier = Modifier,
    onNavRoute: (TopupNavCallbacks) -> Unit,
) {
    navigation<TopupRoutes.GraphRoute>(
        startDestination = TopupRoutes.TopupScreen,
    ) {
        topupScreen(
            modifier = modifier,
            onNavRoute = onNavRoute,
        )
    }
}
