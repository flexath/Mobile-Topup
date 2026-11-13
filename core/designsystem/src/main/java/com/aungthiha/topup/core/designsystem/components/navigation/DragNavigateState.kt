package com.aungthiha.topup.core.designsystem.components.navigation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect

/**
 * Visual-only state for drag-to-navigate on [AppFloatingNavBar].
 *
 * Tab order is fixed. This never reorders destinations. Pointer translation is not stored in
 * snapshot state so drag movement does not recompose the bar on every frame.
 */
@Stable
internal class DragNavigateState {
    var draggedKey: String? by mutableStateOf(null)
        private set

    var hoverKey: String? by mutableStateOf(null)
        private set

    val isDragging: Boolean get() = draggedKey != null

    fun start(key: String) {
        draggedKey = key
        hoverKey = key
    }

    fun move(
        pointer: Offset,
        bounds: Map<String, Rect>,
        verticalSlop: Float,
    ) {
        if (draggedKey == null) return
        val nextHover = destinationAt(pointer, bounds, verticalSlop)
        if (nextHover != hoverKey) {
            hoverKey = nextHover
        }
    }

    /**
     * Ends the gesture and returns the destination key to navigate to, or `null` when the drop
     * should cancel (outside a destination, or onto the dragged tab itself).
     */
    fun complete(): String? {
        val target = hoverKey?.takeIf { it != draggedKey }
        reset()
        return target
    }

    fun cancel() {
        reset()
    }

    private fun reset() {
        draggedKey = null
        hoverKey = null
    }
}

internal fun destinationAt(
    position: Offset,
    bounds: Map<String, Rect>,
    verticalSlop: Float,
): String? {
    var bestKey: String? = null
    var bestDistance = Float.POSITIVE_INFINITY
    bounds.forEach { (key, rect) ->
        if (
            position.x >= rect.left &&
            position.x < rect.right &&
            position.y >= rect.top - verticalSlop &&
            position.y <= rect.bottom + verticalSlop
        ) {
            val dx = position.x - rect.center.x
            val dy = position.y - rect.center.y
            val distance = dx * dx + dy * dy
            if (distance < bestDistance) {
                bestDistance = distance
                bestKey = key
            }
        }
    }
    return bestKey
}
