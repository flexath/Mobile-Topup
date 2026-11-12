package com.aungthiha.topup.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aungthiha.topup.feature.settings.domain.usecase.ObserveAppearanceModeUseCase
import com.aungthiha.topup.feature.settings.domain.usecase.ObserveAuthStateUseCase
import com.aungthiha.topup.feature.settings.domain.usecase.SignOutUseCase
import com.aungthiha.topup.presentation.states.MainUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
