package com.aungthiha.topup.feature.topup.presentation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.aungthiha.topup.core.designsystem.components.AppCard
import com.aungthiha.topup.core.designsystem.components.AppPrimaryButton
import com.aungthiha.topup.core.designsystem.icons.AppIcons
import com.aungthiha.topup.core.designsystem.theme.AppTheme
import com.aungthiha.topup.core.designsystem.theme.appColorScheme
import com.aungthiha.topup.core.designsystem.theme.appShapes
import com.aungthiha.topup.core.designsystem.theme.appSpacing
import com.aungthiha.topup.core.designsystem.theme.appTypography
import com.aungthiha.topup.feature.topup.R

/** Primary entry point from the wallet overview into the recharge flow. */
@Composable
internal fun RechargeEntryCard(
    onStartTopup: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.appColorScheme
    val spacing = MaterialTheme.appSpacing

    AppCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(spacing.extraLarge),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.large)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.large),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .size(spacing.button)
                        .clip(CircleShape)
                        .background(colors.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(AppIcons.Topup),
                        contentDescription = null,
                        tint = colors.onPrimaryContainer,
                        modifier = Modifier.size(spacing.hero),
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(spacing.extraSmall),
                ) {
                    Text(
                        text = stringResource(R.string.topup_entry_title),
                        style = MaterialTheme.appTypography.titleMedium,
                        color = colors.contentPrimary,
                    )
                    Text(
                        text = stringResource(R.string.topup_entry_message),
                        style = MaterialTheme.appTypography.bodyMedium,
                        color = colors.contentSecondary,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.small)) {
                EntryTag(R.string.topup_entry_tag_airtime)
                EntryTag(R.string.topup_entry_tag_data)
                EntryTag(R.string.topup_entry_tag_auto_detect)
            }
            AppPrimaryButton(
                text = stringResource(R.string.topup_entry_cta),
                onClick = onStartTopup,
                leadingIcon = AppIcons.Smartphone,
            )
        }
    }
}

@Composable
private fun EntryTag(@StringRes label: Int) {
    val colors = MaterialTheme.appColorScheme
    val spacing = MaterialTheme.appSpacing
    Text(
        text = stringResource(label),
        style = MaterialTheme.appTypography.labelMedium,
        color = colors.onSurfaceVariant,
        maxLines = 1,
        modifier = Modifier
            .clip(MaterialTheme.appShapes.pill)
            .background(colors.surfaceVariant)
            .padding(horizontal = spacing.medium, vertical = spacing.tight),
    )
}

@Preview(showBackground = true)
@Composable
private fun RechargeEntryCardPreview() {
    AppTheme {
        RechargeEntryCard(onStartTopup = {}, modifier = Modifier.padding(MaterialTheme.appSpacing.large))
    }
}
