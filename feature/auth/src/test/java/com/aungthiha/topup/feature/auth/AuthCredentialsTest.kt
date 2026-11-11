package com.aungthiha.topup.feature.auth

import com.aungthiha.topup.core.common.result.DataResult
import com.aungthiha.topup.feature.auth.domain.model.AuthError
import com.aungthiha.topup.feature.auth.domain.model.CredentialsValidator
import com.aungthiha.topup.feature.auth.domain.model.EmailValidation
import com.aungthiha.topup.feature.auth.domain.model.PasswordConfirmationValidation
import com.aungthiha.topup.feature.auth.domain.model.PasswordValidation
import com.aungthiha.topup.feature.auth.domain.repository.AuthRepository
import com.aungthiha.topup.feature.auth.domain.usecase.SignInUseCase
import com.aungthiha.topup.feature.auth.domain.usecase.SignUpUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthCredentialsTest {

    private val repository = FakeAuthRepository()

    @Test
    fun `email and password validation`() {
        assertEquals(EmailValidation.EMPTY, CredentialsValidator.validateEmail("  "))
        assertEquals(EmailValidation.INVALID, CredentialsValidator.validateEmail("aung@"))
        assertEquals(EmailValidation.VALID, CredentialsValidator.validateEmail(" aung@example.com "))
        assertEquals(PasswordValidation.TOO_SHORT, CredentialsValidator.validatePassword("12345"))
        assertEquals(PasswordValidation.VALID, CredentialsValidator.validatePassword("123456"))
        assertEquals(PasswordConfirmationValidation.MISMATCH, CredentialsValidator.validateConfirmation("abcdef", "abcdeg"))
    }

    @Test
    fun `sign in trims email and rejects invalid input before calling the backend`() = runTest {
        assertEquals(DataResult.Error(AuthError.INVALID_EMAIL), SignInUseCase(repository)("bad", "secret"))
        assertTrue(repository.signIns.isEmpty())

        SignInUseCase(repository)(" aung@example.com ", "secret")

        assertEquals(listOf("aung@example.com" to "secret"), repository.signIns)
    }

    @Test
    fun `sign up enforces the minimum password length`() = runTest {
        assertEquals(DataResult.Error(AuthError.WEAK_PASSWORD), SignUpUseCase(repository)("aung@example.com", "123"))
        assertEquals(
            DataResult.Success(Unit),
            SignUpUseCase(repository)("aung@example.com", "123456"),
        )
        assertEquals(listOf("aung@example.com" to "123456"), repository.signUps)
    }

    private class FakeAuthRepository : AuthRepository {
        val signIns = mutableListOf<Pair<String, String>>()
        val signUps = mutableListOf<Pair<String, String>>()

        override suspend fun signIn(email: String, password: String): DataResult<Unit, AuthError> {
            signIns += email to password
            return DataResult.Success(Unit)
        }

        override suspend fun signUp(email: String, password: String): DataResult<Unit, AuthError> {
            signUps += email to password
            return DataResult.Success(Unit)
        }
    }
}
