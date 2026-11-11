package com.aungthiha.topup.feature.topup.presentation

import com.aungthiha.topup.core.common.network.NetworkMonitor
import com.aungthiha.topup.core.common.result.DataError
import com.aungthiha.topup.core.common.result.DataResult
import com.aungthiha.topup.feature.topup.TestCatalog
import com.aungthiha.topup.feature.topup.domain.model.CatalogSyncStatus
import com.aungthiha.topup.feature.topup.domain.model.OperatorCatalog
import com.aungthiha.topup.feature.topup.domain.model.WalletBalance
import com.aungthiha.topup.feature.topup.domain.repository.CatalogRepository
import com.aungthiha.topup.feature.topup.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class FakeCatalogRepository : CatalogRepository {
    val catalog = MutableStateFlow(TestCatalog.all)
    val status = MutableStateFlow<CatalogSyncStatus>(CatalogSyncStatus.Synced)
    var launchRefreshes = 0
    var retries = 0

    override fun observeCatalog(): Flow<List<OperatorCatalog>> = catalog
    override val syncStatus: StateFlow<CatalogSyncStatus> = status
    override fun startLaunchRefresh() {
        launchRefreshes++
    }
    override fun retryRefresh() {
        retries++
    }
}

internal class FakeWalletRepository(initial: WalletBalance? = null) : WalletRepository {
    private val balance = MutableStateFlow(initial)
    var refreshes = 0

    override fun observeBalance(): Flow<WalletBalance?> = balance

    override suspend fun refresh(): DataResult<WalletBalance, DataError> {
        refreshes++
        return DataResult.Success(WalletBalance(500_000, 0)).also { balance.value = it.data }
    }

    override suspend fun cacheConfirmedBalance(balanceMmk: Long) {
        balance.value = WalletBalance(balanceMmk, 0)
    }
}

internal class FakeNetworkMonitor : NetworkMonitor {
    val online = MutableStateFlow(true)
    override val isOnline: StateFlow<Boolean> = online
}
