package com.aungthiha.topup.feature.topup.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.aungthiha.topup.core.common.format.formatGrouped
import com.aungthiha.topup.core.designsystem.icons.AppIcons
import com.aungthiha.topup.core.designsystem.theme.appColorScheme
import com.aungthiha.topup.core.designsystem.theme.appShapes
import com.aungthiha.topup.core.designsystem.theme.appSpacing
import com.aungthiha.topup.core.designsystem.theme.appTypography
import com.aungthiha.topup.feature.topup.R
import com.aungthiha.topup.feature.topup.presentation.states.WalletUiState

private const val ON_PRIMARY_WELL_ALPHA = 0.16f
private const val ON_PRIMARY_SUPPORTING_ALPHA = 0.85f

@Composable
internal fun WalletBalanceCard(
    wallet: WalletUiState,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.appColorScheme
    val spacing = MaterialTheme.appSpacing
    val supporting = colors.onPrimary.copy(alpha = ON_PRIMARY_SUPPORTING_ALPHA)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.appShapes.large)
            .background(colors.primary)
            .padding(spacing.extraLarge),
        verticalArrangement = Arrangement.spacedBy(spacing.small),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.medium),
        ) {
            Box(
                modifier = Modifier
                    .size(spacing.ample)
                    .clip(CircleShape)
                    .background(colors.onPrimary.copy(alpha = ON_PRIMARY_WELL_ALPHA)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(AppIcons.Wallet),
                    contentDescription = null,
                    tint = colors.onPrimary,
                    modifier = Modifier.size(spacing.generous),
                )
            }
            Text(
                text = stringResource(R.string.topup_wallet_label),
                style = MaterialTheme.appTypography.labelLarge,
                color = supporting,
                modifier = Modifier.weight(1f),
            )
            AnimatedVisibility(visible = wallet.isRefreshing, enter = fadeIn(), exit = fadeOut()) {
                CircularProgressIndicator(
                    modifier = Modifier.size(spacing.large),
                    color = colors.onPrimary,
                    strokeWidth = spacing.extraExtraSmall,
                )
            }
        }

        AnimatedContent(
            targetState = wallet.balanceMmk,
            transitionSpec = {
                (slideInVertically { it / 2 } + fadeIn()) togetherWith (slideOutVertically { -it / 2 } + fadeOut())
            },
            label = "walletBalance",
        ) { balance ->
            if (balance != null) {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(spacing.small)) {
                    Text(
                        text = formatGrouped(balance),
                        style = MaterialTheme.appTypography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = colors.onPrimary,
                    )
                    Text(
                        text = stringResource(R.string.topup_kyat_suffix),
                        style = MaterialTheme.appTypography.titleMedium,
                        color = supporting,
                        modifier = Modifier.padding(bottom = spacing.extraSmall),
                    )
                }
            } else {
                Text(
                    text = stringResource(if (wallet.isRefreshing) R.string.topup_wallet_refreshing else R.string.topup_wallet_unavailable),
                    style = MaterialTheme.appTypography.titleLarge,
                    color = colors.onPrimary,
                )
            }
        }

        AnimatedVisibility(visible = wallet.isStale && wallet.balanceMmk != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall),
            ) {
                Icon(
                    painter = painterResource(AppIcons.CloudOff),
                    contentDescription = null,
                    tint = supporting,
                    modifier = Modifier.size(spacing.large),
                )
                Text(
                    text = stringResource(R.string.topup_wallet_stale),
                    style = MaterialTheme.appTypography.bodySmall,
                    color = supporting,
                )
            }
        }
    }
}
