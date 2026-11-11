package com.aungthiha.topup.feature.topup.data.repository

import com.aungthiha.topup.core.common.result.DataResult
import com.aungthiha.topup.core.common.result.LocalError
import com.aungthiha.topup.core.common.result.NetworkError
import com.aungthiha.topup.feature.topup.data.FakeCurrentUser
import com.aungthiha.topup.feature.topup.data.FakePreferences
import com.aungthiha.topup.feature.topup.data.FakeTopupRemoteService
import com.aungthiha.topup.feature.topup.data.remote.WalletDto
import com.aungthiha.topup.feature.topup.domain.model.WalletBalance
import io.github.jan.supabase.exceptions.HttpRequestException
import io.ktor.client.request.HttpRequestBuilder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WalletRepositoryImplTest {

    private val remote = FakeTopupRemoteService()
    private val preferences = FakePreferences()
    private val currentUser = FakeCurrentUser("user-a")
    private val repository = WalletRepositoryImpl(remote, preferences, currentUser, clock = { 42L })

    @Test
    fun `refresh caches the backend balance under the signed-in user`() = runTest {
        remote.wallet = WalletDto(balanceMmk = 480_000, currency = "MMK")

        val result = repository.refresh()

        assertEquals(DataResult.Success(WalletBalance(480_000, 42L)), result)
        assertEquals(WalletBalance(480_000, 42L), repository.observeBalance().first())
        assertEquals(setOf("user-a"), preferences.wallets.value.keys)
    }

    @Test
    fun `failed refresh keeps the previous cached balance`() = runTest {
        repository.cacheConfirmedBalance(300_000)
        remote.walletError = HttpRequestException("offline", HttpRequestBuilder())

        val result = repository.refresh()

        assertEquals(DataResult.Error(NetworkError.NO_CONNECTION), result)
        assertEquals(300_000L, repository.observeBalance().first()?.balanceMmk)
    }

    @Test
    fun `one user's cached balance is never shown to another user`() = runTest {
        repository.cacheConfirmedBalance(123_000)

        currentUser.state.value = "user-b"

        assertNull(repository.observeBalance().first())
    }

    @Test
    fun `refresh without a session does not call the backend`() = runTest {
        currentUser.state.value = null
        remote.walletError = IllegalStateException("must not be called")

        assertEquals(DataResult.Error(LocalError.NO_ACTIVE_SESSION), repository.refresh())
    }
}
