package com.aungthiha.topup.core.datastore

import com.aungthiha.topup.core.datastore.model.AppLanguage
import kotlinx.coroutines.flow.Flow

class LanguageManager(
    private val preferences: UserPreferencesDataSource,
) {
    fun getAppLanguage(): Flow<AppLanguage> = preferences.appLanguage

    suspend fun setAppLanguage(language: AppLanguage) {
        preferences.setAppLanguage(language)
    }
}
