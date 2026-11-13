package com.aungthiha.topup.feature.settings.domain.repository

import com.aungthiha.topup.feature.settings.domain.model.AuthState
import kotlinx.coroutines.flow.StateFlow

interface AuthSessionRepository {
    val authState: StateFlow<AuthState>

    /** Ends the session on the server when reachable; the local session is always cleared. */
    suspend fun signOut()
}
