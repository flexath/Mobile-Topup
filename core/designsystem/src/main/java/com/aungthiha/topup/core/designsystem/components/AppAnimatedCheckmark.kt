package com.aungthiha.topup.core.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import com.aungthiha.topup.core.designsystem.theme.appColorScheme
import com.aungthiha.topup.core.designsystem.theme.appSpacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val CIRCLE_TINT_ALPHA = 0.14f
private const val CHECK_DRAW_MILLIS = 420
private const val CHECK_DELAY_MILLIS = 120

/**
 * Success visual: a tinted circle springs in, then the check stroke draws itself.
 * The animation plays once per destination and is not replayed after rotation.
 */
@Composable
fun AppAnimatedCheckmark(
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: Dp = MaterialTheme.appSpacing.illustration / 2,
) {
    val colors = MaterialTheme.appColorScheme
    var hasPlayed by rememberSaveable { mutableStateOf(false) }
    val circleScale = remember { Animatable(if (hasPlayed) 1f else 0f) }
    val checkProgress = remember { Animatable(if (hasPlayed) 1f else 0f) }

    LaunchedEffect(Unit) {
        if (hasPlayed) return@LaunchedEffect
        launch {
            circleScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
        delay(CHECK_DELAY_MILLIS.toLong())
        checkProgress.animateTo(1f, tween(CHECK_DRAW_MILLIS, easing = FastOutSlowInEasing))
        hasPlayed = true
    }

    val checkPath = remember { Path() }
    val segment = remember { Path() }
    val measure = remember { PathMeasure() }

    Canvas(
        modifier = modifier
            .size(size)
            .semantics { this.contentDescription = contentDescription },
    ) {
        val radius = this.size.minDimension / 2
        drawCircle(
            color = colors.success.copy(alpha = CIRCLE_TINT_ALPHA),
            radius = radius * circleScale.value,
        )
        drawCircle(
            color = colors.success,
            radius = radius * 0.68f * circleScale.value,
        )

        checkPath.reset()
        checkPath.moveTo(this.size.width * 0.36f, this.size.height * 0.51f)
        checkPath.lineTo(this.size.width * 0.46f, this.size.height * 0.61f)
        checkPath.lineTo(this.size.width * 0.65f, this.size.height * 0.41f)
        measure.setPath(checkPath, false)
        segment.reset()
        measure.getSegment(0f, measure.length * checkProgress.value, segment, true)

        drawPath(
            path = segment,
            color = colors.onSuccess,
            style = Stroke(
                width = radius * 0.11f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
    }
}
