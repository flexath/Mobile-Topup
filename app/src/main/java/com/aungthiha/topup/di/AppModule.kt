package com.aungthiha.topup.di

import androidx.room.Room
import com.aungthiha.topup.presentation.viewmodel.MainViewModel
import com.aungthiha.topup.data.local.TopUpDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single {
        Room.databaseBuilder(androidContext(), TopUpDatabase::class.java, TopUpDatabase.NAME)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }
    single { get<TopUpDatabase>().catalogDao() }
    single { get<TopUpDatabase>().transactionDao() }

    viewModelOf(::MainViewModel)
}
