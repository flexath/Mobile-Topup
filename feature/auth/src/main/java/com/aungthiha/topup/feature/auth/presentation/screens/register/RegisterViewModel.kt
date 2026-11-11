package com.aungthiha.topup.feature.auth.presentation.screens.register

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aungthiha.topup.core.common.result.DataResult
import com.aungthiha.topup.shared.auth.domain.model.AuthError
import com.aungthiha.topup.shared.auth.domain.model.CredentialsValidator
import com.aungthiha.topup.shared.auth.domain.model.EmailValidation
import com.aungthiha.topup.shared.auth.domain.model.PasswordConfirmationValidation
import com.aungthiha.topup.shared.auth.domain.model.PasswordValidation
import com.aungthiha.topup.shared.auth.domain.model.SignUpResult
import com.aungthiha.topup.shared.auth.domain.usecase.SignUpUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val showValidation: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: AuthError? = null,
    /** Set when Supabase requires email confirmation before the first sign-in. */
    val confirmationSentTo: String? = null,
) {
    val emailValidation: EmailValidation get() = CredentialsValidator.validateEmail(email)
    val passwordValidation: PasswordValidation get() = CredentialsValidator.validatePassword(password)
    val confirmationValidation: PasswordConfirmationValidation
        get() = CredentialsValidator.validateConfirmation(password, confirmPassword)
    val isFormValid: Boolean
        get() = emailValidation == EmailValidation.VALID &&
            passwordValidation == PasswordValidation.VALID &&
            confirmationValidation == PasswordConfirmationValidation.VALID
}

class RegisterViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val signUp: SignUpUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        RegisterUiState(
            email = savedStateHandle[KEY_EMAIL] ?: "",
            confirmationSentTo = savedStateHandle[KEY_CONFIRMATION_EMAIL],
        ),
    )
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        savedStateHandle[KEY_EMAIL] = value
        _uiState.update { it.copy(email = value, error = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value, error = null) }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onSubmit() {
        val state = _uiState.value
        if (state.isSubmitting) return
        if (!state.isFormValid) {
            _uiState.update { it.copy(showValidation = true) }
            return
        }
        _uiState.update { it.copy(isSubmitting = true, showValidation = true, error = null) }
        viewModelScope.launch {
            when (val result = signUp(state.email, state.password)) {
                is DataResult.Success -> {
                    val confirmation = (result.data as? SignUpResult.ConfirmationRequired)?.email
                    savedStateHandle[KEY_CONFIRMATION_EMAIL] = confirmation
                    _uiState.update {
                        it.copy(isSubmitting = false, confirmationSentTo = confirmation, password = "", confirmPassword = "")
                    }
                }
                is DataResult.Error -> _uiState.update { it.copy(isSubmitting = false, error = result.error) }
                else -> _uiState.update { it.copy(isSubmitting = false) }
            }
        }
    }

    private companion object {
        const val KEY_EMAIL = "register_email"
        const val KEY_CONFIRMATION_EMAIL = "register_confirmation_email"
    }
}
