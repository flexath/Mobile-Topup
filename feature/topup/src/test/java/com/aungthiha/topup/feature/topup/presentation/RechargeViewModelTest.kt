package com.aungthiha.topup.feature.topup.presentation

import androidx.lifecycle.SavedStateHandle
import com.aungthiha.topup.core.common.result.DataError
import com.aungthiha.topup.feature.topup.TestCatalog
import com.aungthiha.topup.feature.topup.domain.model.CatalogSyncStatus
import com.aungthiha.topup.feature.topup.domain.model.OperatorCode
import com.aungthiha.topup.feature.topup.domain.model.RechargeBlocker
import com.aungthiha.topup.feature.topup.domain.model.RechargeSelection
import com.aungthiha.topup.feature.topup.domain.model.RechargeTab
import com.aungthiha.topup.feature.topup.domain.model.WalletBalance
import com.aungthiha.topup.feature.topup.domain.usecase.DetectOperatorUseCase
import com.aungthiha.topup.feature.topup.domain.usecase.EvaluateRechargeEligibilityUseCase
import com.aungthiha.topup.feature.topup.domain.usecase.ObserveCatalogSyncStatusUseCase
import com.aungthiha.topup.feature.topup.domain.usecase.ObserveCatalogUseCase
import com.aungthiha.topup.feature.topup.domain.usecase.ObserveWalletBalanceUseCase
import com.aungthiha.topup.feature.topup.domain.usecase.SyncCatalogUseCase
import com.aungthiha.topup.feature.topup.domain.usecase.ValidateCustomAmountUseCase
import com.aungthiha.topup.feature.topup.presentation.screens.recharge.RechargeViewModel
import com.aungthiha.topup.feature.topup.presentation.states.CatalogUiState
import com.aungthiha.topup.feature.topup.presentation.states.RechargeEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class RechargeViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val catalogRepository = FakeCatalogRepository()
    private val walletRepository = FakeWalletRepository(initial = WalletBalance(500_000, 0))
    private val network = FakeNetworkMonitor()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(savedState: SavedStateHandle = SavedStateHandle()) = RechargeViewModel(
        savedStateHandle = savedState,
        observeCatalog = ObserveCatalogUseCase(catalogRepository),
        observeCatalogSyncStatus = ObserveCatalogSyncStatusUseCase(catalogRepository),
        syncCatalog = SyncCatalogUseCase(catalogRepository),
        observeWalletBalance = ObserveWalletBalanceUseCase(walletRepository),
        detectOperator = DetectOperatorUseCase(),
        validateCustomAmount = ValidateCustomAmountUseCase(),
        evaluateEligibility = EvaluateRechargeEligibilityUseCase(),
        networkMonitor = network,
    )

    private fun TestScope.subscribe(vm: RechargeViewModel) {
        backgroundScope.launch(dispatcher) { vm.uiState.collect { } }
    }

    @Test
    fun `requests the catalog and reads the cached balance without refreshing the wallet`() = runTest(dispatcher) {
        val vm = viewModel()
        subscribe(vm)

        assertEquals(1, catalogRepository.launchRefreshes)
        assertEquals(0, walletRepository.refreshes)
        assertEquals(500_000L, vm.uiState.value.walletBalanceMmk)
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
        assertEquals(500_000L, vm.uiState.value.walletBalanceMmk)
    }

    @Test
    fun `confirming emits a processing route with a fresh idempotency key`() = runTest(dispatcher) {
        val vm = viewModel()
        subscribe(vm)
        vm.onPhoneNumberChange("09420000001")
        vm.onProductSelected("MPT-den-20000")
        vm.onProceed()
        assertEquals(20_000L, vm.uiState.value.confirmation?.priceMmk)
        assertEquals(500_000L, vm.uiState.value.confirmation?.walletBalanceMmk)

        vm.onConfirmRecharge()

        val event = vm.rechargeEvents.first() as RechargeEvent.StartRecharge
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
}
