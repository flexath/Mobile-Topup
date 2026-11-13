package com.aungthiha.topup.feature.auth.data.repository

import com.aungthiha.topup.core.common.result.DataResult
import com.aungthiha.topup.core.common.result.NetworkError
import com.aungthiha.topup.core.network.toNetworkError
import com.aungthiha.topup.feature.auth.domain.model.AuthError
import com.aungthiha.topup.feature.auth.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.exception.AuthWeakPasswordException
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.CancellationException

internal class SupabaseAuthRepository(
    private val supabase: SupabaseClient,
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): DataResult<Unit, AuthError> =
        runAuthCall {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
        }

    override suspend fun signUp(email: String, password: String): DataResult<Unit, AuthError> =
        runAuthCall {
            val user = supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            when {
                supabase.auth.currentSessionOrNull() != null -> Unit
                // With email-enumeration protection Supabase answers an existing address with an
                // obfuscated user that has no identities instead of an error.
                user?.identities?.isEmpty() == true -> return DataResult.Error(AuthError.EMAIL_ALREADY_REGISTERED)
                else -> return DataResult.Error(AuthError.UNKNOWN)
            }
        }

    private inline fun <T> runAuthCall(block: () -> T): DataResult<T, AuthError> = try {
        DataResult.Success(block())
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (weak: AuthWeakPasswordException) {
        DataResult.Error(AuthError.WEAK_PASSWORD)
    } catch (rest: AuthRestException) {
        DataResult.Error(rest.errorCode.toAuthError())
    } catch (throwable: Throwable) {
        DataResult.Error(
            when (throwable.toNetworkError()) {
                NetworkError.NO_CONNECTION, NetworkError.TIMEOUT -> AuthError.NO_CONNECTION
                else -> AuthError.UNKNOWN
            },
        )
    }

    private fun AuthErrorCode?.toAuthError(): AuthError = when (this) {
        AuthErrorCode.InvalidCredentials -> AuthError.INVALID_CREDENTIALS
        AuthErrorCode.EmailNotConfirmed -> AuthError.EMAIL_NOT_CONFIRMED
        AuthErrorCode.UserAlreadyExists, AuthErrorCode.EmailExists -> AuthError.EMAIL_ALREADY_REGISTERED
        AuthErrorCode.WeakPassword -> AuthError.WEAK_PASSWORD
        AuthErrorCode.EmailAddressInvalid, AuthErrorCode.ValidationFailed -> AuthError.INVALID_EMAIL
        AuthErrorCode.SignupDisabled -> AuthError.SIGNUP_DISABLED
        AuthErrorCode.OverRequestRateLimit, AuthErrorCode.OverEmailSendRateLimit -> AuthError.RATE_LIMITED
        else -> AuthError.UNKNOWN
    }
}
