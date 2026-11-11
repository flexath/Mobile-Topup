package com.aungthiha.topup.feature.topup.presentation

import androidx.lifecycle.SavedStateHandle
import com.aungthiha.topup.core.common.network.NetworkMonitor
import com.aungthiha.topup.core.common.result.DataError
import com.aungthiha.topup.core.common.result.DataResult
import com.aungthiha.topup.feature.topup.TestCatalog
import com.aungthiha.topup.feature.topup.domain.model.CatalogSyncStatus
import com.aungthiha.topup.feature.topup.domain.model.OperatorCatalog
import com.aungthiha.topup.feature.topup.domain.model.OperatorCode
import com.aungthiha.topup.feature.topup.domain.model.RechargeBlocker
import com.aungthiha.topup.feature.topup.domain.model.RechargeSelection
import com.aungthiha.topup.feature.topup.domain.model.RechargeTab
import com.aungthiha.topup.feature.topup.domain.model.WalletBalance
import com.aungthiha.topup.feature.topup.domain.repository.CatalogRepository
import com.aungthiha.topup.feature.topup.domain.repository.WalletRepository
import com.aungthiha.topup.feature.topup.domain.usecase.DetectOperatorUseCase
import com.aungthiha.topup.feature.topup.domain.usecase.EvaluateRechargeEligibilityUseCase
import com.aungthiha.topup.feature.topup.domain.usecase.ObserveCatalogSyncStatusUseCase
import com.aungthiha.topup.feature.topup.domain.usecase.ObserveCatalogUseCase
import com.aungthiha.topup.feature.topup.domain.usecase.ObserveWalletBalanceUseCase
import com.aungthiha.topup.feature.topup.domain.usecase.RefreshWalletBalanceUseCase
import com.aungthiha.topup.feature.topup.domain.usecase.SyncCatalogUseCase
import com.aungthiha.topup.feature.topup.domain.usecase.ValidateCustomAmountUseCase
import com.aungthiha.topup.feature.topup.presentation.states.CatalogUiState
import com.aungthiha.topup.feature.topup.presentation.states.TopupEvent
import com.aungthiha.topup.feature.topup.presentation.screens.topup.TopupViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
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
import org.junit.Assert.assertTrue
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

    private fun viewModel(savedState: SavedStateHandle = SavedStateHandle()) = TopupViewModel(
        savedStateHandle = savedState,
        observeCatalog = ObserveCatalogUseCase(catalogRepository),
        observeCatalogSyncStatus = ObserveCatalogSyncStatusUseCase(catalogRepository),
        syncCatalog = SyncCatalogUseCase(catalogRepository),
        observeWalletBalance = ObserveWalletBalanceUseCase(walletRepository),
        refreshWalletBalance = RefreshWalletBalanceUseCase(walletRepository),
        detectOperator = DetectOperatorUseCase(),
        validateCustomAmount = ValidateCustomAmountUseCase(),
        evaluateEligibility = EvaluateRechargeEligibilityUseCase(),
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
    fun `selecting a denomination enables proceed`() = runTest(dispatcher) {
        val vm = viewModel()
        subscribe(vm)
        vm.onPhoneNumberChange("09420000001")
        assertEquals(RechargeBlocker.NO_SELECTION, vm.uiState.value.blocker)

        vm.onProductSelected("MPT-den-1000")

        val state = vm.uiState.value
        assertEquals(RechargeSelection.Product(TestCatalog.denomination(OperatorCode.MPT, 1_000)), state.selection)
        assertTrue(state.canProceed)
    }

    @Test
    fun `custom amount and denomination are mutually exclusive`() = runTest(dispatcher) {
        val vm = viewModel()
        subscribe(vm)
        vm.onPhoneNumberChange("09420000001")
        vm.onProductSelected("MPT-den-1000")
        vm.onCustomAmountToggle()

        vm.onCustomAmountChange("1500")
        assertNull(vm.uiState.value.form.selectedProductId)
        assertEquals(RechargeSelection.CustomAmount(1_500), vm.uiState.value.selection)

        vm.onProductSelected("MPT-den-2000")
        assertEquals("", vm.uiState.value.form.customAmountInput)
        assertEquals(2_000L, vm.uiState.value.selection?.amountMmk)
    }

    @Test
    fun `invalid custom amount blocks proceed`() = runTest(dispatcher) {
        val vm = viewModel()
        subscribe(vm)
        vm.onPhoneNumberChange("09420000001")
        vm.onCustomAmountToggle()
        vm.onCustomAmountChange("1050")

        assertEquals(RechargeBlocker.INVALID_CUSTOM_AMOUNT, vm.uiState.value.blocker)
    }

    @Test
    fun `switching tabs clears the active selection but keeps the number`() = runTest(dispatcher) {
        val vm = viewModel()
        subscribe(vm)
        vm.onPhoneNumberChange("09420000001")
        vm.onProductSelected("MPT-den-1000")

        vm.onTabSelected(RechargeTab.PACKAGES)

        val state = vm.uiState.value
        assertNull(state.selection)
        assertEquals("09420000001", state.form.phoneInput)
        assertEquals(OperatorCode.MPT, state.operator?.code)
        assertEquals(TestCatalog.operatorCatalog(OperatorCode.MPT).packages, state.packages)
    }

    @Test
    fun `operator change clears the selection, same operator keeps it`() = runTest(dispatcher) {
        val vm = viewModel()
        subscribe(vm)
        vm.onPhoneNumberChange("09420000001")
        vm.onProductSelected("MPT-den-1000")

        vm.onPhoneNumberChange("0942000000")
        vm.onPhoneNumberChange("09420000009")
        assertEquals("MPT-den-1000", vm.uiState.value.form.selectedProductId)

        vm.onPhoneNumberChange("09770000001")
        val state = vm.uiState.value
        assertNull(state.form.selectedProductId)
        assertEquals(OperatorCode.ATOM, state.operator?.code)
        assertEquals(TestCatalog.operatorCatalog(OperatorCode.ATOM).denominations, state.denominations)
    }

    @Test
    fun `form state survives recreation through SavedStateHandle`() = runTest(dispatcher) {
        val savedState = SavedStateHandle()
        val first = viewModel(savedState)
        subscribe(first)
        first.onPhoneNumberChange("09690000005")
        first.onTabSelected(RechargeTab.PACKAGES)
        first.onProductSelected("MYTEL-pkg-20000")

        val restored = viewModel(savedState)
        subscribe(restored)

        val state = restored.uiState.value
        assertEquals("09690000005", state.form.phoneInput)
        assertEquals(RechargeTab.PACKAGES, state.form.activeTab)
        assertEquals("MYTEL-pkg-20000", state.form.selectedProductId)
        assertTrue(state.canProceed)
    }

    @Test
    fun `offline blocks recharge but keeps the cached balance`() = runTest(dispatcher) {
        val vm = viewModel()
        subscribe(vm)
        vm.onPhoneNumberChange("09420000001")
        vm.onProductSelected("MPT-den-1000")

        network.online.value = false

        assertEquals(RechargeBlocker.OFFLINE, vm.uiState.value.blocker)
        assertEquals(500_000L, vm.uiState.value.wallet.balanceMmk)
    }

    @Test
    fun `confirming emits a processing route with a fresh idempotency key`() = runTest(dispatcher) {
        val vm = viewModel()
        subscribe(vm)
        vm.onPhoneNumberChange("09420000001")
        vm.onProductSelected("MPT-den-20000")
        vm.onProceed()
        assertEquals(20_000L, vm.uiState.value.confirmation?.priceMmk)

        vm.onConfirmRecharge()

        val event = vm.topupEvents.first() as TopupEvent.StartRecharge
        assertEquals("09420000001", event.route.phoneNumber)
        assertEquals("MPT", event.route.operatorCode)
        assertEquals("MPT-den-20000", event.route.productId)
        assertNull(event.route.customAmountMmk)
        assertEquals(36, event.route.idempotencyKey.length)
        assertFalse(vm.uiState.value.form.isConfirmationVisible)
    }

    @Test
    fun `empty catalog with a failed refresh shows the unavailable state`() = runTest(dispatcher) {
        catalogRepository.catalog.value = emptyList()
        catalogRepository.status.value = CatalogSyncStatus.Failed(object : DataError {})
        val vm = viewModel()
        subscribe(vm)

        vm.onRetryCatalog()

        assertTrue(vm.uiState.value.catalogState is CatalogUiState.Unavailable)
        assertEquals(1, catalogRepository.retries)
    }

    private class FakeCatalogRepository : CatalogRepository {
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

    private class FakeWalletRepository : WalletRepository {
        private val balance = MutableStateFlow<WalletBalance?>(null)
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

    private class FakeNetworkMonitor : NetworkMonitor {
        val online = MutableStateFlow(true)
        override val isOnline: StateFlow<Boolean> = online
    }
}
