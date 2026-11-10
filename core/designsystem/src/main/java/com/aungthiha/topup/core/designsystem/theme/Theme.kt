package com.aungthiha.topup.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.aungthiha.topup.core.designsystem.utils.InitializeRatios
import com.aungthiha.topup.core.designsystem.utils.getDeviceType

val MaterialLightColorScheme = lightColorScheme(
    primary = AppLightColorScheme.primary,
    onPrimary = AppLightColorScheme.onPrimary,
    primaryContainer = AppLightColorScheme.primaryContainer,
    onPrimaryContainer = AppLightColorScheme.onPrimaryContainer,
    secondary = AppLightColorScheme.secondary,
    onSecondary = AppLightColorScheme.onSecondary,
    secondaryContainer = AppLightColorScheme.secondaryContainer,
    onSecondaryContainer = AppLightColorScheme.onSecondaryContainer,
    tertiary = AppLightColorScheme.complementary,
    onTertiary = AppLightColorScheme.onComplementary,
    tertiaryContainer = AppLightColorScheme.complementaryContainer,
    onTertiaryContainer = AppLightColorScheme.onComplementaryContainer,
    background = AppLightColorScheme.background,
    onBackground = AppLightColorScheme.onBackground,
    surface = AppLightColorScheme.surface,
    onSurface = AppLightColorScheme.onSurface,
    surfaceVariant = AppLightColorScheme.surfaceVariant,
    onSurfaceVariant = AppLightColorScheme.onSurfaceVariant,
    outline = AppLightColorScheme.outline,
    outlineVariant = AppLightColorScheme.outlineVariant,
    error = AppLightColorScheme.error,
    onError = AppLightColorScheme.onError,
    errorContainer = AppLightColorScheme.errorContainer,
    onErrorContainer = AppLightColorScheme.onErrorContainer,
)

val MaterialDarkColorScheme = darkColorScheme(
    primary = AppDarkColorScheme.primary,
    onPrimary = AppDarkColorScheme.onPrimary,
    primaryContainer = AppDarkColorScheme.primaryContainer,
    onPrimaryContainer = AppDarkColorScheme.onPrimaryContainer,
    secondary = AppDarkColorScheme.secondary,
    onSecondary = AppDarkColorScheme.onSecondary,
    secondaryContainer = AppDarkColorScheme.secondaryContainer,
    onSecondaryContainer = AppDarkColorScheme.onSecondaryContainer,
    tertiary = AppDarkColorScheme.complementary,
    onTertiary = AppDarkColorScheme.onComplementary,
    tertiaryContainer = AppDarkColorScheme.complementaryContainer,
    onTertiaryContainer = AppDarkColorScheme.onComplementaryContainer,
    background = AppDarkColorScheme.background,
    onBackground = AppDarkColorScheme.onBackground,
    surface = AppDarkColorScheme.surface,
    onSurface = AppDarkColorScheme.onSurface,
    surfaceVariant = AppDarkColorScheme.surfaceVariant,
    onSurfaceVariant = AppDarkColorScheme.onSurfaceVariant,
    outline = AppDarkColorScheme.outline,
    outlineVariant = AppDarkColorScheme.outlineVariant,
    error = AppDarkColorScheme.error,
    onError = AppDarkColorScheme.onError,
    errorContainer = AppDarkColorScheme.errorContainer,
    onErrorContainer = AppDarkColorScheme.onErrorContainer,
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    InitializeRatios()

    val materialColorScheme = if (darkTheme) MaterialDarkColorScheme else MaterialLightColorScheme
    val appColorScheme = if (darkTheme) AppDarkColorScheme else AppLightColorScheme
    val appShapes = AppShapes()
    val appTypography = getTypography()
    val deviceType = getDeviceType()
    val appSpacing = remember(deviceType.widthClass) {
        getAppSpacing(deviceType)
    }

    AppThemeProvider(
        colorScheme = appColorScheme,
        appShapes = appShapes,
        appTypography = appTypography,
        appSpacing = appSpacing,
        deviceType = deviceType,
        isLightTheme = !darkTheme,
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = appTypography.toMaterialTypography(),
            content = content,
        )
    }
}
