package com.aungthiha.topup.shared.auth.domain.usecase

import com.aungthiha.topup.core.common.result.DataResult
import com.aungthiha.topup.core.common.session.UserScopedDataCleaner
import com.aungthiha.topup.shared.auth.domain.model.AuthError
import com.aungthiha.topup.shared.auth.domain.model.AuthState
import com.aungthiha.topup.shared.auth.domain.model.CredentialsValidator
import com.aungthiha.topup.shared.auth.domain.model.EmailValidation
import com.aungthiha.topup.shared.auth.domain.model.PasswordValidation
import com.aungthiha.topup.shared.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.StateFlow

class ObserveAuthStateUseCase(private val repository: AuthRepository) {
    operator fun invoke(): StateFlow<AuthState> = repository.authState
}

class SignInUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): DataResult<Unit, AuthError> {
        if (CredentialsValidator.validateEmail(email) != EmailValidation.VALID) {
            return DataResult.Error(AuthError.INVALID_EMAIL)
        }
        if (password.isEmpty()) return DataResult.Error(AuthError.INVALID_CREDENTIALS)
        return repository.signIn(CredentialsValidator.normalizeEmail(email), password)
    }
}

class SignUpUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): DataResult<Unit, AuthError> {
        if (CredentialsValidator.validateEmail(email) != EmailValidation.VALID) {
            return DataResult.Error(AuthError.INVALID_EMAIL)
        }
        if (CredentialsValidator.validatePassword(password) != PasswordValidation.VALID) {
            return DataResult.Error(AuthError.WEAK_PASSWORD)
        }
        return repository.signUp(CredentialsValidator.normalizeEmail(email), password)
    }
}

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
