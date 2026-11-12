package com.aungthiha.topup.feature.auth.presentation.states

import com.aungthiha.topup.feature.auth.domain.model.AuthError
import com.aungthiha.topup.feature.auth.domain.model.CredentialsValidator
import com.aungthiha.topup.feature.auth.domain.model.EmailValidation
import com.aungthiha.topup.feature.auth.domain.model.PasswordConfirmationValidation
import com.aungthiha.topup.feature.auth.domain.model.PasswordValidation

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val showValidation: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: AuthError? = null,
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
