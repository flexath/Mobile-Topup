package com.aungthiha.topup.core.designsystem.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

/**
 * TopUp semantic color tokens.
 *
 * Colors must be consumed by role rather than by a raw palette value. Every foreground/background
 * pair is designed to meet WCAG AA contrast for normal text.
 */
@Immutable
data class AppColorScheme(
    /** Brand color for prominent actions, active controls, and selected states. */
    val primary: Color,
    /** Text and icons displayed directly on [primary]. */
    val onPrimary: Color,
    /** Lower-emphasis brand container for chips, cards, and selected areas. */
    val primaryContainer: Color,
    /** Text and icons displayed directly on [primaryContainer]. */
    val onPrimaryContainer: Color,
    /** Supporting accent for secondary actions and complementary UI elements. */
    val secondary: Color,
    /** Text and icons displayed directly on [secondary]. */
    val onSecondary: Color,
    /** Lower-emphasis supporting container for secondary content. */
    val secondaryContainer: Color,
    /** Text and icons displayed directly on [secondaryContainer]. */
    val onSecondaryContainer: Color,
    /**
     * Complementary brand accent for progress, selection, and supporting highlights.
     *
     * Use for fills, tracks, and badges. Do not use as text or hairline outlines on neutral
     * surfaces in light theme — [onComplementaryContainer] is the accessible complementary-hued
     * content color on those surfaces.
     */
    val complementary: Color,
    /** Text and icons displayed directly on [complementary]. */
    val onComplementary: Color,
    /** Lower-emphasis complementary surface for chips, icon wells, and selected areas. */
    val complementaryContainer: Color,
    /**
     * Text and icons displayed directly on [complementaryContainer].
     *
     * Also the complementary-hued color for labels, icons, and outlines on neutral surfaces,
     * where [complementary] itself would not meet contrast requirements.
     */
    val onComplementaryContainer: Color,
    /** Base color behind the app's scrollable content and screens. */
    val background: Color,
    /** Default content color displayed directly on [background]. */
    val onBackground: Color,
    /** Base color for cards, sheets, menus, and other components. */
    val surface: Color,
    /** Default text and icons displayed directly on [surface]. */
    val onSurface: Color,
    /** Surface color for visually elevated cards, dialogs, and popovers. */
    val surfaceElevated: Color,
    /** Contrasting surface used to separate grouped or supporting content. */
    val surfaceVariant: Color,
    /** Text and icons displayed directly on [surfaceVariant]. */
    val onSurfaceVariant: Color,
    /** High-emphasis borders, dividers, and focused component outlines. */
    val outline: Color,
    /** Subtle separators and low-emphasis component outlines. */
    val outlineVariant: Color,
    /** Highest-emphasis text and icons on neutral backgrounds and surfaces. */
    val contentPrimary: Color,
    /** Supporting text, metadata, and medium-emphasis icons. */
    val contentSecondary: Color,
    /** Disabled text, icons, and unavailable controls. */
    val contentDisabled: Color,
    /** Positive states such as completed, connected, or successful actions. */
    val success: Color,
    /** Text and icons displayed directly on [success]. */
    val onSuccess: Color,
    /** Lower-emphasis background for positive status messages. */
    val successContainer: Color,
    /** Text and icons displayed directly on [successContainer]. */
    val onSuccessContainer: Color,
    /** Cautionary states requiring attention without indicating failure. */
    val warning: Color,
    /** Text and icons displayed directly on [warning]. */
    val onWarning: Color,
    /** Lower-emphasis background for cautionary messages. */
    val warningContainer: Color,
    /** Text and icons displayed directly on [warningContainer]. */
    val onWarningContainer: Color,
    /** Destructive actions, validation failures, and critical error states. */
    val error: Color,
    /** Text and icons displayed directly on [error]. */
    val onError: Color,
    /** Lower-emphasis background for error and validation messages. */
    val errorContainer: Color,
    /** Text and icons displayed directly on [errorContainer]. */
    val onErrorContainer: Color,
    /** Informational states, neutral announcements, and contextual guidance. */
    val info: Color,
    /** Text and icons displayed directly on [info]. */
    val onInfo: Color,
    /** Lower-emphasis background for informational messages. */
    val infoContainer: Color,
    /** Text and icons displayed directly on [infoContainer]. */
    val onInfoContainer: Color,
)

