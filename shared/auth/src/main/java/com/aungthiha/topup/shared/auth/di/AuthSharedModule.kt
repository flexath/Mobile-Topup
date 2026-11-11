package com.aungthiha.topup.shared.auth.di

import com.aungthiha.topup.core.common.di.ApplicationScope
import com.aungthiha.topup.core.common.session.CurrentUserProvider
import com.aungthiha.topup.shared.auth.data.SessionCurrentUserProvider
import com.aungthiha.topup.shared.auth.data.SupabaseAuthRepository
import com.aungthiha.topup.shared.auth.domain.repository.AuthRepository
import com.aungthiha.topup.shared.auth.domain.usecase.ObserveAuthStateUseCase
import com.aungthiha.topup.shared.auth.domain.usecase.SignInUseCase
import com.aungthiha.topup.shared.auth.domain.usecase.SignOutUseCase
import com.aungthiha.topup.shared.auth.domain.usecase.SignUpUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val authSharedModule = module {
    single<AuthRepository> { SupabaseAuthRepository(get(), get(ApplicationScope)) }
    single<CurrentUserProvider> { SessionCurrentUserProvider(get(), get(ApplicationScope)) }

    factoryOf(::ObserveAuthStateUseCase)
    factoryOf(::SignInUseCase)
    factoryOf(::SignUpUseCase)
    factory { SignOutUseCase(repository = get(), cleaners = getAll()) }
}
