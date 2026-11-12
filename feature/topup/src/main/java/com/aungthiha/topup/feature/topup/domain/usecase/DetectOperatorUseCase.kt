package com.aungthiha.topup.feature.topup.domain.usecase

import com.aungthiha.topup.feature.topup.domain.phone.MyanmarPhoneNumber
import com.aungthiha.topup.feature.topup.domain.phone.OperatorPrefixTable
import com.aungthiha.topup.feature.topup.domain.phone.PhoneNumberStatus

class DetectOperatorUseCase {
    operator fun invoke(
        rawNumber: String,
        prefixes: OperatorPrefixTable = OperatorPrefixTable.Default,
    ): PhoneNumberStatus = MyanmarPhoneNumber.classify(rawNumber, prefixes)
}
