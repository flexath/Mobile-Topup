package com.aungthiha.topup.feature.settings.data.provider

import com.aungthiha.topup.core.common.session.CurrentUserProvider
import com.aungthiha.topup.feature.settings.domain.model.AuthState
import com.aungthiha.topup.feature.settings.domain.repository.AuthSessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal class SessionCurrentUserProvider(
    repository: AuthSessionRepository,
    scope: CoroutineScope,
) : CurrentUserProvider {
    override val userId: StateFlow<String?> = repository.authState
        .map { (it as? AuthState.Authenticated)?.user?.id }
        .distinctUntilChanged()
        .stateIn(scope, SharingStarted.Eagerly, (repository.authState.value as? AuthState.Authenticated)?.user?.id)
}
