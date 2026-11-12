package com.aungthiha.topup.feature.settings.domain.usecase

import com.aungthiha.topup.core.datastore.UserPreferencesDataSource
import com.aungthiha.topup.core.datastore.model.AppearanceMode
import kotlinx.coroutines.flow.Flow

/** Theme preference (System by default), persisted in DataStore. */
class ObserveAppearanceModeUseCase(private val preferences: UserPreferencesDataSource) {
    operator fun invoke(): Flow<AppearanceMode> = preferences.appearanceMode
}

class SetAppearanceModeUseCase(private val preferences: UserPreferencesDataSource) {
    suspend operator fun invoke(mode: AppearanceMode) = preferences.setAppearanceMode(mode)
}
