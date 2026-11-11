package com.aungthiha.topup.feature.auth.presentation.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import com.aungthiha.topup.feature.auth.presentation.screens.login.loginScreen
import com.aungthiha.topup.feature.auth.presentation.screens.register.registerScreen

fun NavGraphBuilder.authNavGraph(
    modifier: Modifier = Modifier,
    onNavRoute: (AuthNavCallbacks) -> Unit,
) {
    navigation<AuthRoutes.GraphRoute>(
        startDestination = AuthRoutes.LoginScreen,
    ) {
        loginScreen(modifier = modifier, onNavRoute = onNavRoute)
        registerScreen(modifier = modifier, onNavRoute = onNavRoute)
    }
}
