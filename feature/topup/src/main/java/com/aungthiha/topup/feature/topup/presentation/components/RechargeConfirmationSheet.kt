package com.aungthiha.topup.feature.topup.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.aungthiha.topup.core.common.format.HIGHLIGHT_SEPARATOR
import com.aungthiha.topup.core.common.format.formatKyat
import com.aungthiha.topup.core.common.format.packageHighlights
import com.aungthiha.topup.core.designsystem.components.AppBanner
import com.aungthiha.topup.core.designsystem.components.AppBannerTone
import com.aungthiha.topup.core.designsystem.components.AppDetailRow
import com.aungthiha.topup.core.designsystem.components.AppDivider
import com.aungthiha.topup.core.designsystem.components.AppOperatorLogo
import com.aungthiha.topup.core.designsystem.components.AppPrimaryButton
import com.aungthiha.topup.core.designsystem.components.AppSecondaryButton
import com.aungthiha.topup.core.designsystem.icons.AppIcons
import com.aungthiha.topup.core.designsystem.theme.appColorScheme
import com.aungthiha.topup.core.designsystem.theme.appSpacing
import com.aungthiha.topup.core.designsystem.theme.appTypography
import com.aungthiha.topup.feature.topup.R
import com.aungthiha.topup.feature.topup.domain.model.CatalogProductType
import com.aungthiha.topup.feature.topup.domain.model.RechargeSelection
import com.aungthiha.topup.feature.topup.presentation.screens.topup.RechargeConfirmationUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RechargeConfirmationSheet(
    confirmation: RechargeConfirmationUi,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = MaterialTheme.appColorScheme
    val spacing = MaterialTheme.appSpacing
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surfaceElevated,
        contentColor = colors.contentPrimary,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.screenHorizontal)
                .padding(bottom = spacing.large)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(spacing.medium),
        ) {
            Text(
                text = stringResource(R.string.topup_confirm_title),
                style = MaterialTheme.appTypography.titleLarge,
                color = colors.contentPrimary,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.medium),
            ) {
                AppOperatorLogo(url = confirmation.operator.logoUrl, operatorName = confirmation.operator.displayName)
                Column {
                    Text(
                        text = confirmation.phoneNumber,
                        style = MaterialTheme.appTypography.titleMedium,
                        color = colors.contentPrimary,
                    )
                    Text(
                        text = confirmation.operator.displayName,
                        style = MaterialTheme.appTypography.bodySmall,
                        color = colors.contentSecondary,
                    )
                }
            }
            AppDivider()
            Column {
                AppDetailRow(label = stringResource(R.string.topup_confirm_phone), value = confirmation.phoneNumber)
                AppDetailRow(label = stringResource(R.string.topup_confirm_operator), value = confirmation.operator.displayName)
                AppDetailRow(label = stringResource(R.string.topup_confirm_item), value = confirmation.selection.title())
                confirmation.selection.details()?.let {
                    AppDetailRow(label = stringResource(R.string.topup_confirm_details), value = it)
                }
                AppDetailRow(
                    label = stringResource(R.string.topup_confirm_balance),
                    value = confirmation.walletBalanceMmk?.let(::formatKyat) ?: stringResource(R.string.topup_wallet_unavailable),
                )
            }
            AppDivider()
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.topup_confirm_price),
                    style = MaterialTheme.appTypography.titleMedium,
                    color = colors.contentSecondary,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = formatKyat(confirmation.priceMmk),
                    style = MaterialTheme.appTypography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.contentPrimary,
                )
            }
            if (confirmation.isBalanceLikelyInsufficient) {
                AppBanner(
                    message = stringResource(R.string.topup_confirm_low_balance),
                    tone = AppBannerTone.Warning,
                    icon = AppIcons.Warning,
                )
            }
            AppBanner(message = stringResource(R.string.topup_confirm_simulated_note), tone = AppBannerTone.Info)
            AppPrimaryButton(text = stringResource(R.string.topup_confirm), onClick = onConfirm)
            AppSecondaryButton(text = stringResource(R.string.topup_cancel), onClick = onDismiss)
        }
    }
}

@Composable
internal fun RechargeSelection.title(): String = when (this) {
    is RechargeSelection.CustomAmount -> stringResource(R.string.topup_item_custom)
    is RechargeSelection.Product -> when (product.type) {
        CatalogProductType.DENOMINATION -> stringResource(R.string.topup_item_denomination, formatKyat(product.priceMmk))
        CatalogProductType.DATA_PACKAGE -> product.name
    }
}

internal fun RechargeSelection.details(): String? {
    val product = (this as? RechargeSelection.Product)?.product ?: return null
    if (product.type != CatalogProductType.DATA_PACKAGE) return null
    return packageHighlights(
        services = product.services,
        dataAllowanceMb = product.dataAllowanceMb,
        callMinutes = product.callMinutes,
        validityDays = product.validityDays,
    ).joinToString(HIGHLIGHT_SEPARATOR).ifEmpty { null }
}
