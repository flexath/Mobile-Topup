package com.aungthiha.topup

import android.app.Application
import android.content.pm.ApplicationInfo
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy.Builder
import com.aungthiha.topup.core.common.di.commonModule
import com.aungthiha.topup.core.datastore.di.datastoreModule
import com.aungthiha.topup.core.network.SupabaseConfig
import com.aungthiha.topup.core.network.di.networkModule
import com.aungthiha.topup.di.appModule
import com.aungthiha.topup.feature.auth.di.authFeatureModule
import com.aungthiha.topup.feature.settings.di.settingsFeatureModule
import com.aungthiha.topup.feature.topup.di.topupFeatureModule
import com.aungthiha.topup.feature.transactionhistory.di.transactionHistoryFeatureModule
import com.aungthiha.topup.shared.auth.di.authSharedModule
import com.aungthiha.topup.shared.transaction.di.transactionSharedModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class TopUpApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        setStrictModePolicy()
        startKoin {
            androidContext(this@TopUpApplication)
            modules(
                commonModule,
                networkModule(
                    SupabaseConfig(url = BuildConfig.SUPABASE_URL, clientKey = BuildConfig.SUPABASE_ANON_KEY),
                ),
                datastoreModule,
                authSharedModule,
                transactionSharedModule,
                authFeatureModule,
                topupFeatureModule,
                transactionHistoryFeatureModule,
                settingsFeatureModule,
                appModule,
            )
        }
    }

    private fun isDebuggable(): Boolean {
        return 0 != applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
    }

    private fun setStrictModePolicy() {
        if (isDebuggable()) {
            StrictMode.setThreadPolicy(
                Builder().detectAll().penaltyLog().build(),
            )
        }
    }
}
