package com.aungthiha.topup.core.designsystem.utils

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer

/**
 * Records a composable subtree so it can be exported as a bitmap (e.g. a shareable receipt).
 * Only content inside [capturable] is captured — never app bars or navigation chrome.
 */
@Stable
class CaptureController internal constructor(internal val layer: GraphicsLayer) {
    suspend fun capture(): Bitmap = layer.toImageBitmap().asAndroidBitmap()
}

@Composable
fun rememberCaptureController(): CaptureController {
    val layer = rememberGraphicsLayer()
    return remember(layer) { CaptureController(layer) }
}

fun Modifier.capturable(controller: CaptureController): Modifier = drawWithContent {
    controller.layer.record { this@drawWithContent.drawContent() }
    drawLayer(controller.layer)
}
