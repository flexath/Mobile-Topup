package com.aungthiha.topup.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import com.aungthiha.topup.core.designsystem.utils.DeviceType
import com.aungthiha.topup.core.designsystem.utils.DeviceWidthClass
import com.aungthiha.topup.core.designsystem.utils.calculatedDp

/**
 * Shared spacing tokens for TopUp's adaptive layout system.
 *
 * Tokens fall into three groups:
 * 1. **Fixed primitive scale** — padding and gaps inside reusable components; never scale with
 *    window width.
 * 2. **Fixed component tokens** — control sizes, radii, borders, and other component rhythm values;
 *    never scale with window width.
 * 3. **Adaptive semantic layout** — screen insets, section rhythm, pane gaps, and content width
 *    caps; resolved by [getAppSpacing] from the current [DeviceWidthClass].
 *
 * Feature UI must read adaptive values from [androidx.compose.material3.MaterialTheme.appSpacing],
 * not hard-coded `calculatedDp` or [Default] (which always returns the compact baseline).
 */
@Immutable
data class AppSpacing(
    val none: Dp = 0.calculatedDp,
    val extraExtraSmall: Dp = 2.calculatedDp,
    val extraSmall: Dp = 4.calculatedDp,
    val small: Dp = 8.calculatedDp,
    val medium: Dp = 12.calculatedDp,
    val large: Dp = 16.calculatedDp,
    val extraLarge: Dp = 24.calculatedDp,
    val extraExtraLarge: Dp = 32.calculatedDp,
    val huge: Dp = 48.calculatedDp,
    val extraHuge: Dp = 64.calculatedDp,

    val hairline: Dp = 1.calculatedDp,
    val compact: Dp = 3.calculatedDp,
    val tight: Dp = 6.calculatedDp,
    val snug: Dp = 7.calculatedDp,
    val relaxed: Dp = 10.calculatedDp,
    val segmentedInner: Dp = 11.calculatedDp,
    val roomy: Dp = 14.calculatedDp,
    val spacious: Dp = 18.calculatedDp,
    val generous: Dp = 20.calculatedDp,
    val wide: Dp = 22.calculatedDp,
    val broader: Dp = 26.calculatedDp,
    val hero: Dp = 28.calculatedDp,
    val tall: Dp = 30.calculatedDp,
    val extended: Dp = 34.calculatedDp,
    val grand: Dp = 36.calculatedDp,
    val ample: Dp = 40.calculatedDp,
    val control: Dp = 44.calculatedDp,
    val button: Dp = 52.calculatedDp,
    val toolbar: Dp = 56.calculatedDp,
    val thumbnailShort: Dp = 60.calculatedDp,
    val display: Dp = 72.calculatedDp,
    val rail: Dp = 88.calculatedDp,
    val illustration: Dp = 200.calculatedDp,
    val navigationMaxWidth: Dp = 480.calculatedDp,
    val pill: Dp = 999.calculatedDp,

    val screenHorizontal: Dp = 16.calculatedDp,
    val screenVertical: Dp = 16.calculatedDp,
    val content: Dp = 16.calculatedDp,
    val section: Dp = 24.calculatedDp,
    val pane: Dp = 16.calculatedDp,
    val readableMaxWidth: Dp = 640.calculatedDp,
    val messageMaxWidth: Dp = 360.calculatedDp,
    val emptyStateLottieSize: Dp = 150.calculatedDp,
    val preview: Dp = 180.calculatedDp,
    /**
     * Height reserved for the floating pill nav on compact windows (bar + shell vertical padding +
     * breathing room). Zero on rail layouts. Prefer [bottomNavigationClearance] in screens.
     */
    val floatingNavClearance: Dp = 132.calculatedDp,
) {
    /** Scroll/list bottom inset: floating nav overlay on compact, screen vertical inset on rail layouts. */
    val bottomNavigationClearance: Dp
        get() = floatingNavClearance + screenVertical

    companion object {
        /** Compact-window baseline. Use in non-composable defaults only — not for screen layout. */
        val Default = AppSpacing()
    }
}

/**
 * Resolves adaptive semantic layout spacing from the available app window.
 *
 * Foldable posture deliberately does not change the spacing scale. Hinges affect pane placement,
 * while the available window width remains the source of truth for layout breathing room.
 *
 * Fixed primitive and component tokens always keep their [AppSpacing.Default] values.
 */
fun getAppSpacing(deviceType: DeviceType): AppSpacing =
    when (deviceType.widthClass) {
        DeviceWidthClass.Compact -> AppSpacing()
        DeviceWidthClass.Medium -> AppSpacing(
            screenHorizontal = 24.calculatedDp,
            screenVertical = 24.calculatedDp,
            content = 20.calculatedDp,
            section = 32.calculatedDp,
            pane = 24.calculatedDp,
            readableMaxWidth = 720.calculatedDp,
            messageMaxWidth = 400.calculatedDp,
            preview = 220.calculatedDp,
            floatingNavClearance = 0.calculatedDp,
        )
        DeviceWidthClass.Expanded -> AppSpacing(
            screenHorizontal = 32.calculatedDp,
            screenVertical = 32.calculatedDp,
            content = 24.calculatedDp,
            section = 40.calculatedDp,
            pane = 32.calculatedDp,
            readableMaxWidth = 800.calculatedDp,
            messageMaxWidth = 440.calculatedDp,
            preview = 260.calculatedDp,
            floatingNavClearance = 0.calculatedDp,
        )
        DeviceWidthClass.Large -> AppSpacing(
            screenHorizontal = 48.calculatedDp,
            screenVertical = 40.calculatedDp,
            content = 24.calculatedDp,
            section = 48.calculatedDp,
            pane = 40.calculatedDp,
            readableMaxWidth = 960.calculatedDp,
            messageMaxWidth = 480.calculatedDp,
            preview = 300.calculatedDp,
            floatingNavClearance = 0.calculatedDp,
        )
        DeviceWidthClass.ExtraLarge -> AppSpacing(
            screenHorizontal = 64.calculatedDp,
            screenVertical = 48.calculatedDp,
            content = 32.calculatedDp,
            section = 64.calculatedDp,
            pane = 48.calculatedDp,
            readableMaxWidth = 1_120.calculatedDp,
            messageMaxWidth = 520.calculatedDp,
            preview = 340.calculatedDp,
            floatingNavClearance = 0.calculatedDp,
        )
    }
