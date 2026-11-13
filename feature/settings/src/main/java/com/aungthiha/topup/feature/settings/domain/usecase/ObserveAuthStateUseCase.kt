package com.aungthiha.topup.feature.settings.domain.usecase

import com.aungthiha.topup.feature.settings.domain.model.AuthState
import com.aungthiha.topup.feature.settings.domain.repository.AuthSessionRepository
import kotlinx.coroutines.flow.StateFlow

class ObserveAuthStateUseCase(private val repository: AuthSessionRepository) {
    operator fun invoke(): StateFlow<AuthState> = repository.authState
}
