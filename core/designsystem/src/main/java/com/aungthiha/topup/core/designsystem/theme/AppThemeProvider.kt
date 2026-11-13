package com.aungthiha.topup.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.aungthiha.topup.core.designsystem.utils.DeviceType
import com.aungthiha.topup.core.designsystem.utils.getDeviceType

@Composable
fun AppThemeProvider(
    colorScheme: AppColorScheme,
    appShapes: AppShapes,
    appTypography: AppTypography,
    appSpacing: AppSpacing,
    deviceType: DeviceType,
    isLightTheme: Boolean,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalAppColors provides colorScheme,
        LocalAppTypography provides appTypography,
        LocalAppShapes provides appShapes,
        LocalAppSpacing provides appSpacing,
        LocalDeviceType provides deviceType,
        LocalIsLightTheme provides isLightTheme,
    ) {
        content()
    }
}

val LocalAppColors = staticCompositionLocalOf { AppLightColorScheme }
val MaterialTheme.appColorScheme: AppColorScheme
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current

val LocalAppTypography = staticCompositionLocalOf { AppTypography() }
val MaterialTheme.appTypography: AppTypography
    @Composable
    @ReadOnlyComposable
    get() = LocalAppTypography.current

val LocalAppShapes = staticCompositionLocalOf { AppShapes() }
val MaterialTheme.appShapes: AppShapes
    @Composable
    @ReadOnlyComposable
    get() = LocalAppShapes.current

val LocalAppSpacing = staticCompositionLocalOf { AppSpacing() }
val MaterialTheme.appSpacing: AppSpacing
    @Composable
    @ReadOnlyComposable
    get() = LocalAppSpacing.current

// Window dimensions can change continuously during resizing, so only local consumers should update.
val LocalDeviceType = compositionLocalOf { getDeviceType(widthDp = 0, heightDp = 0) }
val MaterialTheme.deviceType: DeviceType
    @Composable
    @ReadOnlyComposable
    get() = LocalDeviceType.current

val LocalIsLightTheme = staticCompositionLocalOf { true }
val MaterialTheme.isLightTheme: Boolean
    @Composable
    @ReadOnlyComposable
    get() = LocalIsLightTheme.current
