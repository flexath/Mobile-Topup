package com.aungthiha.topup.shared.auth.domain.model

import com.aungthiha.topup.core.common.result.DataError

data class AuthUser(
    val id: String,
    val email: String?,
)

sealed interface AuthState {
    /** The persisted session is still being loaded / refreshed. */
    data object Initializing : AuthState

    data class Authenticated(val user: AuthUser) : AuthState

    data object Unauthenticated : AuthState
}

sealed interface SignUpResult {
    data object SignedIn : SignUpResult

    /** The project requires email confirmation before the first sign-in. */
    data class ConfirmationRequired(val email: String) : SignUpResult
}

enum class AuthError : DataError {
    INVALID_CREDENTIALS,
    EMAIL_NOT_CONFIRMED,
    EMAIL_ALREADY_REGISTERED,
    WEAK_PASSWORD,
    INVALID_EMAIL,
    SIGNUP_DISABLED,
    RATE_LIMITED,
    NO_CONNECTION,
    UNKNOWN,
}
