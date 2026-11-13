package com.aungthiha.topup.core.designsystem.components.navigation

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import com.aungthiha.topup.core.designsystem.theme.AppColorScheme
import com.aungthiha.topup.core.designsystem.theme.appColorScheme
import com.aungthiha.topup.core.designsystem.theme.isLightTheme

/**
 * Compact-nav glass: one translucent fill, a cached top highlight, and a hairline.
 * No backdrop blur — capturing the scene every frame would be too expensive on Android.
 */
internal fun Modifier.appLiquidGlassFill(
    shape: Shape,
    colors: AppGlassColors,
    borderWidth: Dp,
): Modifier = this
    .clip(shape)
    .drawWithCache {
        val corner = CornerRadius(size.height / 2f)
        val stroke = borderWidth.toPx()
        val highlightBrush = Brush.verticalGradient(
            0f to colors.highlight,
            0.4f to Color.Transparent,
        )
        onDrawBehind {
            drawRoundRect(color = colors.fill, cornerRadius = corner)
            drawRoundRect(brush = highlightBrush, cornerRadius = corner)
            drawRoundRect(
                color = colors.edge,
                topLeft = Offset(stroke / 2f, stroke / 2f),
                size = Size(size.width - stroke, size.height - stroke),
                cornerRadius = CornerRadius((size.height - stroke) / 2f),
                style = Stroke(stroke),
            )
        }
    }

@Immutable
internal data class AppGlassColors(
    val fill: Color,
    val highlight: Color,
    val edge: Color,
    val selectedPill: Color,
    val idlePill: Color,
    val dropTargetPill: Color,
    val selectedContent: Color,
    val unselectedContent: Color,
    val dropTargetContent: Color,
)

@Composable
internal fun rememberAppGlassColors(): AppGlassColors {
    val colors = MaterialTheme.appColorScheme
    val light = MaterialTheme.isLightTheme
    return remember(colors, light) {
        appGlassColors(colors, light)
    }
}

internal fun appGlassColors(
    colors: AppColorScheme,
    light: Boolean,
): AppGlassColors = if (light) {
    val active = colors.primary
    AppGlassColors(
        fill = colors.surface.copy(alpha = 0.88f),
        highlight = colors.surface.copy(alpha = 0.45f),
        edge = colors.outlineVariant.copy(alpha = 0.42f),
        selectedPill = colors.onSurface.copy(alpha = 0.08f),
        idlePill = Color.Transparent,
        dropTargetPill = active.copy(alpha = 0.18f),
        selectedContent = active,
        unselectedContent = colors.contentSecondary,
        dropTargetContent = active,
    )
} else {
    val active = colors.primary
    AppGlassColors(
        fill = colors.surfaceElevated.copy(alpha = 0.92f),
        highlight = colors.onSurface.copy(alpha = 0.06f),
        edge = colors.onSurface.copy(alpha = 0.12f),
        selectedPill = colors.onSurface.copy(alpha = 0.11f),
        idlePill = Color.Transparent,
        dropTargetPill = active.copy(alpha = 0.22f),
        selectedContent = active,
        unselectedContent = colors.onSurface.copy(alpha = 0.88f),
        dropTargetContent = active,
    )
}
