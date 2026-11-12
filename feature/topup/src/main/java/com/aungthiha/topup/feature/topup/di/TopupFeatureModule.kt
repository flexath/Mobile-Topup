package com.aungthiha.topup.feature.topup.di

import com.aungthiha.topup.core.common.di.ApplicationScope
import com.aungthiha.topup.feature.topup.data.remote.service.SupabaseTopupRemoteService
import com.aungthiha.topup.feature.topup.data.remote.service.TopupRemoteService
import com.aungthiha.topup.feature.topup.data.repository.CatalogRepositoryImpl
import com.aungthiha.topup.feature.topup.data.repository.OperatorPrefixRepositoryImpl
import com.aungthiha.topup.feature.topup.data.repository.RechargeRepositoryImpl
import com.aungthiha.topup.feature.topup.data.repository.WalletRepositoryImpl
import com.aungthiha.topup.feature.topup.domain.repository.CatalogRepository
import com.aungthiha.topup.feature.topup.domain.repository.OperatorPrefixRepository
import com.aungthiha.topup.feature.topup.domain.repository.RechargeRepository
import com.aungthiha.topup.feature.topup.domain.repository.WalletRepository
import com.aungthiha.topup.feature.topup.domain.usecase.DetectOperatorUseCase
import com.aungthiha.topup.feature.topup.domain.usecase.EvaluateRechargeEligibilityUseCase
import com.aungthiha.topup.feature.topup.domain.usecase.ObserveCatalogSyncStatusUseCase
import com.aungthiha.topup.feature.topup.domain.usecase.ObserveCatalogUseCase
import com.aungthiha.topup.feature.topup.domain.usecase.ObserveOperatorPrefixesUseCase
import com.aungthiha.topup.feature.topup.domain.usecase.ObserveWalletBalanceUseCase
import com.aungthiha.topup.feature.topup.domain.usecase.ProcessRechargeUseCase
import com.aungthiha.topup.feature.topup.domain.usecase.RefreshWalletBalanceUseCase
import com.aungthiha.topup.feature.topup.domain.usecase.SyncCatalogUseCase
import com.aungthiha.topup.feature.topup.domain.usecase.SyncOperatorPrefixesUseCase
import com.aungthiha.topup.feature.topup.domain.usecase.ValidateCustomAmountUseCase
import com.aungthiha.topup.feature.topup.presentation.screens.processing.RechargeProcessingViewModel
import com.aungthiha.topup.feature.topup.presentation.screens.recharge.RechargeViewModel
import com.aungthiha.topup.feature.topup.presentation.screens.success.RechargeSuccessViewModel
import com.aungthiha.topup.feature.topup.presentation.screens.topup.TopupViewModel
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val topupFeatureModule = module {
    single<TopupRemoteService> { SupabaseTopupRemoteService(get()) }

    single<CatalogRepository> {
        CatalogRepositoryImpl(
            dao = get(),
            remote = get(),
            storage = get(),
            appScope = get(ApplicationScope),
            dispatchers = get(),
        )
    }
    single<OperatorPrefixRepository> {
        OperatorPrefixRepositoryImpl(
            dao = get(),
            remote = get(),
            appScope = get(ApplicationScope),
            dispatchers = get(),
        )
    }
    single<WalletRepository> { WalletRepositoryImpl(remote = get(), preferences = get(), currentUser = get()) }
    single<RechargeRepository> {
        RechargeRepositoryImpl(
            remote = get(),
            storage = get(),
            json = Json { ignoreUnknownKeys = true },
        )
    }

    factory { DetectOperatorUseCase() }
    factory { ValidateCustomAmountUseCase() }
    factory { EvaluateRechargeEligibilityUseCase() }
    factoryOf(::ObserveCatalogUseCase)
    factoryOf(::ObserveCatalogSyncStatusUseCase)
    factoryOf(::SyncCatalogUseCase)
    factoryOf(::ObserveOperatorPrefixesUseCase)
    factoryOf(::SyncOperatorPrefixesUseCase)
    factoryOf(::ObserveWalletBalanceUseCase)
    factoryOf(::RefreshWalletBalanceUseCase)
    factoryOf(::ProcessRechargeUseCase)

    viewModel {
        TopupViewModel(
            observeCatalog = get(),
            syncCatalog = get(),
            syncOperatorPrefixes = get(),
            observeWalletBalance = get(),
            refreshWalletBalance = get(),
            networkMonitor = get(),
        )
    }
    viewModel {
        RechargeViewModel(
            savedStateHandle = get(),
            observeCatalog = get(),
            observeCatalogSyncStatus = get(),
            syncCatalog = get(),
            syncOperatorPrefixes = get(),
            observeOperatorPrefixes = get(),
            observeWalletBalance = get(),
            detectOperator = get(),
            validateCustomAmount = get(),
            evaluateEligibility = get(),
            networkMonitor = get(),
        )
    }
    viewModelOf(::RechargeProcessingViewModel)
    viewModelOf(::RechargeSuccessViewModel)
}
