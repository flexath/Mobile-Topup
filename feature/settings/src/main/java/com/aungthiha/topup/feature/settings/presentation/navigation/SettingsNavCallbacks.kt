package com.aungthiha.topup.feature.settings.presentation.navigation

/** Logout is not a navigation intent: the app shell reacts to the session ending. */
sealed interface SettingsNavCallbacks {
    data object NavigateUp : SettingsNavCallbacks
}
