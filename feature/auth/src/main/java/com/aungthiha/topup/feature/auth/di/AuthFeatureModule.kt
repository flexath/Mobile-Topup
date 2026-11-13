package com.aungthiha.topup.feature.auth.di

import com.aungthiha.topup.feature.auth.data.repository.SupabaseAuthRepository
import com.aungthiha.topup.feature.auth.domain.repository.AuthRepository
import com.aungthiha.topup.feature.auth.domain.usecase.SignInUseCase
import com.aungthiha.topup.feature.auth.domain.usecase.SignUpUseCase
import com.aungthiha.topup.feature.auth.presentation.screens.login.LoginViewModel
import com.aungthiha.topup.feature.auth.presentation.screens.register.RegisterViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authFeatureModule = module {
    single<AuthRepository> { SupabaseAuthRepository(get()) }

    factoryOf(::SignInUseCase)
    factoryOf(::SignUpUseCase)

    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
}
