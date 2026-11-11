package com.aungthiha.topup.feature.auth.domain.repository

import com.aungthiha.topup.core.common.result.DataResult
import com.aungthiha.topup.feature.auth.domain.model.AuthError

interface AuthRepository {
    suspend fun signIn(email: String, password: String): DataResult<Unit, AuthError>

    suspend fun signUp(email: String, password: String): DataResult<Unit, AuthError>
}
