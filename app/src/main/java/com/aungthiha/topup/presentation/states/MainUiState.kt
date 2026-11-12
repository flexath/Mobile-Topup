package com.aungthiha.topup.presentation.states

import com.aungthiha.topup.core.datastore.model.AppearanceMode
import com.aungthiha.topup.feature.settings.domain.model.AuthState

data class MainUiState(
    val authState: AuthState = AuthState.Initializing,
    /** `null` until the stored preference is read, so the first frame uses the right theme. */
    val appearanceMode: AppearanceMode? = null,
) {
    val isReady: Boolean get() = authState !is AuthState.Initializing && appearanceMode != null
}
