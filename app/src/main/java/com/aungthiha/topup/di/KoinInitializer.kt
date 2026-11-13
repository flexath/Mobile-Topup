package com.aungthiha.topup.di

import android.app.Application
import com.aungthiha.topup.BuildConfig
import com.aungthiha.topup.core.common.di.commonModule
import com.aungthiha.topup.core.datastore.di.datastoreModule
import com.aungthiha.topup.core.network.SupabaseConfig
import com.aungthiha.topup.core.network.di.networkModule
import com.aungthiha.topup.feature.auth.di.authFeatureModule
import com.aungthiha.topup.feature.settings.di.settingsFeatureModule
import com.aungthiha.topup.feature.topup.di.topupFeatureModule
import com.aungthiha.topup.feature.transactionhistory.di.transactionHistoryFeatureModule
import com.aungthiha.topup.shared.transaction.di.transactionSharedModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

fun Application.initKoin() {
    startKoin {
        androidContext(this@initKoin)
        modules(
            commonModule,
            networkModule(
                SupabaseConfig(url = BuildConfig.SUPABASE_URL, clientKey = BuildConfig.SUPABASE_ANON_KEY),
            ),
            datastoreModule,
            appModule,
            transactionSharedModule,
            authFeatureModule,
            topupFeatureModule,
            transactionHistoryFeatureModule,
            settingsFeatureModule,
        )
    }
}
