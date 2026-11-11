package com.aungthiha.topup.shared.auth.domain.usecase

import com.aungthiha.topup.core.common.session.UserScopedDataCleaner
import com.aungthiha.topup.shared.auth.domain.repository.AuthRepository

/**
 * Signs out and wipes every user-scoped local cache (wallet balance, transaction history) so the
 * next account never sees the previous account's data. The shared catalog cache is kept.
 */
class SignOutUseCase(
    private val repository: AuthRepository,
    private val cleaners: List<UserScopedDataCleaner>,
) {
    suspend operator fun invoke() {
        cleaners.forEach { cleaner -> runCatching { cleaner.clearUserData() } }
        repository.signOut()
    }
}
