package com.aungthiha.topup.feature.transactionhistory.di

import com.aungthiha.topup.feature.transactionhistory.presentation.screens.detail.TransactionDetailViewModel
import com.aungthiha.topup.feature.transactionhistory.presentation.screens.history.TransactionHistoryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val transactionHistoryFeatureModule = module {
    viewModel {
        TransactionHistoryViewModel(
            savedStateHandle = get(),
            observeHistory = get(),
            observeCount = get(),
            syncPage = get(),
            networkMonitor = get(),
        )
    }
    viewModelOf(::TransactionDetailViewModel)
}
