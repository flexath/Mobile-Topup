package com.aungthiha.topup.feature.topup.domain.repository

import com.aungthiha.topup.core.common.result.DataError
import com.aungthiha.topup.core.common.result.DataResult
import com.aungthiha.topup.feature.topup.domain.model.WalletBalance
import kotlinx.coroutines.flow.Flow

/** Wallet of the signed-in user: cached per user in DataStore, authoritative in Supabase. */
interface WalletRepository {
    fun observeBalance(): Flow<WalletBalance?>

    suspend fun refresh(): DataResult<WalletBalance, DataError>

    /** Stores a balance the recharge backend just confirmed. Never derived locally. */
    suspend fun cacheConfirmedBalance(balanceMmk: Long)
}
