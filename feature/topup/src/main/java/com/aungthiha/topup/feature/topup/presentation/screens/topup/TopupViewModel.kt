package com.aungthiha.topup.feature.topup.presentation.screens.topup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aungthiha.topup.core.common.network.NetworkMonitor
import com.aungthiha.topup.core.common.result.DataResult
import com.aungthiha.topup.feature.topup.domain.model.OperatorCatalog
import com.aungthiha.topup.feature.topup.domain.model.OperatorCode
import com.aungthiha.topup.feature.topup.domain.usecase.ObserveCatalogUseCase
import com.aungthiha.topup.feature.topup.domain.usecase.ObserveWalletBalanceUseCase
import com.aungthiha.topup.feature.topup.domain.usecase.RefreshWalletBalanceUseCase
import com.aungthiha.topup.feature.topup.domain.usecase.SyncCatalogUseCase
import com.aungthiha.topup.feature.topup.presentation.states.OperatorUiModel
import com.aungthiha.topup.feature.topup.presentation.states.TopupUiState
import com.aungthiha.topup.feature.topup.presentation.states.WalletUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Wallet overview. Owns the wallet refresh; the recharge form lives in RechargeViewModel. */
class TopupViewModel(
    observeCatalog: ObserveCatalogUseCase,
    private val syncCatalog: SyncCatalogUseCase,
    observeWalletBalance: ObserveWalletBalanceUseCase,
    private val refreshWalletBalance: RefreshWalletBalanceUseCase,
    networkMonitor: NetworkMonitor,
    private val clock: () -> Long = System::currentTimeMillis,
) : ViewModel() {

    private val walletRefresh = MutableStateFlow(WalletRefreshState())
    private var walletRefreshJob: Job? = null
    private var lastWalletRefreshAt = 0L

    val uiState: StateFlow<TopupUiState> = combine(
        observeWalletBalance(),
        walletRefresh,
        observeCatalog(),
        networkMonitor.isOnline,
    ) { balance, refresh, catalogs, online ->
        TopupUiState(
            wallet = WalletUiState(
                balanceMmk = balance?.balanceMmk,
                isRefreshing = refresh.isRefreshing,
                isStale = refresh.lastFailed && !refresh.isRefreshing,
            ),
            operators = supportedOperators(catalogs),
            isOnline = online,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), TopupUiState())

    init {
        // Start the once-per-launch catalog refresh early so the recharge screen opens with data.
        syncCatalog()
        refreshWallet(force = true)
    }

    /** Called when the overview becomes visible (initial entry or returning to it). */
    fun onScreenVisible() = refreshWallet(force = false)

    private fun refreshWallet(force: Boolean) {
        val now = clock()
        if (!force && now - lastWalletRefreshAt < WALLET_REFRESH_THROTTLE_MILLIS) return
        if (walletRefreshJob?.isActive == true) return
        lastWalletRefreshAt = now
        walletRefreshJob = viewModelScope.launch {
            walletRefresh.update { it.copy(isRefreshing = true) }
            val result = refreshWalletBalance()
            walletRefresh.value = WalletRefreshState(isRefreshing = false, lastFailed = result is DataResult.Error)
        }
    }

    private fun supportedOperators(catalogs: List<OperatorCatalog>): List<OperatorUiModel> =
        OperatorCode.entries.map { code ->
            val operator = catalogs.firstOrNull { it.operator.code == code }?.operator
            OperatorUiModel(
                code = code,
                displayName = operator?.displayName ?: code.fallbackDisplayName,
                logoUrl = operator?.logoUrl,
            )
        }

    private data class WalletRefreshState(
        val isRefreshing: Boolean = false,
        val lastFailed: Boolean = false,
    )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val WALLET_REFRESH_THROTTLE_MILLIS = 10_000L
    }
}
