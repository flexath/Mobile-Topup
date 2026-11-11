package com.aungthiha.topup.feature.topup.domain.usecase

import com.aungthiha.topup.core.common.result.DataError
import com.aungthiha.topup.core.common.result.DataResult
import com.aungthiha.topup.feature.topup.domain.amount.CustomAmountValidation
import com.aungthiha.topup.feature.topup.domain.amount.CustomAmountValidator
import com.aungthiha.topup.feature.topup.domain.eligibility.RechargeEligibility
import com.aungthiha.topup.feature.topup.domain.model.CatalogSyncStatus
import com.aungthiha.topup.feature.topup.domain.model.CustomAmountRule
import com.aungthiha.topup.feature.topup.domain.model.OperatorCatalog
import com.aungthiha.topup.feature.topup.domain.model.RechargeBlocker
import com.aungthiha.topup.feature.topup.domain.model.RechargeError
import com.aungthiha.topup.feature.topup.domain.model.RechargeReceipt
import com.aungthiha.topup.feature.topup.domain.model.RechargeRequest
import com.aungthiha.topup.feature.topup.domain.model.RechargeSelection
import com.aungthiha.topup.feature.topup.domain.model.WalletBalance
import com.aungthiha.topup.feature.topup.domain.phone.MyanmarPhoneNumber
import com.aungthiha.topup.feature.topup.domain.phone.OperatorPrefixTable
import com.aungthiha.topup.feature.topup.domain.phone.PhoneNumberStatus
import com.aungthiha.topup.feature.topup.domain.repository.CatalogRepository
import com.aungthiha.topup.feature.topup.domain.repository.RechargeRepository
import com.aungthiha.topup.feature.topup.domain.repository.WalletRepository
import com.aungthiha.topup.shared.transaction.domain.usecase.SaveConfirmedTransactionUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class DetectOperatorUseCase(
    private val prefixes: OperatorPrefixTable = OperatorPrefixTable.Default,
) {
    operator fun invoke(rawNumber: String): PhoneNumberStatus = MyanmarPhoneNumber.classify(rawNumber, prefixes)
}

class ValidateCustomAmountUseCase {
    operator fun invoke(input: String, rule: CustomAmountRule): CustomAmountValidation =
        CustomAmountValidator.validate(input, rule)
}

class EvaluateRechargeEligibilityUseCase {
    operator fun invoke(
        phone: PhoneNumberStatus,
        catalog: OperatorCatalog?,
        selection: RechargeSelection?,
        customAmount: CustomAmountValidation?,
        isOnline: Boolean,
    ): RechargeBlocker? = RechargeEligibility.blocker(phone, catalog, selection, customAmount, isOnline)
}

class ObserveCatalogUseCase(private val repository: CatalogRepository) {
    operator fun invoke(): Flow<List<OperatorCatalog>> = repository.observeCatalog()
}

class ObserveCatalogSyncStatusUseCase(private val repository: CatalogRepository) {
    operator fun invoke(): StateFlow<CatalogSyncStatus> = repository.syncStatus
}

class SyncCatalogUseCase(private val repository: CatalogRepository) {
    /** [retry] = false starts the once-per-launch refresh; true is the explicit Retry action. */
    operator fun invoke(retry: Boolean = false) {
        if (retry) repository.retryRefresh() else repository.startLaunchRefresh()
    }
}

class ObserveWalletBalanceUseCase(private val repository: WalletRepository) {
    operator fun invoke(): Flow<WalletBalance?> = repository.observeBalance()
}

class RefreshWalletBalanceUseCase(private val repository: WalletRepository) {
    suspend operator fun invoke(): DataResult<WalletBalance, DataError> = repository.refresh()
}

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
