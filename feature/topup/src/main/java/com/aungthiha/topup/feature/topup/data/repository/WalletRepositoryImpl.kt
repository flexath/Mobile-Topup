package com.aungthiha.topup.feature.topup.data.repository

import com.aungthiha.topup.core.common.result.DataError
import com.aungthiha.topup.core.common.result.DataResult
import com.aungthiha.topup.core.common.result.LocalError
import com.aungthiha.topup.core.common.result.NetworkError
import com.aungthiha.topup.core.common.session.CurrentUserProvider
import com.aungthiha.topup.core.datastore.UserPreferencesDataSource
import com.aungthiha.topup.core.datastore.model.CachedWalletBalance
import com.aungthiha.topup.core.network.toNetworkError
import com.aungthiha.topup.feature.topup.data.remote.TopupRemoteService
import com.aungthiha.topup.feature.topup.domain.model.WalletBalance
import com.aungthiha.topup.feature.topup.domain.repository.WalletRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
internal class WalletRepositoryImpl(
    private val remote: TopupRemoteService,
    private val preferences: UserPreferencesDataSource,
    private val currentUser: CurrentUserProvider,
    private val clock: () -> Long = System::currentTimeMillis,
) : WalletRepository {

    override fun observeBalance(): Flow<WalletBalance?> =
        currentUser.userId.flatMapLatest { userId ->
            if (userId == null) {
                flowOf(null)
            } else {
                preferences.walletBalance(userId).map { cached ->
                    cached?.let { WalletBalance(balanceMmk = it.balanceMmk, syncedAtMillis = it.syncedAtMillis) }
                }
            }
        }

    override suspend fun refresh(): DataResult<WalletBalance, DataError> {
        val userId = currentUser.userId.value ?: return DataResult.Error(LocalError.NO_ACTIVE_SESSION)
        return try {
            val wallet = remote.fetchWallet() ?: return DataResult.Error(NetworkError.NOT_FOUND)
            // Never write one account's balance under another account's key.
            if (currentUser.userId.value != userId) return DataResult.Error(LocalError.NO_ACTIVE_SESSION)
            val balance = WalletBalance(balanceMmk = wallet.balanceMmk, syncedAtMillis = clock())
            preferences.saveWalletBalance(userId, CachedWalletBalance(balance.balanceMmk, balance.syncedAtMillis))
            DataResult.Success(balance)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            DataResult.Error(throwable.toNetworkError())
        }
    }

    override suspend fun cacheConfirmedBalance(balanceMmk: Long) {
        val userId = currentUser.userId.value ?: return
        preferences.saveWalletBalance(userId, CachedWalletBalance(balanceMmk = balanceMmk, syncedAtMillis = clock()))
    }
}
