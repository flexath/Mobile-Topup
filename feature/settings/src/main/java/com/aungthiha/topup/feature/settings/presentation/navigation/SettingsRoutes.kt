package com.aungthiha.topup.feature.settings.presentation.navigation

import com.aungthiha.topup.core.common.navigation.AppRoutes
import kotlinx.serialization.Serializable

sealed class SettingsRoutes : AppRoutes {
    @Serializable
    data object GraphRoute : SettingsRoutes()

    @Serializable
    data object SettingsScreen : SettingsRoutes()
}
