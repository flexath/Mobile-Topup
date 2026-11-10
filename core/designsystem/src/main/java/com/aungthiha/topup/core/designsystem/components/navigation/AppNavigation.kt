package com.aungthiha.topup.core.designsystem.components.navigation

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.aungthiha.topup.core.designsystem.R
import com.aungthiha.topup.core.designsystem.theme.AppTheme
import com.aungthiha.topup.core.designsystem.theme.appColorScheme
import com.aungthiha.topup.core.designsystem.theme.appShapes
import com.aungthiha.topup.core.designsystem.theme.appSpacing
import com.aungthiha.topup.core.designsystem.theme.appTypography

@Immutable
data class AppNavItem(
    val key: String,
    val label: String,
    @DrawableRes val icon: Int,
)

/**
 * Floating glass navigation for compact windows.
 *
 * Destinations stay in a fixed order. Long-press and drag onto another tab to navigate there;
 * it does not reorder the bar. Deliberately not Material `NavigationBar`.
 */
@Composable
fun AppFloatingNavBar(
    items: List<AppNavItem>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.appSpacing
    val glass = rememberAppGlassColors()
    val haptics = LocalHapticFeedback.current
    val density = LocalDensity.current
    val dragState = remember { DragNavigateState() }
    val itemBounds = remember { mutableMapOf<String, Rect>() }
    val barLayoutRef = remember { arrayOfNulls<LayoutCoordinates>(1) }
    val itemKeys = remember(items) { items.map { it.key } }
    val currentOnSelect by rememberUpdatedState(onSelect)
    val currentHaptics by rememberUpdatedState(haptics)
    val verticalSlopPx = with(density) { spacing.small.toPx() }
    val lastHoverKey = remember { arrayOfNulls<String>(1) }

    val barShape = MaterialTheme.appShapes.pill

    Box(
        modifier = modifier
            .shadow(
                elevation = spacing.medium,
                shape = barShape,
                clip = false,
            )
            .height(spacing.display)
            .onGloballyPositioned { barLayoutRef[0] = it }
            .pointerInput(itemKeys, verticalSlopPx) {
                detectTapOrLongPressDrag(
                    onTap = { position ->
                        destinationAt(position, itemBounds, verticalSlopPx)
                            ?.let(currentOnSelect)
                    },
                    onDragStart = { position ->
                        val key = destinationAt(position, itemBounds, verticalSlopPx)
                            ?: return@detectTapOrLongPressDrag
                        dragState.start(key)
                        lastHoverKey[0] = key
                        currentHaptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onDrag = { position ->
                        dragState.move(position, itemBounds, verticalSlopPx)
                        val hover = dragState.hoverKey
                        if (hover != lastHoverKey[0]) {
                            lastHoverKey[0] = hover
                            if (hover != null && hover != dragState.draggedKey) {
                                currentHaptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        }
                    },
                    onDragEnd = {
                        lastHoverKey[0] = null
                        dragState.complete()?.let(currentOnSelect)
                    },
                    onDragCancel = {
                        lastHoverKey[0] = null
                        dragState.cancel()
                    },
                )
            },
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .appLiquidGlassFill(
                    shape = barShape,
                    colors = glass,
                    borderWidth = spacing.hairline,
                )
                .clearAndSetSemantics { },
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = spacing.tight),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            items.forEach { item ->
                val isSource = item.key == dragState.draggedKey
                val isDropTarget = item.key == dragState.hoverKey && !isSource
                NavBarItem(
                    item = item,
                    selected = item.key == selectedKey,
                    isDropTarget = isDropTarget,
                    glass = glass,
                    onSelect = { currentOnSelect(item.key) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .onGloballyPositioned { coordinates ->
                            val bar = barLayoutRef[0]
                            if (bar == null || !bar.isAttached || !coordinates.isAttached) {
                                return@onGloballyPositioned
                            }
                            val topLeft = bar.localPositionOf(coordinates, Offset.Zero)
                            itemBounds[item.key] = Rect(
                                left = topLeft.x,
                                top = topLeft.y,
                                right = topLeft.x + coordinates.size.width,
                                bottom = topLeft.y + coordinates.size.height,
                            )
                        },
                )
            }
        }
    }
}

@Composable
private fun NavBarItem(
    item: AppNavItem,
    selected: Boolean,
    isDropTarget: Boolean,
    glass: AppGlassColors,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.appSpacing
    val showSelected = selected && !isDropTarget
    val itemPillShape = RoundedCornerShape(percent = 50)

    val contentColor by animateColorAsState(
        targetValue = when {
            isDropTarget -> glass.dropTargetContent
            showSelected -> glass.selectedContent
            else -> glass.unselectedContent
        },
        animationSpec = navSpring(),
        label = "FloatingNavContent",
    )
    val containerColor by animateColorAsState(
        targetValue = when {
            isDropTarget -> glass.dropTargetPill
            showSelected -> glass.selectedPill
            else -> glass.idlePill
        },
        animationSpec = navSpring(),
        label = "FloatingNavPill",
    )

    Box(
        modifier = modifier.semantics(mergeDescendants = true) {
            role = Role.Tab
            this.selected = selected
            contentDescription = item.label
            onClick {
                onSelect()
                true
            }
        },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(vertical = spacing.tight),
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(itemPillShape)
                    .background(containerColor),
            )
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    painter = painterResource(item.icon),
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(spacing.generous),
                )
                Text(
                    text = item.label,
                    style = MaterialTheme.appTypography.labelMedium.copy(
                        fontWeight = if (showSelected) FontWeight.SemiBold else FontWeight.Medium,
                    ),
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = spacing.extraExtraSmall),
                )
            }
        }
    }
}

