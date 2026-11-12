package com.aungthiha.topup.feature.auth.domain.model

enum class EmailValidation { VALID, EMPTY, INVALID }

enum class PasswordValidation { VALID, EMPTY, TOO_SHORT }

enum class PasswordConfirmationValidation { VALID, EMPTY, MISMATCH }

/** Client-side checks before calling Supabase Auth (which remains the authority). */
object CredentialsValidator {

    /** Supabase Auth's default minimum password length. */
    const val MIN_PASSWORD_LENGTH = 6

    private val EMAIL_PATTERN = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun normalizeEmail(email: String): String = email.trim()

    fun validateEmail(email: String): EmailValidation {
        val normalized = normalizeEmail(email)
        return when {
            normalized.isEmpty() -> EmailValidation.EMPTY
            !EMAIL_PATTERN.matches(normalized) -> EmailValidation.INVALID
            else -> EmailValidation.VALID
        }
    }

    fun validatePassword(password: String): PasswordValidation = when {
        password.isEmpty() -> PasswordValidation.EMPTY
        password.length < MIN_PASSWORD_LENGTH -> PasswordValidation.TOO_SHORT
        else -> PasswordValidation.VALID
    }

    fun validateConfirmation(password: String, confirmation: String): PasswordConfirmationValidation = when {
        confirmation.isEmpty() -> PasswordConfirmationValidation.EMPTY
        password != confirmation -> PasswordConfirmationValidation.MISMATCH
        else -> PasswordConfirmationValidation.VALID
    }
}
