package com.aungthiha.topup.feature.auth.di

import com.aungthiha.topup.feature.auth.presentation.screens.login.LoginViewModel
import com.aungthiha.topup.feature.auth.presentation.screens.register.RegisterViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authFeatureModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
}
