package com.aungthiha.topup.feature.topup.domain.usecase

import com.aungthiha.topup.core.common.result.DataResult
import com.aungthiha.topup.feature.topup.domain.model.RechargeError
import com.aungthiha.topup.feature.topup.domain.model.RechargeReceipt
import com.aungthiha.topup.feature.topup.domain.model.RechargeRequest
import com.aungthiha.topup.feature.topup.domain.repository.RechargeRepository
import com.aungthiha.topup.feature.topup.domain.repository.WalletRepository
import com.aungthiha.topup.shared.transaction.domain.usecase.SaveConfirmedTransactionUseCase

/**
 * Submits a recharge to the backend. Only a backend-confirmed success updates local state:
 * the returned transaction is cached for History and the returned balance replaces the cache.
 */
class ProcessRechargeUseCase(
    private val rechargeRepository: RechargeRepository,
    private val walletRepository: WalletRepository,
    private val saveConfirmedTransaction: SaveConfirmedTransactionUseCase,
) {
    suspend operator fun invoke(request: RechargeRequest): DataResult<RechargeReceipt, RechargeError> {
        val result = rechargeRepository.submit(request)
        if (result is DataResult.Success) {
            saveConfirmedTransaction(result.data.transaction)
            walletRepository.cacheConfirmedBalance(result.data.walletBalanceMmk)
        }
        if (result is DataResult.Error && result.error is RechargeError.InsufficientBalance) {
            (result.error as RechargeError.InsufficientBalance).balanceMmk?.let {
                walletRepository.cacheConfirmedBalance(it)
            }
        }
        return result
    }
}
