package com.aungthiha.topup.feature.topup.presentation.states

import com.aungthiha.topup.feature.topup.domain.model.OperatorCode

data class OperatorUiModel(
    val code: OperatorCode,
    val displayName: String,
    val logoUrl: String?,
)
