package com.aungthiha.topup.feature.auth.presentation.navigation

/**
 * Successful sign-in is not a navigation intent: the app shell observes the session and swaps
 * the whole graph to the authenticated shell.
 */
sealed interface AuthNavCallbacks {
    data object NavigateUp : AuthNavCallbacks

    sealed interface LoginScreen : AuthNavCallbacks {
        data object ToRegister : LoginScreen
    }

    sealed interface RegisterScreen : AuthNavCallbacks {
        data object ToLogin : RegisterScreen
    }
}
