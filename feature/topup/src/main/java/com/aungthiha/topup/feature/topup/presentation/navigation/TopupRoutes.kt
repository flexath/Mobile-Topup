package com.aungthiha.topup.feature.topup.presentation.navigation

import com.aungthiha.topup.core.common.navigation.AppRoutes
import kotlinx.serialization.Serializable

sealed class TopupRoutes : AppRoutes {
    @Serializable
    data object GraphRoute : TopupRoutes()

    @Serializable
    data object TopupScreen : TopupRoutes()
}
