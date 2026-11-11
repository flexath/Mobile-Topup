package com.aungthiha.topup.feature.topup.domain.usecase

import com.aungthiha.topup.feature.topup.domain.amount.CustomAmountValidation
import com.aungthiha.topup.feature.topup.domain.eligibility.RechargeEligibility
import com.aungthiha.topup.feature.topup.domain.model.OperatorCatalog
import com.aungthiha.topup.feature.topup.domain.model.RechargeBlocker
import com.aungthiha.topup.feature.topup.domain.model.RechargeSelection
import com.aungthiha.topup.feature.topup.domain.phone.PhoneNumberStatus

class EvaluateRechargeEligibilityUseCase {
    operator fun invoke(
        phone: PhoneNumberStatus,
        catalog: OperatorCatalog?,
        selection: RechargeSelection?,
        customAmount: CustomAmountValidation?,
        isOnline: Boolean,
    ): RechargeBlocker? = RechargeEligibility.blocker(phone, catalog, selection, customAmount, isOnline)
}
