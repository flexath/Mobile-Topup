package com.aungthiha.topup.feature.settings.presentation.states

import com.aungthiha.topup.core.datastore.model.AppearanceMode

data class SettingsUiState(
    val appearanceMode: AppearanceMode = AppearanceMode.SYSTEM,
    val email: String? = null,
    val isLogoutConfirmationVisible: Boolean = false,
    val isSigningOut: Boolean = false,
)
