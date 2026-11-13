package com.aungthiha.topup.di

import androidx.room.Room
import com.aungthiha.topup.BuildConfig
import com.aungthiha.topup.data.local.SqlCipherPassphrase
import com.aungthiha.topup.data.local.TopUpDatabase
import com.aungthiha.topup.presentation.viewmodel.MainViewModel
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single {
        val context = androidContext()
        val builder = Room.databaseBuilder(context, TopUpDatabase::class.java, TopUpDatabase.NAME)
            .fallbackToDestructiveMigration(dropAllTables = true)
        if (!BuildConfig.DEBUG) {
            System.loadLibrary("sqlcipher")
            builder.openHelperFactory(
                SupportOpenHelperFactory(
                    SqlCipherPassphrase.getOrCreate(context),
                    null,
                    /* enableWriteAheadLogging = */ true,
                ),
            )
        }
        builder.build()
    }
    single { get<TopUpDatabase>().catalogDao() }
    single { get<TopUpDatabase>().operatorPrefixDao() }
    single { get<TopUpDatabase>().transactionDao() }

    viewModelOf(::MainViewModel)
}
