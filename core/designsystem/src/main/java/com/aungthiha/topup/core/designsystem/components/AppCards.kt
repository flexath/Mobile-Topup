package com.aungthiha.topup.core.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import com.aungthiha.topup.core.designsystem.icons.AppIcons
import com.aungthiha.topup.core.designsystem.theme.appColorScheme
import com.aungthiha.topup.core.designsystem.theme.appShapes
import com.aungthiha.topup.core.designsystem.theme.appSpacing

object AppSelectableCardDefaults {
    /** Opacity of [com.aungthiha.topup.core.designsystem.theme.AppColorScheme.primary] behind a selected card. */
    const val SELECTED_TINT_ALPHA = 0.08f
    const val ANIMATION_MILLIS = 180
}

/** Neutral surface card with a subtle outline — no elevation shadow. */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(MaterialTheme.appSpacing.large),
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = MaterialTheme.appColorScheme
    val shape = MaterialTheme.appShapes.large
    Column(
        modifier = modifier
            .clip(shape)
            .background(colors.surface)
            .border(MaterialTheme.appSpacing.hairline, colors.outlineVariant, shape)
            .padding(contentPadding),
        content = content,
    )
}

/**
 * Selectable card used for denominations and data packages.
 *
 * Selected: primary border + subtle primary tint + top-right checkmark. All three animate.
 */
@Composable
fun AppSelectableCard(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(MaterialTheme.appSpacing.medium),
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = MaterialTheme.appColorScheme
    val spacing = MaterialTheme.appSpacing
    val shape = MaterialTheme.appShapes.large
    val animation = tween<Color>(AppSelectableCardDefaults.ANIMATION_MILLIS)

    val borderColor by animateColorAsState(
        targetValue = if (selected) colors.primary else colors.outlineVariant,
        animationSpec = animation,
        label = "selectableCardBorder",
    )
    val containerColor by animateColorAsState(
        targetValue = if (selected) {
            colors.primary.copy(alpha = AppSelectableCardDefaults.SELECTED_TINT_ALPHA)
        } else {
            colors.surface
        },
        animationSpec = animation,
        label = "selectableCardContainer",
    )
    val borderWidth = if (selected) spacing.extraExtraSmall else spacing.hairline

    Box(
        modifier = modifier
            .clip(shape)
            .background(colors.surface)
            .background(containerColor)
            .border(borderWidth, borderColor, shape)
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick,
            ),
    ) {
        Box(modifier = Modifier.padding(contentPadding), content = content)

        AnimatedVisibility(
            visible = selected,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(spacing.small),
        ) {
            Box(
                modifier = Modifier
                    .size(spacing.generous)
                    .clip(CircleShape)
                    .background(colors.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(AppIcons.Check),
                    contentDescription = null,
                    tint = colors.onPrimary,
                    modifier = Modifier.size(spacing.medium),
                )
            }
        }
    }
}
