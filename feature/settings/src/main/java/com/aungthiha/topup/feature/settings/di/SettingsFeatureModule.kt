package com.aungthiha.topup.feature.settings.di

import com.aungthiha.topup.core.common.di.ApplicationScope
import com.aungthiha.topup.core.common.session.CurrentUserProvider
import com.aungthiha.topup.feature.settings.data.SessionCurrentUserProvider
import com.aungthiha.topup.feature.settings.data.repository.SupabaseAuthSessionRepository
import com.aungthiha.topup.feature.settings.domain.ObserveAppearanceModeUseCase
import com.aungthiha.topup.feature.settings.domain.SetAppearanceModeUseCase
import com.aungthiha.topup.feature.settings.domain.repository.AuthSessionRepository
import com.aungthiha.topup.feature.settings.domain.usecase.ObserveAuthStateUseCase
import com.aungthiha.topup.feature.settings.domain.usecase.SignOutUseCase
import com.aungthiha.topup.feature.settings.presentation.screens.SettingsViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val settingsFeatureModule = module {
    single<AuthSessionRepository> { SupabaseAuthSessionRepository(get(), get(ApplicationScope)) }
    single<CurrentUserProvider> { SessionCurrentUserProvider(get(), get(ApplicationScope)) }

    factoryOf(::ObserveAuthStateUseCase)
    factory { SignOutUseCase(repository = get(), cleaners = getAll()) }

    factoryOf(::ObserveAppearanceModeUseCase)
    factoryOf(::SetAppearanceModeUseCase)
    viewModelOf(::SettingsViewModel)
}
