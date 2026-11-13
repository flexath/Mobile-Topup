package com.aungthiha.topup.feature.settings.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aungthiha.topup.core.datastore.model.AppearanceMode
import com.aungthiha.topup.feature.settings.domain.usecase.ObserveAppearanceModeUseCase
import com.aungthiha.topup.feature.settings.domain.usecase.SetAppearanceModeUseCase
import com.aungthiha.topup.feature.settings.domain.model.AuthState
import com.aungthiha.topup.feature.settings.domain.usecase.ObserveAuthStateUseCase
import com.aungthiha.topup.feature.settings.domain.usecase.SignOutUseCase
import com.aungthiha.topup.feature.settings.presentation.states.SettingsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    observeAppearanceMode: ObserveAppearanceModeUseCase,
    private val setAppearanceMode: SetAppearanceModeUseCase,
    observeAuthState: ObserveAuthStateUseCase,
    private val signOut: SignOutUseCase,
) : ViewModel() {

    private val dialogState = MutableStateFlow(SettingsUiState())

    val uiState: StateFlow<SettingsUiState> = combine(
        observeAppearanceMode(),
        observeAuthState(),
        dialogState,
    ) { mode, auth, dialog ->
        dialog.copy(
            appearanceMode = mode,
            email = (auth as? AuthState.Authenticated)?.user?.email,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun onAppearanceSelected(mode: AppearanceMode) {
        viewModelScope.launch { setAppearanceMode(mode) }
    }

    fun onLogoutClick() = dialogState.update { it.copy(isLogoutConfirmationVisible = true) }

    fun onDismissLogout() = dialogState.update { it.copy(isLogoutConfirmationVisible = false) }

    fun onConfirmLogout() {
        if (dialogState.value.isSigningOut) return
        dialogState.update { it.copy(isLogoutConfirmationVisible = false, isSigningOut = true) }
        viewModelScope.launch { signOut() }
    }
}
