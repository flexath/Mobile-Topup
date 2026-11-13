package com.aungthiha.topup.feature.settings.domain.usecase

import com.aungthiha.topup.core.common.session.UserScopedDataCleaner
import com.aungthiha.topup.feature.settings.domain.repository.AuthSessionRepository

/**
 * Signs out and wipes every user-scoped local cache (wallet balance, transaction history) so the
 * next account never sees the previous account's data. The shared catalog cache is kept.
 */
class SignOutUseCase(
    private val repository: AuthSessionRepository,
    private val cleaners: List<UserScopedDataCleaner>,
) {
    suspend operator fun invoke() {
        cleaners.forEach { cleaner -> runCatching { cleaner.clearUserData() } }
        repository.signOut()
    }
}
