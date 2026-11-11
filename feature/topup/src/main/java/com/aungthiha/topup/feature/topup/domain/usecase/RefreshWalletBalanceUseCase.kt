package com.aungthiha.topup.feature.topup.domain.usecase

import com.aungthiha.topup.core.common.result.DataError
import com.aungthiha.topup.core.common.result.DataResult
import com.aungthiha.topup.feature.topup.domain.model.WalletBalance
import com.aungthiha.topup.feature.topup.domain.repository.WalletRepository

class RefreshWalletBalanceUseCase(private val repository: WalletRepository) {
    suspend operator fun invoke(): DataResult<WalletBalance, DataError> = repository.refresh()
}
