package com.aungthiha.topup.shared.auth.domain.usecase

import com.aungthiha.topup.shared.auth.domain.model.AuthState
import com.aungthiha.topup.shared.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.StateFlow

class ObserveAuthStateUseCase(private val repository: AuthRepository) {
    operator fun invoke(): StateFlow<AuthState> = repository.authState
}
