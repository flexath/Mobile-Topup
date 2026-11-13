package com.aungthiha.topup.feature.topup.domain.usecase

import com.aungthiha.topup.feature.topup.domain.model.WalletBalance
import com.aungthiha.topup.feature.topup.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow

class ObserveWalletBalanceUseCase(private val repository: WalletRepository) {
    operator fun invoke(): Flow<WalletBalance?> = repository.observeBalance()
}
