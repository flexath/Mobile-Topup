package com.aungthiha.topup.shared.auth.domain.repository

import com.aungthiha.topup.core.common.result.DataResult
import com.aungthiha.topup.shared.auth.domain.model.AuthError
import com.aungthiha.topup.shared.auth.domain.model.AuthState
import com.aungthiha.topup.shared.auth.domain.model.SignUpResult
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val authState: StateFlow<AuthState>

    suspend fun signIn(email: String, password: String): DataResult<Unit, AuthError>

    suspend fun signUp(email: String, password: String): DataResult<SignUpResult, AuthError>

    /** Ends the session on the server when reachable; the local session is always cleared. */
    suspend fun signOut()
}
