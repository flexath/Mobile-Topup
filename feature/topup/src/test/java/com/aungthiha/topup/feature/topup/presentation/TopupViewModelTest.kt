package com.aungthiha.topup.feature.topup.presentation

import com.aungthiha.topup.feature.topup.domain.model.OperatorCode
import com.aungthiha.topup.feature.topup.domain.usecase.ObserveCatalogUseCase
import com.aungthiha.topup.feature.topup.domain.usecase.ObserveWalletBalanceUseCase
import com.aungthiha.topup.feature.topup.domain.usecase.RefreshWalletBalanceUseCase
import com.aungthiha.topup.feature.topup.domain.usecase.SyncCatalogUseCase
import com.aungthiha.topup.feature.topup.presentation.screens.topup.TopupViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TopupViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val catalogRepository = FakeCatalogRepository()
    private val walletRepository = FakeWalletRepository()
    private val network = FakeNetworkMonitor()
    private var now = 0L

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = TopupViewModel(
        observeCatalog = ObserveCatalogUseCase(catalogRepository),
        syncCatalog = SyncCatalogUseCase(catalogRepository),
        observeWalletBalance = ObserveWalletBalanceUseCase(walletRepository),
        refreshWalletBalance = RefreshWalletBalanceUseCase(walletRepository),
        networkMonitor = network,
        clock = { now },
    )

    private fun TestScope.subscribe(vm: TopupViewModel) {
        backgroundScope.launch(dispatcher) { vm.uiState.collect { } }
    }

    @Test
    fun `starts the launch catalog refresh and a wallet refresh once`() = runTest(dispatcher) {
        val vm = viewModel()
        subscribe(vm)
        vm.onScreenVisible()

        assertEquals(1, catalogRepository.launchRefreshes)
        assertEquals(1, walletRepository.refreshes)
        assertEquals(500_000L, vm.uiState.value.wallet.balanceMmk)
        assertFalse(vm.uiState.value.wallet.isRefreshing)
    }

    @Test
    fun `wallet refreshes again when the screen is revisited after the throttle window`() = runTest(dispatcher) {
        val vm = viewModel()
        subscribe(vm)
        now = 60_000
        vm.onScreenVisible()

        assertEquals(2, walletRepository.refreshes)
    }

    @Test
    fun `offline keeps the cached balance`() = runTest(dispatcher) {
        val vm = viewModel()
        subscribe(vm)

        network.online.value = false

        assertFalse(vm.uiState.value.isOnline)
        assertEquals(500_000L, vm.uiState.value.wallet.balanceMmk)
    }

    @Test
    fun `lists every supported operator, with catalog names and fallbacks`() = runTest(dispatcher) {
        catalogRepository.catalog.value = emptyList()
        val vm = viewModel()
        subscribe(vm)

        val operators = vm.uiState.value.operators
        assertEquals(OperatorCode.entries, operators.map { it.code })
        assertEquals(OperatorCode.MPT.fallbackDisplayName, operators.first().displayName)
        assertNull(operators.first().logoUrl)
    }
}
