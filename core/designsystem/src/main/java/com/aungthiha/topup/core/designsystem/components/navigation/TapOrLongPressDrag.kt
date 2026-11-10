package com.aungthiha.topup.core.designsystem.components.navigation

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.util.fastFirstOrNull
import kotlin.math.max

/**
 * Distinguishes a tap from a long-press-then-drag without starting a drag on ordinary swipes.
 *
 * - Pointer up before the long-press timeout and within touch slop → [onTap]
 * - Held still until the long-press timeout → [onDragStart], then pointer movement → [onDrag]
 * - Movement beyond touch slop before the timeout → cancelled (no tap, no drag)
 */
internal suspend fun PointerInputScope.detectTapOrLongPressDrag(
    onTap: (Offset) -> Unit,
    onDragStart: (Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val slopSquared = viewConfiguration.touchSlop * viewConfiguration.touchSlop
        var farthestSquared = 0f
        var current = down
        var timedOut = false

        try {
            withTimeout(viewConfiguration.longPressTimeoutMillis) {
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Main)
                    val change = event.changes.fastFirstOrNull { it.id == down.id } ?: return@withTimeout
                    current = change
                    val dx = change.position.x - down.position.x
                    val dy = change.position.y - down.position.y
                    farthestSquared = max(farthestSquared, dx * dx + dy * dy)
                    if (farthestSquared > slopSquared || change.changedToUpIgnoreConsumed() || !change.pressed) {
                        return@withTimeout
                    }
                }
            }
        } catch (_: PointerEventTimeoutCancellationException) {
            timedOut = true
        }

        when {
            timedOut && current.pressed && farthestSquared <= slopSquared -> {
                current.consume()
                onDragStart(current.position)
                val released = drag(down.id) { change ->
                    change.consume()
                    onDrag(change.position)
                }
                if (released) onDragEnd() else onDragCancel()
            }
            !timedOut && current.changedToUpIgnoreConsumed() && farthestSquared <= slopSquared -> {
                current.consume()
                onTap(down.position)
            }
            else -> Unit
        }
    }
}
