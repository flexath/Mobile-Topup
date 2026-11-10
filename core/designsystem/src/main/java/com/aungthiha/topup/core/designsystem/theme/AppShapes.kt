package com.aungthiha.topup.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape
import com.aungthiha.topup.core.designsystem.utils.calculatedDp

@Immutable
data class AppShapes(
    val extraSmall: Shape = RoundedCornerShape(4.calculatedDp),
    val small: Shape = RoundedCornerShape(8.calculatedDp),
    val medium: Shape = RoundedCornerShape(12.calculatedDp),
    val large: Shape = RoundedCornerShape(16.calculatedDp),
    val extraLarge: Shape = RoundedCornerShape(20.calculatedDp),
    val pill: Shape = RoundedCornerShape(50),
)
