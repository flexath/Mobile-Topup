package com.aungthiha.topup.core.designsystem.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.aungthiha.topup.core.designsystem.icons.AppIcons
import com.aungthiha.topup.core.designsystem.theme.appColorScheme
import com.aungthiha.topup.core.designsystem.theme.appShapes
import com.aungthiha.topup.core.designsystem.theme.appSpacing
import com.aungthiha.topup.core.designsystem.theme.appTypography

enum class AppBannerTone { Info, Warning, Error }

/** Subtle inline status message (offline, sync warnings, validation summaries). */
@Composable
fun AppBanner(
    message: String,
    modifier: Modifier = Modifier,
    tone: AppBannerTone = AppBannerTone.Warning,
    @DrawableRes icon: Int = tone.defaultIcon(),
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = MaterialTheme.appColorScheme
    val spacing = MaterialTheme.appSpacing
    val (container, content) = when (tone) {
        AppBannerTone.Info -> colors.infoContainer to colors.onInfoContainer
        AppBannerTone.Warning -> colors.warningContainer to colors.onWarningContainer
        AppBannerTone.Error -> colors.errorContainer to colors.onErrorContainer
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.appShapes.medium)
            .background(container)
            .padding(horizontal = spacing.medium, vertical = spacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.small),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = content,
            modifier = Modifier.size(spacing.generous),
        )
        Text(
            text = message,
            style = MaterialTheme.appTypography.bodySmall,
            color = colors.contentPrimary,
            modifier = Modifier.weight(1f),
        )
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.appTypography.labelLarge,
                    color = if (tone == AppBannerTone.Error) content else colors.primary,
                )
            }
        }
    }
}

private fun AppBannerTone.defaultIcon(): Int = when (this) {
    AppBannerTone.Info -> AppIcons.Info
    AppBannerTone.Warning -> AppIcons.CloudOff
    AppBannerTone.Error -> AppIcons.Error
}
