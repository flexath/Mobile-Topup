package com.aungthiha.topup.feature.auth.presentation.components

import androidx.annotation.StringRes
import com.aungthiha.topup.feature.auth.R
import com.aungthiha.topup.feature.auth.domain.model.AuthError
import com.aungthiha.topup.feature.auth.domain.model.EmailValidation
import com.aungthiha.topup.feature.auth.domain.model.PasswordConfirmationValidation
import com.aungthiha.topup.feature.auth.domain.model.PasswordValidation

@StringRes
internal fun AuthError.messageRes(): Int = when (this) {
    AuthError.INVALID_CREDENTIALS -> R.string.auth_error_invalid_credentials
    AuthError.EMAIL_NOT_CONFIRMED -> R.string.auth_error_email_not_confirmed
    AuthError.EMAIL_ALREADY_REGISTERED -> R.string.auth_error_email_registered
    AuthError.WEAK_PASSWORD -> R.string.auth_error_weak_password
    AuthError.INVALID_EMAIL -> R.string.auth_error_invalid_email
    AuthError.SIGNUP_DISABLED -> R.string.auth_error_signup_disabled
    AuthError.RATE_LIMITED -> R.string.auth_error_rate_limited
    AuthError.NO_CONNECTION -> R.string.auth_error_no_connection
    AuthError.UNKNOWN -> R.string.auth_error_unknown
}

@StringRes
internal fun EmailValidation.messageRes(): Int? = when (this) {
    EmailValidation.VALID -> null
    EmailValidation.EMPTY -> R.string.auth_error_email_empty
    EmailValidation.INVALID -> R.string.auth_error_email_invalid
}

@StringRes
internal fun PasswordValidation.messageRes(): Int? = when (this) {
    PasswordValidation.VALID -> null
    PasswordValidation.EMPTY -> R.string.auth_error_password_empty
    PasswordValidation.TOO_SHORT -> R.string.auth_error_password_short
}

@StringRes
internal fun PasswordConfirmationValidation.messageRes(): Int? = when (this) {
    PasswordConfirmationValidation.VALID -> null
    PasswordConfirmationValidation.EMPTY -> R.string.auth_error_confirm_empty
    PasswordConfirmationValidation.MISMATCH -> R.string.auth_error_confirm_mismatch
}
