package com.aungthiha.topup.feature.topup.presentation.navigation

sealed interface TopupNavCallbacks {
    data object NavigateUp : TopupNavCallbacks

    sealed interface TopupScreen : TopupNavCallbacks {
        data object ToHistory : TopupScreen
    }
}
