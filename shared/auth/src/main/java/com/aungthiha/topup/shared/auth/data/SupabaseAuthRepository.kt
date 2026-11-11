package com.aungthiha.topup.shared.auth.data

import com.aungthiha.topup.core.common.result.DataResult
import com.aungthiha.topup.core.common.result.NetworkError
import com.aungthiha.topup.core.network.toNetworkError
import com.aungthiha.topup.shared.auth.domain.model.AuthError
import com.aungthiha.topup.shared.auth.domain.model.AuthState
import com.aungthiha.topup.shared.auth.domain.model.AuthUser
import com.aungthiha.topup.shared.auth.domain.model.SignUpResult
import com.aungthiha.topup.shared.auth.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthWeakPasswordException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal class SupabaseAuthRepository(
    private val supabase: SupabaseClient,
    scope: CoroutineScope,
) : AuthRepository {

    /** Last authenticated user, kept while a token refresh is failing (e.g. offline launch). */
    @Volatile
    private var lastKnownUser: AuthUser? = null

    override val authState: StateFlow<AuthState> = supabase.auth.sessionStatus
        .map { status -> status.toAuthState() }
        .distinctUntilChanged()
        .stateIn(scope, SharingStarted.Eagerly, AuthState.Initializing)

    override suspend fun signIn(email: String, password: String): DataResult<Unit, AuthError> =
        runAuthCall {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
        }

    override suspend fun signUp(email: String, password: String): DataResult<SignUpResult, AuthError> =
        runAuthCall {
            val user = supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            when {
                supabase.auth.currentSessionOrNull() != null -> SignUpResult.SignedIn
                // With email-enumeration protection Supabase answers an existing address with an
                // obfuscated user that has no identities instead of an error.
                user?.identities?.isEmpty() == true -> return DataResult.Error(AuthError.EMAIL_ALREADY_REGISTERED)
                else -> SignUpResult.ConfirmationRequired(email)
            }
        }

    override suspend fun signOut() {
        try {
            supabase.auth.signOut()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            // Offline or server error: the server session expires on its own; drop it locally.
            supabase.auth.clearSession()
        }
        lastKnownUser = null
    }

    private suspend fun SessionStatus.toAuthState(): AuthState = when (this) {
        SessionStatus.Initializing -> AuthState.Initializing
        is SessionStatus.Authenticated -> {
            val user = session.user?.toAuthUser() ?: lastKnownUser
            if (user == null) {
                AuthState.Initializing
            } else {
                lastKnownUser = user
                AuthState.Authenticated(user)
            }
        }
        // A stored session exists but could not be refreshed yet (usually no connectivity).
        // Keep the user signed in so cached data stays usable; Auth keeps retrying.
        is SessionStatus.RefreshFailure -> {
            val user = lastKnownUser ?: supabase.auth.sessionManager.loadSessionOrNull()?.user?.toAuthUser()
            if (user != null) {
                lastKnownUser = user
                AuthState.Authenticated(user)
            } else {
                AuthState.Unauthenticated
            }
        }
        is SessionStatus.NotAuthenticated -> {
            lastKnownUser = null
            AuthState.Unauthenticated
        }
    }

    private fun UserInfo.toAuthUser() = AuthUser(id = id, email = email)

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
