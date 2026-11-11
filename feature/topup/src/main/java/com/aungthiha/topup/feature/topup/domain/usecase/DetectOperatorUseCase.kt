package com.aungthiha.topup.feature.topup.domain.usecase

import com.aungthiha.topup.feature.topup.domain.phone.MyanmarPhoneNumber
import com.aungthiha.topup.feature.topup.domain.phone.OperatorPrefixTable
import com.aungthiha.topup.feature.topup.domain.phone.PhoneNumberStatus

class DetectOperatorUseCase(
    private val prefixes: OperatorPrefixTable = OperatorPrefixTable.Default,
) {
    operator fun invoke(rawNumber: String): PhoneNumberStatus = MyanmarPhoneNumber.classify(rawNumber, prefixes)
}
