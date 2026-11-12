package com.aungthiha.topup.feature.settings

import com.aungthiha.topup.core.common.session.UserScopedDataCleaner
import com.aungthiha.topup.feature.settings.domain.model.AuthState
import com.aungthiha.topup.feature.settings.domain.repository.AuthSessionRepository
import com.aungthiha.topup.feature.settings.domain.usecase.SignOutUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SignOutUseCaseTest {

    @Test
    fun `sign out clears every user scoped cache, even if one fails`() = runTest {
        val repository = FakeAuthSessionRepository()
        val cleared = mutableListOf<String>()
        val cleaners = listOf(
            UserScopedDataCleaner { cleared += "wallet" },
            UserScopedDataCleaner { error("disk full") },
            UserScopedDataCleaner { cleared += "transactions" },
        )

        SignOutUseCase(repository, cleaners)()

        assertEquals(listOf("wallet", "transactions"), cleared)
        assertEquals(AuthState.Unauthenticated, repository.authState.value)
    }

    private class FakeAuthSessionRepository : AuthSessionRepository {
        private val state = MutableStateFlow<AuthState>(AuthState.Initializing)
        override val authState: StateFlow<AuthState> = state

        override suspend fun signOut() {
            state.value = AuthState.Unauthenticated
        }
    }
}
