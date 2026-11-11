package com.aungthiha.topup.feature.topup.presentation.screens.recharge

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aungthiha.topup.core.common.network.NetworkMonitor
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
import com.aungthiha.topup.feature.topup.domain.usecase.SyncCatalogUseCase
import com.aungthiha.topup.feature.topup.domain.usecase.ValidateCustomAmountUseCase
import com.aungthiha.topup.feature.topup.presentation.navigation.TopupRoutes
import com.aungthiha.topup.feature.topup.presentation.states.CatalogUiState
import com.aungthiha.topup.feature.topup.presentation.states.OperatorUiModel
import com.aungthiha.topup.feature.topup.presentation.states.RechargeConfirmationUi
import com.aungthiha.topup.feature.topup.presentation.states.RechargeEvent
import com.aungthiha.topup.feature.topup.presentation.states.RechargeFormState
import com.aungthiha.topup.feature.topup.presentation.states.RechargeUiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.UUID

class RechargeViewModel(
    private val savedStateHandle: SavedStateHandle,
    observeCatalog: ObserveCatalogUseCase,
    observeCatalogSyncStatus: ObserveCatalogSyncStatusUseCase,
    private val syncCatalog: SyncCatalogUseCase,
    observeWalletBalance: ObserveWalletBalanceUseCase,
    private val detectOperator: DetectOperatorUseCase,
    private val validateCustomAmount: ValidateCustomAmountUseCase,
    private val evaluateEligibility: EvaluateRechargeEligibilityUseCase,
    networkMonitor: NetworkMonitor,
) : ViewModel() {

    private val formState = MutableStateFlow(restoreForm())
    private val events = Channel<RechargeEvent>(Channel.BUFFERED)

    val rechargeEvents: Flow<RechargeEvent> = events.receiveAsFlow()

    private val catalogInputs = combine(
        observeCatalog(),
        observeCatalogSyncStatus(),
    ) { catalog, status -> catalog to status }

    val uiState: StateFlow<RechargeUiState> = combine(
        formState,
        catalogInputs,
        observeWalletBalance(),
        networkMonitor.isOnline,
    ) { form, catalog, balance, online ->
        buildUiState(
            form = form,
            catalogs = catalog.first,
            syncStatus = catalog.second,
            balance = balance,
            isOnline = online,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), RechargeUiState(form = formState.value))

    init {
        // No-op once the launch refresh ran; still needed when restored here after process death.
        syncCatalog()
    }

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
        viewModelScope.launch { events.send(RechargeEvent.StartRecharge(route)) }
    }

    private fun currentOperator(form: RechargeFormState) =
        (detectOperator(form.phoneInput) as? PhoneNumberStatus.Detected)?.operator

    private fun updateForm(transform: (RechargeFormState) -> RechargeFormState) {
        val updated = formState.updateAndGet(transform)
        savedStateHandle[KEY_FORM] = Json.encodeToString(updated)
    }

    private fun restoreForm(): RechargeFormState =
        savedStateHandle.get<String>(KEY_FORM)
            ?.let { runCatching { Json.decodeFromString<RechargeFormState>(it) }.getOrNull() }
            ?: RechargeFormState()

    private fun buildUiState(
        form: RechargeFormState,
        catalogs: List<OperatorCatalog>,
        syncStatus: CatalogSyncStatus,
        balance: WalletBalance?,
        isOnline: Boolean,
    ): RechargeUiState {
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
        return RechargeUiState(
            form = form,
            walletBalanceMmk = balance?.balanceMmk,
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
        form: RechargeFormState,
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

    private companion object {
        const val KEY_FORM = "recharge_form"
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val MAX_PHONE_INPUT_LENGTH = 16
    }
}
