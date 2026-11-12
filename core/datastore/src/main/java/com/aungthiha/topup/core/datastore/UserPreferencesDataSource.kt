package com.aungthiha.topup.core.datastore

import com.aungthiha.topup.core.datastore.model.AppLanguage
import com.aungthiha.topup.core.datastore.model.AppearanceMode
import com.aungthiha.topup.core.datastore.model.CachedWalletBalance
import kotlinx.coroutines.flow.Flow

interface UserPreferencesDataSource {
    val appearanceMode: Flow<AppearanceMode>

    suspend fun setAppearanceMode(mode: AppearanceMode)

    val appLanguage: Flow<AppLanguage>

    suspend fun setAppLanguage(language: AppLanguage)

    /** Wallet cache keyed by user id so one account's balance is never shown to another. */
    fun walletBalance(userId: String): Flow<CachedWalletBalance?>

    suspend fun saveWalletBalance(userId: String, balance: CachedWalletBalance)

    suspend fun clearWalletBalances()
}
