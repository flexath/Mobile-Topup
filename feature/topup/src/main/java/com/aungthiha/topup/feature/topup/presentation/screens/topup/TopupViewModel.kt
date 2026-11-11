package com.aungthiha.topup.feature.topup.presentation.screens.topup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aungthiha.topup.core.common.network.NetworkMonitor
import com.aungthiha.topup.core.common.result.DataResult
import com.aungthiha.topup.feature.topup.domain.amount.CustomAmountValidation
import com.aungthiha.topup.feature.topup.domain.amount.CustomAmountValidator
import com.aungthiha.topup.feature.topup.domain.model.CatalogSyncStatus
import com.aungthiha.topup.feature.topup.domain.model.OperatorCatalog
import com.aungthiha.topup.feature.topup.domain.model.RechargeSelection
import com.aungthiha.topup.feature.topup.domain.model.RechargeTab
import com.aungthiha.topup.feature.topup.domain.model.WalletBalance
import com.aungthiha.topup.feature.topup.domain.phone.MyanmarPhoneNumber
import com.aungthiha.topup.feature.topup.domain.phone.PhoneNumberStatus
import com.aungthiha.topup.feature.topup.domain.usecase.DetectOperatorUseCase
import com.aungthiha.topup.feature.topup.domain.usecase.EvaluateRechargeEligibilityUseCase
import com.aungthiha.topup.feature.topup.domain.usecase.ObserveCatalogSyncStatusUseCase
import com.aungthiha.topup.feature.topup.domain.usecase.ObserveCatalogUseCase
import com.aungthiha.topup.feature.topup.domain.usecase.ObserveWalletBalanceUseCase
import com.aungthiha.topup.feature.topup.domain.usecase.RefreshWalletBalanceUseCase
import com.aungthiha.topup.feature.topup.domain.usecase.SyncCatalogUseCase
import com.aungthiha.topup.feature.topup.domain.usecase.ValidateCustomAmountUseCase
import com.aungthiha.topup.feature.topup.presentation.navigation.TopupRoutes
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.UUID