/**
 * Vertical navigation for medium and larger windows, where a bottom bar would waste width and
 * force an unnecessarily long reach.
 */
@Composable
fun AppNavRail(
    items: List<AppNavItem>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.appColorScheme
    val spacing = MaterialTheme.appSpacing

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(spacing.rail)
            .background(colors.surfaceElevated)
            .padding(vertical = spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.small),
    ) {
        if (header != null) {
            header()
        }

        items.forEach { item ->
            NavRailItem(
                item = item,
                selected = item.key == selectedKey,
                onSelect = { onSelect(item.key) },
            )
        }
    }
}

@Composable
private fun NavRailItem(
    item: AppNavItem,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    val colors = MaterialTheme.appColorScheme
    val spacing = MaterialTheme.appSpacing
    val interactionSource = remember { MutableInteractionSource() }

    val contentColor by animateColorAsState(
        targetValue = if (selected) colors.onPrimaryContainer else colors.contentSecondary,
        animationSpec = spring(),
        label = "NavRailContent",
    )
    val containerColor by animateColorAsState(
        targetValue = if (selected) {
            colors.primaryContainer
        } else {
            colors.primaryContainer.copy(alpha = 0f)
        },
        animationSpec = spring(),
        label = "NavRailContainer",
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(spacing.spacious))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Tab,
                onClick = onSelect,
            )
            .background(containerColor)
            .padding(horizontal = spacing.medium, vertical = spacing.relaxed),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.extraSmall),
    ) {
        Icon(
            painter = painterResource(item.icon),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(spacing.wide),
        )
        Text(
            text = item.label,
            style = MaterialTheme.appTypography.labelMedium.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            ),
            color = contentColor,
        )
    }
}

private fun <T> navSpring() = spring<T>(
    dampingRatio = 0.84f,
    stiffness = 420f,
)

@Preview(name = "Floating nav - Light")
@Composable
private fun AppFloatingNavBarLightPreview() {
    AppFloatingNavBarPreviewContent(darkTheme = false)
}

@Preview(name = "Floating nav - Dark")
@Composable
private fun AppFloatingNavBarDarkPreview() {
    AppFloatingNavBarPreviewContent(darkTheme = true)
}

@Preview(name = "Nav rail - Light")
@Composable
private fun AppNavRailLightPreview() {
    AppNavRailPreviewContent(darkTheme = false)
}

@Preview(name = "Nav rail - Dark")
@Composable
private fun AppNavRailDarkPreview() {
    AppNavRailPreviewContent(darkTheme = true)
}

@Composable
private fun AppNavRailPreviewContent(darkTheme: Boolean) {
    AppTheme(darkTheme = darkTheme) {
        val spacing = MaterialTheme.appSpacing
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.appColorScheme.background)
                .padding(spacing.large),
        ) {
            AppNavRail(
                items = previewNavItems(),
                selectedKey = "History",
                onSelect = {},
            )
        }
    }
}

@Composable
private fun AppFloatingNavBarPreviewContent(darkTheme: Boolean) {
    AppTheme(darkTheme = darkTheme) {
        val spacing = MaterialTheme.appSpacing
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.appColorScheme.background)
                .padding(spacing.large),
            contentAlignment = Alignment.BottomCenter,
        ) {
            AppFloatingNavBar(
                items = previewNavItems(),
                selectedKey = "Topup",
                onSelect = {},
            )
        }
    }
}

private fun previewNavItems(): List<AppNavItem> = listOf(
    AppNavItem("Topup", "Topup", R.drawable.ic_topup),
    AppNavItem("History", "History", R.drawable.ic_transaction_history),
)
