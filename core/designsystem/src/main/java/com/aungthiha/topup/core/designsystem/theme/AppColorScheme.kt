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

/** Light palette — KBZ Blue / Red / Gold. */
val AppLightColorScheme = AppColorScheme(
    // Primary - KBZ Blue
    primary = Color(0xFF174F91),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDCEAFF),
    onPrimaryContainer = Color(0xFF10365F),

    // Secondary - KBZ Red
    secondary = Color(0xFFD62828),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFE0DF),
    onSecondaryContainer = Color(0xFF751414),

    // Complementary - Gold
    complementary = Color(0xFFE6A52E),
    onComplementary = Color(0xFF241800),
    complementaryContainer = Color(0xFFFFE8B8),
    onComplementaryContainer = Color(0xFF654300),

    // Background
    background = Color(0xFFF4F6FA),
    onBackground = Color(0xFF172033),

    // Surfaces
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF172033),
    surfaceElevated = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE9EEF5),
    onSurfaceVariant = Color(0xFF46556B),

    // Outlines
    outline = Color(0xFF78869A),
    outlineVariant = Color(0xFFD4DCE7),

    // Content
    contentPrimary = Color(0xFF172033),
    contentSecondary = Color(0xFF66758A),
    contentDisabled = Color(0xFFADB7C5),

    // Success
    success = Color(0xFF16834A),
    onSuccess = Color(0xFFFFFFFF),
    successContainer = Color(0xFFD8F5E4),
    onSuccessContainer = Color(0xFF105A34),

    // Warning
    warning = Color(0xFF986000),
    onWarning = Color(0xFFFFFFFF),
    warningContainer = Color(0xFFFFEDC2),
    onWarningContainer = Color(0xFF674000),

    // Error
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF7F1111),

    // Info
    info = Color(0xFF1769AA),
    onInfo = Color(0xFFFFFFFF),
    infoContainer = Color(0xFFDCEEFF),
    onInfoContainer = Color(0xFF10456F),
)

/** Dark palette — KBZ Blue / Red / Gold. */
val AppDarkColorScheme = AppColorScheme(
    // Primary - KBZ Blue
    primary = Color(0xFF8ABEFF),
    onPrimary = Color(0xFF082E55),
    primaryContainer = Color(0xFF174F91),
    onPrimaryContainer = Color(0xFFDCEAFF),

    // Secondary - KBZ Red
    secondary = Color(0xFFFF8A80),
    onSecondary = Color(0xFF5C1010),
    secondaryContainer = Color(0xFF8F2525),
    onSecondaryContainer = Color(0xFFFFE0DF),

    // Complementary - Gold
    complementary = Color(0xFFFFD180),
    onComplementary = Color(0xFF382500),
    complementaryContainer = Color(0xFF654A18),
    onComplementaryContainer = Color(0xFFFFE8B8),

    // Background
    background = Color(0xFF0B1220),
    onBackground = Color(0xFFE8EDF5),

    // Surfaces
    surface = Color(0xFF111B2E),
    onSurface = Color(0xFFE8EDF5),
    surfaceElevated = Color(0xFF19263A),
    surfaceVariant = Color(0xFF202D43),
    onSurfaceVariant = Color(0xFFBBC7D8),

    // Outlines
    outline = Color(0xFF8797AD),
    outlineVariant = Color(0xFF344258),

    // Content
    contentPrimary = Color(0xFFF1F5FB),
    contentSecondary = Color(0xFFABB8CA),
    contentDisabled = Color(0xFF66758A),

    // Success
    success = Color(0xFF69D99A),
    onSuccess = Color(0xFF063D22),
    successContainer = Color(0xFF124F32),
    onSuccessContainer = Color(0xFFB5F1CC),

    // Warning
    warning = Color(0xFFFFCA72),
    onWarning = Color(0xFF432900),
    warningContainer = Color(0xFF604313),
    onWarningContainer = Color(0xFFFFE6B3),

    // Error
    error = Color(0xFFFF8A80),
    onError = Color(0xFF5C1010),
    errorContainer = Color(0xFF852B2B),
    onErrorContainer = Color(0xFFFFDAD6),

    // Info
    info = Color(0xFF82C7FF),
    onInfo = Color(0xFF07385B),
    infoContainer = Color(0xFF16476B),
    onInfoContainer = Color(0xFFD6EDFF),
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
