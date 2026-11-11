package com.aungthiha.topup.shared.auth.domain.usecase

import com.aungthiha.topup.core.common.result.DataResult
import com.aungthiha.topup.shared.auth.domain.model.AuthError
import com.aungthiha.topup.shared.auth.domain.model.CredentialsValidator
import com.aungthiha.topup.shared.auth.domain.model.EmailValidation
import com.aungthiha.topup.shared.auth.domain.model.PasswordValidation
import com.aungthiha.topup.shared.auth.domain.repository.AuthRepository

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
