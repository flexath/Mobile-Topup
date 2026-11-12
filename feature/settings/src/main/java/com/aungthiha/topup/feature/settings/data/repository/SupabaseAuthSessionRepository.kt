package com.aungthiha.topup.feature.settings.data.repository

import com.aungthiha.topup.feature.settings.domain.model.AuthState
import com.aungthiha.topup.feature.settings.domain.model.AuthUser
import com.aungthiha.topup.feature.settings.domain.repository.AuthSessionRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal class SupabaseAuthSessionRepository(
    private val supabase: SupabaseClient,
    scope: CoroutineScope,
) : AuthSessionRepository {

    /** Last authenticated user, kept while a token refresh is failing (e.g. offline launch). */
    @Volatile
    private var lastKnownUser: AuthUser? = null

    override val authState: StateFlow<AuthState> = supabase.auth.sessionStatus
        .map { status -> status.toAuthState() }
        .distinctUntilChanged()
        .stateIn(scope, SharingStarted.Eagerly, AuthState.Initializing)

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
}
