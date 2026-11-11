package com.aungthiha.topup.feature.topup.domain.repository

import com.aungthiha.topup.core.common.result.DataError
import com.aungthiha.topup.core.common.result.DataResult
import com.aungthiha.topup.feature.topup.domain.model.CatalogSyncStatus
import com.aungthiha.topup.feature.topup.domain.model.OperatorCatalog
import com.aungthiha.topup.feature.topup.domain.model.RechargeError
import com.aungthiha.topup.feature.topup.domain.model.RechargeReceipt
import com.aungthiha.topup.feature.topup.domain.model.RechargeRequest
import com.aungthiha.topup.feature.topup.domain.model.WalletBalance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/** Unified telecom catalog. Room is the source of truth; Supabase refreshes it in the background. */
interface CatalogRepository {
    fun observeCatalog(): Flow<List<OperatorCatalog>>

    val syncStatus: StateFlow<CatalogSyncStatus>

    /** Starts the cold-launch refresh once per process; later calls are no-ops. */
    fun startLaunchRefresh()

    /** Explicit retry (used only when no cached catalog exists). Superseded requests never write. */
    fun retryRefresh()
}

/** Wallet of the signed-in user: cached per user in DataStore, authoritative in Supabase. */
interface WalletRepository {
    fun observeBalance(): Flow<WalletBalance?>

    suspend fun refresh(): DataResult<WalletBalance, DataError>

    /** Stores a balance the recharge backend just confirmed. Never derived locally. */
    suspend fun cacheConfirmedBalance(balanceMmk: Long)
}

interface RechargeRepository {
    suspend fun submit(request: RechargeRequest): DataResult<RechargeReceipt, RechargeError>
}
