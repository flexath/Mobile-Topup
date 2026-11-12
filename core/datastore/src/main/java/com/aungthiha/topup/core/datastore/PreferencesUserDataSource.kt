package com.aungthiha.topup.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.aungthiha.topup.core.datastore.model.AppearanceMode
import com.aungthiha.topup.core.datastore.model.CachedWalletBalance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal class PreferencesUserDataSource(
    private val dataStore: DataStore<Preferences>,
) : UserPreferencesDataSource {

    override val appearanceMode: Flow<AppearanceMode> = dataStore.data
        .map { preferences ->
            preferences[APPEARANCE_MODE]
                ?.let { stored -> AppearanceMode.entries.firstOrNull { it.name == stored } }
                ?: AppearanceMode.SYSTEM
        }
        .distinctUntilChanged()

    override suspend fun setAppearanceMode(mode: AppearanceMode) {
        dataStore.edit { it[APPEARANCE_MODE] = mode.name }
    }

    override fun walletBalance(userId: String): Flow<CachedWalletBalance?> = dataStore.data
        .map { preferences ->
            val balance = preferences[walletBalanceKey(userId)] ?: return@map null
            CachedWalletBalance(
                balanceMmk = balance,
                syncedAtMillis = preferences[walletSyncedAtKey(userId)] ?: 0L,
            )
        }
        .distinctUntilChanged()

    override suspend fun saveWalletBalance(userId: String, balance: CachedWalletBalance) {
        dataStore.edit {
            it[walletBalanceKey(userId)] = balance.balanceMmk
            it[walletSyncedAtKey(userId)] = balance.syncedAtMillis
        }
    }

    override suspend fun clearWalletBalances() {
        dataStore.edit { preferences ->
            preferences.asMap().keys
                .filter { it.name.startsWith(WALLET_KEY_PREFIX) }
                .forEach { preferences.remove(it) }
        }
    }

    private fun walletBalanceKey(userId: String) = longPreferencesKey("${WALLET_KEY_PREFIX}balance_$userId")

    private fun walletSyncedAtKey(userId: String) = longPreferencesKey("${WALLET_KEY_PREFIX}synced_at_$userId")

    private companion object {
        val APPEARANCE_MODE = stringPreferencesKey("appearance_mode")
        const val WALLET_KEY_PREFIX = "wallet_"
    }
}
