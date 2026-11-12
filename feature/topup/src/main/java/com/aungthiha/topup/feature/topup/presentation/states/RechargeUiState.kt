package com.aungthiha.topup.feature.topup.presentation.states

import com.aungthiha.topup.core.common.result.DataError
import com.aungthiha.topup.feature.topup.domain.amount.CustomAmountValidation
import com.aungthiha.topup.feature.topup.domain.model.CatalogProduct
import com.aungthiha.topup.feature.topup.domain.model.CustomAmountRule
import com.aungthiha.topup.feature.topup.domain.model.OperatorCode
import com.aungthiha.topup.feature.topup.domain.model.RechargeBlocker
import com.aungthiha.topup.feature.topup.domain.model.RechargeSelection
import com.aungthiha.topup.feature.topup.domain.model.RechargeTab
import com.aungthiha.topup.feature.topup.domain.phone.PhoneNumberStatus
import com.aungthiha.topup.feature.topup.presentation.navigation.TopupRoutes
import kotlinx.serialization.Serializable

/** User-entered state. Persisted in SavedStateHandle so it survives rotation and process death. */
@Serializable
data class RechargeFormState(
    val phoneInput: String = "",
    val activeTab: RechargeTab = RechargeTab.DENOMINATIONS,
    val selectedProductId: String? = null,
    val isCustomAmountVisible: Boolean = false,
    val customAmountInput: String = "",
    /** Operator the current selection was made for; a different detected operator clears it. */
    val selectionOperator: OperatorCode? = null,
    val isConfirmationVisible: Boolean = false,
)

sealed interface CatalogUiState {
    data object Loading : CatalogUiState
    data class Unavailable(val error: DataError?) : CatalogUiState
    data object Ready : CatalogUiState
}

data class RechargeConfirmationUi(
    val phoneNumber: String,
    val operator: OperatorUiModel,
    val selection: RechargeSelection,
    val priceMmk: Long,
    val walletBalanceMmk: Long?,
) {
    val isBalanceLikelyInsufficient: Boolean
        get() = walletBalanceMmk != null && walletBalanceMmk < priceMmk
}

data class RechargeUiState(
    val form: RechargeFormState = RechargeFormState(),
    /** Cached balance; refreshed by the wallet overview, never derived locally. */
    val walletBalanceMmk: Long? = null,
    val phoneStatus: PhoneNumberStatus = PhoneNumberStatus.Empty,
    val operator: OperatorUiModel? = null,
    val catalogState: CatalogUiState = CatalogUiState.Loading,
    val isCatalogStale: Boolean = false,
    val denominations: List<CatalogProduct> = emptyList(),
    val packages: List<CatalogProduct> = emptyList(),
    val customAmountRule: CustomAmountRule? = null,
    val customAmountValidation: CustomAmountValidation = CustomAmountValidation.Empty,
    val selection: RechargeSelection? = null,
    val isOnline: Boolean = true,
    val blocker: RechargeBlocker? = RechargeBlocker.INVALID_NUMBER,
    val confirmation: RechargeConfirmationUi? = null,
) {
    val canProceed: Boolean get() = blocker == null
    val hasDemoPackages: Boolean get() = packages.any { it.isDemo }
}

sealed interface RechargeEvent {
    data class StartRecharge(val route: TopupRoutes.RechargeProcessing) : RechargeEvent
}
