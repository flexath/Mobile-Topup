package com.aungthiha.topup.feature.topup.domain.eligibility

import com.aungthiha.topup.feature.topup.domain.amount.CustomAmountValidation
import com.aungthiha.topup.feature.topup.domain.model.OperatorCatalog
import com.aungthiha.topup.feature.topup.domain.model.RechargeBlocker
import com.aungthiha.topup.feature.topup.domain.model.RechargeSelection
import com.aungthiha.topup.feature.topup.domain.phone.PhoneNumberStatus

object RechargeEligibility {

    /**
     * Returns `null` when recharge may proceed, otherwise the first blocking reason.
     *
     * [customAmount] is only consulted when the custom amount field is the active selection
     * source (i.e. no catalog product is selected).
     */
    fun blocker(
        phone: PhoneNumberStatus,
        catalog: OperatorCatalog?,
        selection: RechargeSelection?,
        customAmount: CustomAmountValidation?,
        isOnline: Boolean,
    ): RechargeBlocker? {
        val detected = when (phone) {
            is PhoneNumberStatus.Detected -> phone
            is PhoneNumberStatus.Unsupported -> return RechargeBlocker.UNSUPPORTED_OPERATOR
            else -> return RechargeBlocker.INVALID_NUMBER
        }
        if (catalog == null || catalog.operator.code != detected.operator) return RechargeBlocker.CATALOG_UNAVAILABLE

        if (selection == null) {
            return if (customAmount != null && customAmount != CustomAmountValidation.Empty) {
                RechargeBlocker.INVALID_CUSTOM_AMOUNT
            } else {
                RechargeBlocker.NO_SELECTION
            }
        }
        if (selection is RechargeSelection.Product) {
            val belongsToOperator = selection.product.operatorCode == detected.operator &&
                (catalog.denominations + catalog.packages).any { it.id == selection.product.id }
            if (!belongsToOperator) return RechargeBlocker.NO_SELECTION
        }
        if (!isOnline) return RechargeBlocker.OFFLINE
        return null
    }
}
