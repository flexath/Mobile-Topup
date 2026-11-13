package com.aungthiha.topup.feature.topup.presentation.navigation

import com.aungthiha.topup.core.common.navigation.AppRoutes
import kotlinx.serialization.Serializable

sealed class TopupRoutes : AppRoutes {
    @Serializable
    data object GraphRoute : TopupRoutes()

    /** Wallet overview and tab root. */
    @Serializable
    data object TopupScreen : TopupRoutes()

    /** Mobile number entry, operator detection, denomination / package selection. */
    @Serializable
    data object RechargeScreen : TopupRoutes()

    /**
     * Full-screen processing step. Carries the ids the backend needs plus display-only labels;
     * the backend re-validates everything and decides the price.
     */
    @Serializable
    data class RechargeProcessing(
        val phoneNumber: String,
        val operatorCode: String,
        val operatorName: String,
        val productId: String? = null,
        val customAmountMmk: Long? = null,
        val displayAmountMmk: Long,
        val idempotencyKey: String,
    ) : TopupRoutes()

    @Serializable
    data class RechargeSuccess(val transactionId: String) : TopupRoutes()
}
