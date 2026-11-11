package com.aungthiha.topup.core.designsystem.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import com.aungthiha.topup.core.designsystem.icons.AppIcons
import com.aungthiha.topup.core.designsystem.theme.appColorScheme
import com.aungthiha.topup.core.designsystem.theme.appSpacing
import com.aungthiha.topup.core.designsystem.theme.appTypography

/**
 * Flat screen app bar. Status-bar insets are handled by the app shell, so this bar applies none.
 */
@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onNavigateUp: (() -> Unit)? = null,
    navigateUpContentDescription: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val spacing = MaterialTheme.appSpacing
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = spacing.toolbar)
            .padding(horizontal = spacing.extraSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onNavigateUp != null) {
            AppIconButton(
                icon = AppIcons.ArrowBack,
                contentDescription = navigateUpContentDescription,
                onClick = onNavigateUp,
            )
        } else {
            Spacer(Modifier.width(spacing.medium))
        }
        Text(
            text = title,
            style = MaterialTheme.appTypography.titleLarge,
            color = MaterialTheme.appColorScheme.contentPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = spacing.extraSmall),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall),
            verticalAlignment = Alignment.CenterVertically,
            content = actions,
        )
    }
}

@Composable
fun AppIconButton(
    @DrawableRes icon: Int,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = MaterialTheme.appColorScheme
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(MaterialTheme.appSpacing.button),
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = colors.contentPrimary,
            disabledContentColor = colors.contentDisabled,
        ),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            modifier = Modifier.size(MaterialTheme.appSpacing.extraLarge),
        )
    }
}