/** Light palette — Aura Blue / Purple / Gold. */
val AppLightColorScheme = AppColorScheme(
    // Primary - Aura Blue
    primary = Color(0xFF2970FF),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFECEFF4),
    onPrimaryContainer = Color(0xFF2970FF),

    // Secondary - Aura Purple
    secondary = Color(0xFF9C6BFF),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF2F2F7),
    onSecondaryContainer = Color(0xFF9C6BFF),

    // Complementary - Aura Rating Gold
    complementary = Color(0xFFFFD600),
    onComplementary = Color(0xFF0A0A0A),
    complementaryContainer = Color(0xFFF0F2F8),
    onComplementaryContainer = Color(0xFF4A4A4A),

    // Background
    background = Color(0xFFF9FAFB),
    onBackground = Color(0xFF0A0A0A),

    // Surfaces
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0A0A0A),
    surfaceElevated = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF4F6FA),
    onSurfaceVariant = Color(0xFF4A4A4A),

    // Outlines
    outline = Color(0xFFD6D9E0),
    outlineVariant = Color(0xFFE5E7EB),

    // Content
    contentPrimary = Color(0xFF0A0A0A),
    contentSecondary = Color(0xFF4A4A4A),
    contentDisabled = Color(0xFF8C8C8C),

    // Success
    success = Color(0xFF2ECC71),
    onSuccess = Color(0xFFFFFFFF),
    successContainer = Color(0xFFECEFF4),
    onSuccessContainer = Color(0xFF2ECC71),

    // Warning - Aura Growth Indicator
    warning = Color(0xFFFF9800),
    onWarning = Color(0xFFFFFFFF),
    warningContainer = Color(0xFFF0F2F8),
    onWarningContainer = Color(0xFFFF9800),

    // Error
    error = Color(0xFFE74C3C),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF2F2F7),
    onErrorContainer = Color(0xFFE74C3C),

    // Info - Aura Blue
    info = Color(0xFF2970FF),
    onInfo = Color(0xFFFFFFFF),
    infoContainer = Color(0xFFECEFF4),
    onInfoContainer = Color(0xFF2970FF),
)

/** Dark palette — Aura Blue / Purple / Gold. */
val AppDarkColorScheme = AppColorScheme(
    // Primary - Aura Blue
    primary = Color(0xFF2970FF),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF1A1F29),
    onPrimaryContainer = Color(0xFF2970FF),

    // Secondary - Aura Purple
    secondary = Color(0xFFB68CFF),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF2C2C2E),
    onSecondaryContainer = Color(0xFFB68CFF),

    // Complementary - Aura Rating Gold
    complementary = Color(0xFFFFD600),
    onComplementary = Color(0xFF0E1117),
    complementaryContainer = Color(0xFF1F2633),
    onComplementaryContainer = Color(0xFFD0D0D0),

    // Background
    background = Color(0xFF0E1117),
    onBackground = Color(0xFFFFFFFF),

    // Surfaces
    surface = Color(0xFF161B24),
    onSurface = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFF1A1F29),
    surfaceVariant = Color(0xFF1F2633),
    onSurfaceVariant = Color(0xFFD0D0D0),

    // Outlines
    outline = Color(0xFF2F3542),
    outlineVariant = Color(0xFF2B303B),

    // Content
    contentPrimary = Color(0xFFFFFFFF),
    contentSecondary = Color(0xFFD0D0D0),
    contentDisabled = Color(0xFFA0A0A0),

    // Success
    success = Color(0xFF27AE60),
    onSuccess = Color(0xFFFFFFFF),
    successContainer = Color(0xFF1A1F29),
    onSuccessContainer = Color(0xFF27AE60),

    // Warning - Aura Growth Indicator
    warning = Color(0xFFFF9800),
    onWarning = Color(0xFFFFFFFF),
    warningContainer = Color(0xFF1F2633),
    onWarningContainer = Color(0xFFFF9800),

    // Error
    error = Color(0xFFC0392B),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFF2C2C2E),
    onErrorContainer = Color(0xFFC0392B),

    // Info - Aura Blue
    info = Color(0xFF2970FF),
    onInfo = Color(0xFFFFFFFF),
    infoContainer = Color(0xFF1A1F29),
    onInfoContainer = Color(0xFF2970FF),
)

@Preview(name = "Color tokens - Light")
@Composable
private fun AppColorSchemeLightPreview() {
    AppColorSchemePreviewContent(darkTheme = false)
}

@Preview(name = "Color tokens - Dark")
@Composable
private fun AppColorSchemeDarkPreview() {
    AppColorSchemePreviewContent(darkTheme = true)
}

@Composable
private fun AppColorSchemePreviewContent(darkTheme: Boolean) {
    AppTheme(darkTheme = darkTheme) {
        val colors = MaterialTheme.appColorScheme
        val spacing = MaterialTheme.appSpacing
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.background)
                .padding(spacing.large),
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            ColorTokenRow(label = "Primary", fill = colors.primary, onFill = colors.onPrimary)
            ColorTokenRow(
                label = "Primary container",
                fill = colors.primaryContainer,
                onFill = colors.onPrimaryContainer,
            )
            ColorTokenRow(
                label = "Complementary",
                fill = colors.complementary,
                onFill = colors.onComplementary,
            )
            ColorTokenRow(
                label = "Complementary container",
                fill = colors.complementaryContainer,
                onFill = colors.onComplementaryContainer,
            )
            ColorTokenRow(label = "Success", fill = colors.success, onFill = colors.onSuccess)
        }
    }
}

@Composable
private fun ColorTokenRow(
    label: String,
    fill: Color,
    onFill: Color,
) {
    val spacing = MaterialTheme.appSpacing
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(spacing.medium))
            .background(fill)
            .padding(horizontal = spacing.medium, vertical = spacing.roomy),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.appTypography.labelLarge,
            color = onFill,
        )
    }
}