class TopupViewModel(
    private val savedStateHandle: SavedStateHandle,
    observeCatalog: ObserveCatalogUseCase,
    observeCatalogSyncStatus: ObserveCatalogSyncStatusUseCase,
    private val syncCatalog: SyncCatalogUseCase,
    observeWalletBalance: ObserveWalletBalanceUseCase,
    private val refreshWalletBalance: RefreshWalletBalanceUseCase,
    private val detectOperator: DetectOperatorUseCase,
    private val validateCustomAmount: ValidateCustomAmountUseCase,
    private val evaluateEligibility: EvaluateRechargeEligibilityUseCase,
    networkMonitor: NetworkMonitor,
    private val clock: () -> Long = System::currentTimeMillis,
) : ViewModel() {

    private val formState = MutableStateFlow(restoreForm())
    private val walletRefresh = MutableStateFlow(WalletRefreshState())
    private val events = Channel<TopupEvent>(Channel.BUFFERED)
    private var walletRefreshJob: Job? = null
    private var lastWalletRefreshAt = 0L

    val topupEvents: Flow<TopupEvent> = events.receiveAsFlow()

    private val catalogInputs = combine(
        observeCatalog(),
        observeCatalogSyncStatus(),
    ) { catalog, status -> catalog to status }

    private val walletInputs = combine(
        observeWalletBalance(),
        walletRefresh,
        networkMonitor.isOnline,
    ) { balance, refresh, online -> Triple(balance, refresh, online) }

    val uiState: StateFlow<TopupUiState> = combine(formState, catalogInputs, walletInputs) { form, catalog, wallet ->
        buildUiState(
            form = form,
            catalogs = catalog.first,
            syncStatus = catalog.second,
            balance = wallet.first,
            refresh = wallet.second,
            isOnline = wallet.third,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), TopupUiState(form = formState.value))

    init {
        syncCatalog()
        refreshWallet(force = true)
    }

    /** Called when the Top-up screen becomes visible (initial entry or returning to it). */
    fun onScreenVisible() = refreshWallet(force = false)

    fun onPhoneNumberChange(raw: String) {
        val normalized = MyanmarPhoneNumber.normalize(raw).take(MAX_PHONE_INPUT_LENGTH)
        val detected = (detectOperator(normalized) as? PhoneNumberStatus.Detected)?.operator
        updateForm { form ->
            val operatorChanged = detected != null && form.selectionOperator != null && detected != form.selectionOperator
            if (operatorChanged) {
                form.copy(
                    phoneInput = normalized,
                    selectedProductId = null,
                    customAmountInput = "",
                    selectionOperator = null,
                )
            } else {
                form.copy(phoneInput = normalized)
            }
        }
    }

    fun onTabSelected(tab: RechargeTab) = updateForm { form ->
        if (form.activeTab == tab) {
            form
        } else {
            form.copy(
                activeTab = tab,
                selectedProductId = null,
                customAmountInput = "",
                isCustomAmountVisible = false,
                selectionOperator = null,
            )
        }
    }

    fun onProductSelected(productId: String) = updateForm { form ->
        form.copy(
            selectedProductId = productId,
            customAmountInput = "",
            selectionOperator = currentOperator(form),
        )
    }

    fun onCustomAmountToggle() = updateForm { form ->
        if (form.isCustomAmountVisible) {
            form.copy(isCustomAmountVisible = false, customAmountInput = "")
        } else {
            form.copy(isCustomAmountVisible = true)
        }
    }

    fun onCustomAmountChange(input: String) = updateForm { form ->
        val sanitized = CustomAmountValidator.sanitize(input)
        form.copy(
            customAmountInput = sanitized,
            selectedProductId = if (sanitized.isNotEmpty()) null else form.selectedProductId,
            selectionOperator = if (sanitized.isNotEmpty()) currentOperator(form) else form.selectionOperator,
        )
    }

    fun onRetryCatalog() = syncCatalog(retry = true)

    fun onProceed() {
        if (uiState.value.canProceed) updateForm { it.copy(isConfirmationVisible = true) }
    }

    fun onDismissConfirmation() = updateForm { it.copy(isConfirmationVisible = false) }

    fun onConfirmRecharge() {
        val state = uiState.value
        val confirmation = state.confirmation ?: return
        if (!state.canProceed) return
        updateForm { it.copy(isConfirmationVisible = false) }
        val route = TopupRoutes.RechargeProcessing(
            phoneNumber = confirmation.phoneNumber,
            operatorCode = confirmation.operator.code.code,
            operatorName = confirmation.operator.displayName,
            productId = (confirmation.selection as? RechargeSelection.Product)?.product?.id,
            customAmountMmk = (confirmation.selection as? RechargeSelection.CustomAmount)?.amountMmk,
            displayAmountMmk = confirmation.priceMmk,
            idempotencyKey = UUID.randomUUID().toString(),
        )
        viewModelScope.launch { events.send(TopupEvent.StartRecharge(route)) }
    }

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

    private fun currentOperator(form: TopupFormState) =
        (detectOperator(form.phoneInput) as? PhoneNumberStatus.Detected)?.operator

    private fun updateForm(transform: (TopupFormState) -> TopupFormState) {
        val updated = formState.updateAndGet(transform)
        savedStateHandle[KEY_FORM] = Json.encodeToString(updated)
    }

    private fun restoreForm(): TopupFormState =
        savedStateHandle.get<String>(KEY_FORM)
            ?.let { runCatching { Json.decodeFromString<TopupFormState>(it) }.getOrNull() }
            ?: TopupFormState()

    private fun buildUiState(
        form: TopupFormState,
        catalogs: List<OperatorCatalog>,
        syncStatus: CatalogSyncStatus,
        balance: WalletBalance?,
        refresh: WalletRefreshState,
        isOnline: Boolean,
    ): TopupUiState {
        val phoneStatus = detectOperator(form.phoneInput)
        val detectedCode = (phoneStatus as? PhoneNumberStatus.Detected)?.operator
        val operatorCatalog = catalogs.firstOrNull { it.operator.code == detectedCode }
        val operator = detectedCode?.let { code ->
            OperatorUiModel(
                code = code,
                displayName = operatorCatalog?.operator?.displayName ?: code.fallbackDisplayName,
                logoUrl = operatorCatalog?.operator?.logoUrl,
            )
        }
        val rule = operatorCatalog?.operator?.customAmountRule
        val customValidation = if (form.isCustomAmountVisible && rule != null) {
            validateCustomAmount(form.customAmountInput, rule)
        } else {
            CustomAmountValidation.Empty
        }
        val selection = resolveSelection(form, operatorCatalog, customValidation)
        val blocker = evaluateEligibility(
            phone = phoneStatus,
            catalog = operatorCatalog,
            selection = selection,
            customAmount = customValidation.takeIf { form.isCustomAmountVisible && form.selectedProductId == null },
            isOnline = isOnline,
        )
        val catalogState = when {
            catalogs.isNotEmpty() -> CatalogUiState.Ready
            syncStatus is CatalogSyncStatus.Failed -> CatalogUiState.Unavailable(syncStatus.error)
            else -> CatalogUiState.Loading
        }
        val confirmation = if (form.isConfirmationVisible && blocker == null && selection != null && operator != null) {
            RechargeConfirmationUi(
                phoneNumber = phoneStatus.normalized,
                operator = operator,
                selection = selection,
                priceMmk = selection.amountMmk,
                walletBalanceMmk = balance?.balanceMmk,
            )
        } else {
            null
        }
        return TopupUiState(
            form = form,
            wallet = WalletUiState(
                balanceMmk = balance?.balanceMmk,
                isRefreshing = refresh.isRefreshing,
                isStale = refresh.lastFailed && !refresh.isRefreshing,
            ),
            phoneStatus = phoneStatus,
            operator = operator,
            catalogState = catalogState,
            isCatalogStale = catalogs.isNotEmpty() && syncStatus is CatalogSyncStatus.Failed,
            denominations = operatorCatalog?.denominations.orEmpty(),
            packages = operatorCatalog?.packages.orEmpty(),
            customAmountRule = rule,
            customAmountValidation = customValidation,
            selection = selection,
            isOnline = isOnline,
            blocker = blocker,
            confirmation = confirmation,
        )
    }

    private fun resolveSelection(
        form: TopupFormState,
        catalog: OperatorCatalog?,
        customValidation: CustomAmountValidation,
    ): RechargeSelection? {
        if (catalog == null) return null
        form.selectedProductId?.let { id ->
            val products = when (form.activeTab) {
                RechargeTab.DENOMINATIONS -> catalog.denominations
                RechargeTab.PACKAGES -> catalog.packages
            }
            return products.firstOrNull { it.id == id }?.let(RechargeSelection::Product)
        }
        if (form.activeTab == RechargeTab.DENOMINATIONS && customValidation is CustomAmountValidation.Valid) {
            return RechargeSelection.CustomAmount(customValidation.amountMmk)
        }
        return null
    }

    private data class WalletRefreshState(
        val isRefreshing: Boolean = false,
        val lastFailed: Boolean = false,
    )

    private companion object {
        const val KEY_FORM = "topup_form"
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val WALLET_REFRESH_THROTTLE_MILLIS = 10_000L
        const val MAX_PHONE_INPUT_LENGTH = 16
    }
}
