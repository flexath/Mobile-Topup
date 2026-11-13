package com.aungthiha.topup.feature.auth.presentation.navigation

import com.aungthiha.topup.core.common.navigation.AppRoutes
import kotlinx.serialization.Serializable

sealed class AuthRoutes : AppRoutes {
    @Serializable
    data object GraphRoute : AuthRoutes()

    @Serializable
    data object LoginScreen : AuthRoutes()

    @Serializable
    data object RegisterScreen : AuthRoutes()
}
