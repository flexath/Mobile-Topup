package com.aungthiha.topup.shared.transaction.di

import com.aungthiha.topup.core.common.session.UserScopedDataCleaner
import com.aungthiha.topup.shared.transaction.data.local.cache.TransactionCacheImpl
import com.aungthiha.topup.shared.transaction.data.remote.service.SupabaseTransactionRemoteService
import com.aungthiha.topup.shared.transaction.data.remote.service.TransactionRemoteService
import com.aungthiha.topup.shared.transaction.data.repository.TransactionRepositoryImpl
import com.aungthiha.topup.shared.transaction.domain.cache.TransactionCache
import com.aungthiha.topup.shared.transaction.domain.repository.TransactionRepository
import com.aungthiha.topup.shared.transaction.domain.usecase.ObserveTransactionCountUseCase
import com.aungthiha.topup.shared.transaction.domain.usecase.ObserveTransactionHistoryUseCase
import com.aungthiha.topup.shared.transaction.domain.usecase.ObserveTransactionUseCase
import com.aungthiha.topup.shared.transaction.domain.usecase.SaveConfirmedTransactionUseCase
import com.aungthiha.topup.shared.transaction.domain.usecase.SyncTransactionPageUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val transactionSharedModule = module {
    single<TransactionRemoteService> { SupabaseTransactionRemoteService(get()) }
    single<TransactionCache> { TransactionCacheImpl(dao = get()) }
    single<TransactionRepository> {
        TransactionRepositoryImpl(
            cache = get(),
            remote = get(),
            currentUser = get(),
            storage = get(),
            dispatchers = get(),
        )
    }
    single<UserScopedDataCleaner>(named("transactionCache")) {
        val repository = get<TransactionRepository>()
        UserScopedDataCleaner { repository.clearCache() }
    }

    factoryOf(::ObserveTransactionHistoryUseCase)
    factoryOf(::ObserveTransactionCountUseCase)
    factoryOf(::ObserveTransactionUseCase)
    factoryOf(::SyncTransactionPageUseCase)
    factoryOf(::SaveConfirmedTransactionUseCase)
}
