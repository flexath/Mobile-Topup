package com.aungthiha.topup.feature.settings.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aungthiha.topup.core.datastore.model.AppearanceMode
import com.aungthiha.topup.feature.settings.domain.ObserveAppearanceModeUseCase
import com.aungthiha.topup.feature.settings.domain.SetAppearanceModeUseCase
import com.aungthiha.topup.shared.auth.domain.model.AuthState
import com.aungthiha.topup.shared.auth.domain.usecase.ObserveAuthStateUseCase
import com.aungthiha.topup.shared.auth.domain.usecase.SignOutUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val appearanceMode: AppearanceMode = AppearanceMode.SYSTEM,
    val email: String? = null,
    val isLogoutConfirmationVisible: Boolean = false,
    val isSigningOut: Boolean = false,
)

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
