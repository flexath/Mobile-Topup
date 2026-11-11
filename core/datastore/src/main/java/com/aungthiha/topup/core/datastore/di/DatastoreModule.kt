package com.aungthiha.topup.core.datastore.di

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import com.aungthiha.topup.core.common.coroutines.DispatcherProvider
import com.aungthiha.topup.core.common.session.UserScopedDataCleaner
import com.aungthiha.topup.core.datastore.PreferencesUserDataSource
import com.aungthiha.topup.core.datastore.UserPreferencesDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val datastoreModule = module {
    single {
        PreferenceDataStoreFactory.create(
            scope = CoroutineScope(get<DispatcherProvider>().io + SupervisorJob()),
        ) {
            androidContext().preferencesDataStoreFile("topup_preferences")
        }
    }
    single<UserPreferencesDataSource> { PreferencesUserDataSource(get()) }
    single<UserScopedDataCleaner>(named("walletBalanceCache")) {
        val preferences = get<UserPreferencesDataSource>()
        UserScopedDataCleaner { preferences.clearWalletBalances() }
    }
}
