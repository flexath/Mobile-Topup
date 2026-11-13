package com.aungthiha.topup.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.aungthiha.topup.core.designsystem.icons.AppIcons
import com.aungthiha.topup.core.designsystem.theme.appColorScheme
import com.aungthiha.topup.core.designsystem.theme.appShapes
import com.aungthiha.topup.core.designsystem.theme.appSpacing
import com.aungthiha.topup.core.designsystem.theme.appTypography

/** Label on the left, value on the right; values wrap instead of truncating (e.g. transaction ids). */
@Composable
fun AppDetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.appColorScheme
    val spacing = MaterialTheme.appSpacing
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = spacing.small),
        horizontalArrangement = Arrangement.spacedBy(spacing.large),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            style = MaterialTheme.appTypography.bodyMedium,
            color = colors.contentSecondary,
        )
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(spacing.small, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            trailing?.invoke()
            Text(
                text = value,
                style = MaterialTheme.appTypography.bodyMedium,
                color = colors.contentPrimary,
                textAlign = TextAlign.End,
            )
        }
    }
}

@Composable
fun AppDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        thickness = MaterialTheme.appSpacing.hairline,
        color = MaterialTheme.appColorScheme.outlineVariant,
    )
}

/** Small success pill ("Success"). Only successful transactions exist in this app. */
@Composable
fun AppSuccessChip(text: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.appColorScheme
    val spacing = MaterialTheme.appSpacing
    Row(
        modifier = modifier
            .clip(MaterialTheme.appShapes.pill)
            .background(colors.success.copy(alpha = AppSelectableCardDefaults.SELECTED_TINT_ALPHA * 2))
            .padding(horizontal = spacing.small, vertical = spacing.extraExtraSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall),
    ) {
        Icon(
            painter = painterResource(AppIcons.CheckCircle),
            contentDescription = null,
            tint = colors.success,
            modifier = Modifier.size(spacing.roomy),
        )
        Text(
            text = text,
            style = MaterialTheme.appTypography.labelMedium,
            color = colors.onSuccessContainer,
        )
    }
}
