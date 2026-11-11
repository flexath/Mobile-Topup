package com.aungthiha.topup.feature.settings.di

import com.aungthiha.topup.feature.settings.domain.ObserveAppearanceModeUseCase
import com.aungthiha.topup.feature.settings.domain.SetAppearanceModeUseCase
import com.aungthiha.topup.feature.settings.presentation.screens.SettingsViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val settingsFeatureModule = module {
    factoryOf(::ObserveAppearanceModeUseCase)
    factoryOf(::SetAppearanceModeUseCase)
    viewModelOf(::SettingsViewModel)
}
