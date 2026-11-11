package com.aungthiha.topup.core.designsystem.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp

private var _dpRatio = 1f
private var _spRatio = 1f

val dpRatio get() = _dpRatio
val spRatio get() = _spRatio

@Composable
fun InitializeRatios(designWidth: Int = 390) {
    val configuration = LocalConfiguration.current
    // Preview hosts often report 0 for Resources.getSystem(); prefer Configuration there.
    val screenWidthDp = if (LocalInspectionMode.current) {
        configuration.screenWidthDp.coerceAtLeast(1)
    } else {
        PlatformConfiguration.screenWidthDp.coerceAtLeast(1)
    }
    val fontScale = if (LocalInspectionMode.current) {
        configuration.fontScale
    } else {
        PlatformConfiguration.fontScale
    }
    val baseRatio = designWidth.toFloat() / screenWidthDp

    _dpRatio = when {
        screenWidthDp < 600 -> baseRatio
        else -> baseRatio * 0.1f
    }.coerceIn(0.5f, 1.1f)

    _spRatio = (fontScale * _dpRatio * 0.9f)
        .coerceIn(0.8f, 1.2f)
}

@Stable
inline val Int.calculatedDp: Dp
    get() = (this * dpRatio).dp

@Stable
inline val Double.calculatedDp: Dp
    get() = (this * dpRatio).dp

@OptIn(ExperimentalUnitApi::class)
@Stable
inline val Int.calculatedSp: TextUnit
    get() = TextUnit(value = this.toFloat().div(spRatio), TextUnitType.Sp)

@OptIn(ExperimentalUnitApi::class)
@Stable
inline val Double.calculatedSp: TextUnit
    get() = TextUnit(value = this.toFloat().div(spRatio), TextUnitType.Sp)
