package com.aungthiha.topup.feature.topup.presentation.actions

import com.aungthiha.topup.feature.topup.domain.model.RechargeTab

data class RechargeActions(
    val onNavigateUp: () -> Unit = {},
    val onPhoneNumberChange: (String) -> Unit = {},
    val onTabSelected: (RechargeTab) -> Unit = {},
    val onProductSelected: (String) -> Unit = {},
    val onToggleCustomAmount: () -> Unit = {},
    val onCustomAmountChange: (String) -> Unit = {},
    val onRetryCatalog: () -> Unit = {},
    val onProceed: () -> Unit = {},
    val onConfirm: () -> Unit = {},
    val onDismissConfirmation: () -> Unit = {},
)