package com.aungthiha.topup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aungthiha.topup.core.datastore.model.AppearanceMode
import com.aungthiha.topup.feature.settings.domain.ObserveAppearanceModeUseCase
import com.aungthiha.topup.shared.auth.domain.model.AuthState
import com.aungthiha.topup.shared.auth.domain.usecase.ObserveAuthStateUseCase
import com.aungthiha.topup.shared.auth.domain.usecase.SignOutUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MainUiState(
    val authState: AuthState = AuthState.Initializing,
    /** `null` until the stored preference is read, so the first frame uses the right theme. */
    val appearanceMode: AppearanceMode? = null,
) {
    val isReady: Boolean get() = authState !is AuthState.Initializing && appearanceMode != null
}

class MainViewModel(
    observeAuthState: ObserveAuthStateUseCase,
    observeAppearanceMode: ObserveAppearanceModeUseCase,
    private val signOut: SignOutUseCase,
) : ViewModel() {

    val uiState: StateFlow<MainUiState> = combine(observeAuthState(), observeAppearanceMode()) { auth, appearance ->
        MainUiState(authState = auth, appearanceMode = appearance)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, MainUiState())

    /** The backend rejected the session (e.g. revoked or expired refresh token). */
    fun onSessionExpired() {
        viewModelScope.launch { signOut() }
    }
}
