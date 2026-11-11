package com.aungthiha.topup.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.aungthiha.topup.core.designsystem.theme.AppTheme
import com.aungthiha.topup.core.designsystem.theme.appColorScheme
import com.aungthiha.topup.core.designsystem.theme.appSpacing
import com.aungthiha.topup.core.designsystem.theme.appTypography

@Immutable
data class AppReceiptLine(val label: String, val value: String)

/**
 * Transaction receipt: operator + status header, prominent amount, then label/value lines.
 * Contains no app branding so it can be captured and shared as-is.
 */
@Composable
fun AppReceiptCard(
    operatorName: String,
    operatorLogoUrl: String?,
    statusText: String,
    amountLabel: String,
    amountText: String,
    lines: List<AppReceiptLine>,
    modifier: Modifier = Modifier,
    allowHardwareBitmap: Boolean = false,
) {
    val colors = MaterialTheme.appColorScheme
    val spacing = MaterialTheme.appSpacing
    AppCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.medium),
        ) {
            AppOperatorLogo(
                url = operatorLogoUrl,
                operatorName = operatorName,
                allowHardwareBitmap = allowHardwareBitmap,
            )
            Text(
                text = operatorName,
                style = MaterialTheme.appTypography.titleMedium,
                color = colors.contentPrimary,
                modifier = Modifier.weight(1f),
            )
            AppSuccessChip(text = statusText)
        }
        Column(
            modifier = Modifier.padding(vertical = spacing.large),
            verticalArrangement = Arrangement.spacedBy(spacing.extraExtraSmall),
        ) {
            Text(
                text = amountLabel,
                style = MaterialTheme.appTypography.bodySmall,
                color = colors.contentSecondary,
            )
            Text(
                text = amountText,
                style = MaterialTheme.appTypography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.contentPrimary,
            )
        }
        AppDivider()
        Column(modifier = Modifier.padding(top = spacing.small)) {
            lines.forEach { line -> AppDetailRow(label = line.label, value = line.value) }
        }
    }
}

@Preview
@Composable
private fun AppReceiptCardPreview() {
    AppTheme {
        AppReceiptCard(
            operatorName = "MPT",
            operatorLogoUrl = null,
            statusText = "Success",
            amountLabel = "Amount",
            amountText = "20,000 Ks",
            lines = listOf(
                AppReceiptLine("Transaction ID", "380b7498-6985-43d5-804e-f4298c936f0d"),
                AppReceiptLine("Date & time", "11 Nov 2026, 2:24 PM"),
                AppReceiptLine("Mobile number", "09420000001"),
                AppReceiptLine("Package", "Social Pack"),
                AppReceiptLine("Details", "YouTube + TikTok · 5 GB · 50 Minutes · 30 Days"),
            ),
        )
    }
}
