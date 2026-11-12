package com.aungthiha.topup.feature.settings.domain.model

sealed interface AuthState {
    /** The persisted session is still being loaded / refreshed. */
    data object Initializing : AuthState

    data class Authenticated(val user: AuthUser) : AuthState

    data object Unauthenticated : AuthState
}
