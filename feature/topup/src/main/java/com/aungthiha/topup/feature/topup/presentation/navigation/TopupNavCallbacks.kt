package com.aungthiha.topup.feature.topup.presentation.navigation

sealed interface TopupNavCallbacks {
    data object NavigateUp : TopupNavCallbacks

    sealed interface TopupScreen : TopupNavCallbacks {
        data object ToSettings : TopupScreen
        data class ToProcessing(val route: TopupRoutes.RechargeProcessing) : TopupScreen
    }

    sealed interface ProcessingScreen : TopupNavCallbacks {
        /** Replace the processing step with the success screen. */
        data class ToSuccess(val transactionId: String) : ProcessingScreen

        /** Back to the Top-up screen with the user's selection intact. */
        data object BackToTopup : ProcessingScreen

        /** The backend rejected the session; the app should sign the user out. */
        data object SessionExpired : ProcessingScreen
    }

    sealed interface SuccessScreen : TopupNavCallbacks {
        data object BackToTopup : SuccessScreen
        data object ToHistory : SuccessScreen
    }
}
