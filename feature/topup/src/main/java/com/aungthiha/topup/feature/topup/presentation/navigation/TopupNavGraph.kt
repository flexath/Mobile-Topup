package com.aungthiha.topup.feature.topup.presentation.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import com.aungthiha.topup.feature.topup.presentation.screens.processing.rechargeProcessingScreen
import com.aungthiha.topup.feature.topup.presentation.screens.recharge.rechargeScreen
import com.aungthiha.topup.feature.topup.presentation.screens.success.rechargeSuccessScreen
import com.aungthiha.topup.feature.topup.presentation.screens.topup.topupScreen

fun NavGraphBuilder.topupNavGraph(
    modifier: Modifier = Modifier,
    onNavRoute: (TopupNavCallbacks) -> Unit,
) {
    navigation<TopupRoutes.GraphRoute>(
        startDestination = TopupRoutes.TopupScreen,
    ) {
        topupScreen(modifier = modifier, onNavRoute = onNavRoute)
        rechargeScreen(modifier = modifier, onNavRoute = onNavRoute)
        rechargeProcessingScreen(modifier = modifier, onNavRoute = onNavRoute)
        rechargeSuccessScreen(modifier = modifier, onNavRoute = onNavRoute)
    }
}
