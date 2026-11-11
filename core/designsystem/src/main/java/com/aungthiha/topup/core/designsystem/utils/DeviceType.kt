package com.aungthiha.topup.core.designsystem.utils

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.HingeInfo
import androidx.compose.material3.adaptive.Posture
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowDpSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalInspectionMode
import kotlin.math.roundToInt

/**
 * Material 3 window-width classes extended for large desktop and tablet windows.
 *
 * Classification is based on the app's current window, not its physical device. This makes
 * Android multi-window and large-screen layouts adapt without special cases.
 */
enum class DeviceWidthClass(
    val minWidthDp: Int,
) {
    Compact(minWidthDp = 0),
    Medium(minWidthDp = 600),
    Expanded(minWidthDp = 840),
    Large(minWidthDp = 1_200),
    ExtraLarge(minWidthDp = 1_600),
}

sealed interface DevicePosture {
    data object Normal : DevicePosture

    data class Foldable(
        val isTabletop: Boolean,
        val hingeList: List<HingeInfo>,
    ) : DevicePosture
}

data class DeviceType(
    val widthDp: Int,
    val heightDp: Int,
    val widthClass: DeviceWidthClass,
    val posture: DevicePosture = DevicePosture.Normal,
) : Comparable<DeviceType> {
    init {
        require(widthDp >= 0) { "widthDp must be non-negative." }
        require(heightDp >= 0) { "heightDp must be non-negative." }
    }

    val isLandscape: Boolean
        get() = widthDp > heightDp

    val isFoldable: Boolean
        get() = posture is DevicePosture.Foldable

    override fun compareTo(other: DeviceType): Int =
        widthClass.compareTo(other.widthClass)
}

fun getDeviceType(
    widthDp: Int,
    heightDp: Int,
    posture: DevicePosture = DevicePosture.Normal,
): DeviceType {
    require(widthDp >= 0) { "widthDp must be non-negative." }
    require(heightDp >= 0) { "heightDp must be non-negative." }

    return DeviceType(
        widthDp = widthDp,
        heightDp = heightDp,
        widthClass = DeviceWidthClass.entries
            .last { widthDp >= it.minWidthDp },
        posture = posture,
    )
}

fun Posture.toDevicePosture(): DevicePosture {
    val hinges = hingeList
    return if (hinges.isEmpty()) {
        DevicePosture.Normal
    } else {
        DevicePosture.Foldable(
            isTabletop = isTabletop,
            hingeList = hinges,
        )
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun getDeviceType(
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo(
        supportLargeAndXLargeWidth = true,
    ),
): DeviceType {
    // Adaptive window APIs are not available in Compose Preview; fall back to Configuration.
    if (LocalInspectionMode.current) {
        val configuration = LocalConfiguration.current
        return remember(configuration.screenWidthDp, configuration.screenHeightDp) {
            getDeviceType(
                widthDp = configuration.screenWidthDp.coerceAtLeast(1),
                heightDp = configuration.screenHeightDp.coerceAtLeast(1),
            )
        }
    }

    val windowSize = currentWindowDpSize()
    val posture = windowAdaptiveInfo.windowPosture.toDevicePosture()
    return remember(windowSize, posture) {
        getDeviceType(
            widthDp = windowSize.width.value.roundToInt(),
            heightDp = windowSize.height.value.roundToInt(),
            posture = posture,
        )
    }
}

infix fun DeviceType.greaterThan(other: DeviceType): Boolean =
    widthClass > other.widthClass

infix fun DeviceType.greaterThanOrEqual(other: DeviceType): Boolean =
    widthClass >= other.widthClass

infix fun DeviceType.smallerThan(other: DeviceType): Boolean =
    widthClass < other.widthClass

infix fun DeviceType.smallerThanOrEqual(other: DeviceType): Boolean =
    widthClass <= other.widthClass

val DeviceType.isLandscapePhone: Boolean
    get() = isLandscape &&
        heightDp < 500 &&
        widthDp.toFloat() / heightDp.coerceAtLeast(1) >= 1.8f
