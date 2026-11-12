package com.aungthiha.topup.feature.topup.domain.usecase

import com.aungthiha.topup.feature.topup.domain.amount.CustomAmountValidation
import com.aungthiha.topup.feature.topup.domain.amount.CustomAmountValidator
import com.aungthiha.topup.feature.topup.domain.model.CustomAmountRule

class ValidateCustomAmountUseCase {
    operator fun invoke(input: String, rule: CustomAmountRule): CustomAmountValidation =
        CustomAmountValidator.validate(input, rule)
}
