package com.aungthiha.topup.feature.auth.presentation.screens.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aungthiha.topup.core.common.result.DataResult
import com.aungthiha.topup.feature.auth.domain.model.EmailValidation
import com.aungthiha.topup.feature.auth.domain.model.PasswordValidation
import com.aungthiha.topup.feature.auth.domain.usecase.SignInUseCase
import com.aungthiha.topup.feature.auth.presentation.states.LoginUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * On success nothing is emitted: the session change moves the app to the authenticated shell.
 * The password is kept in memory only (never in [SavedStateHandle]).
 */
class LoginViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val signIn: SignInUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState(email = savedStateHandle[KEY_EMAIL] ?: ""))
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        savedStateHandle[KEY_EMAIL] = value
        _uiState.update { it.copy(email = value, error = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onSubmit() {
        val state = _uiState.value
        if (state.isSubmitting) return
        if (state.emailValidation != EmailValidation.VALID || state.passwordValidation != PasswordValidation.VALID) {
            _uiState.update { it.copy(showValidation = true) }
            return
        }
        _uiState.update { it.copy(isSubmitting = true, showValidation = true, error = null) }
        viewModelScope.launch {
            val result = signIn(state.email, state.password)
            _uiState.update {
                it.copy(
                    isSubmitting = false,
                    error = (result as? DataResult.Error)?.error,
                )
            }
        }
    }

    private companion object {
        const val KEY_EMAIL = "login_email"
    }
}
