package com.aungthiha.topup.feature.topup.presentation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.aungthiha.topup.core.designsystem.components.AppCard
import com.aungthiha.topup.core.designsystem.components.AppOperatorLogo
import com.aungthiha.topup.core.designsystem.theme.AppTheme
import com.aungthiha.topup.core.designsystem.theme.appColorScheme
import com.aungthiha.topup.core.designsystem.theme.appSpacing
import com.aungthiha.topup.core.designsystem.theme.appTypography
import com.aungthiha.topup.feature.topup.R
import com.aungthiha.topup.feature.topup.domain.model.OperatorCode
import com.aungthiha.topup.feature.topup.presentation.states.OperatorUiModel

/** Logos of every operator the recharge flow supports. */
@Composable
internal fun SupportedOperatorsCard(
    operators: List<OperatorUiModel>,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.appSpacing
    AppCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.large)) {
            SectionTitle(R.string.topup_operators_title)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.small),
            ) {
                operators.forEach { operator ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(spacing.small),
                    ) {
                        AppOperatorLogo(url = operator.logoUrl, operatorName = operator.displayName)
                        Text(
                            text = operator.displayName,
                            style = MaterialTheme.appTypography.labelMedium,
                            color = MaterialTheme.appColorScheme.contentSecondary,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

/** Three-step explainer of the recharge flow. */
@Composable
internal fun RechargeStepsCard(modifier: Modifier = Modifier) {
    val spacing = MaterialTheme.appSpacing
    AppCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.large)) {
            SectionTitle(R.string.topup_steps_title)
            RechargeStep(number = 1, title = R.string.topup_step_number_title, message = R.string.topup_step_number_message)
            RechargeStep(number = 2, title = R.string.topup_step_amount_title, message = R.string.topup_step_amount_message)
            RechargeStep(number = 3, title = R.string.topup_step_confirm_title, message = R.string.topup_step_confirm_message)
        }
    }
}

@Composable
private fun RechargeStep(number: Int, @StringRes title: Int, @StringRes message: Int) {
    val colors = MaterialTheme.appColorScheme
    val spacing = MaterialTheme.appSpacing
    Row(horizontalArrangement = Arrangement.spacedBy(spacing.medium)) {
        Box(
            modifier = Modifier
                .size(spacing.hero)
                .clip(CircleShape)
                .background(colors.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = number.toString(),
                style = MaterialTheme.appTypography.labelLarge,
                color = colors.onPrimaryContainer,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(spacing.extraExtraSmall),
        ) {
            Text(
                text = stringResource(title),
                style = MaterialTheme.appTypography.titleSmall,
                color = colors.contentPrimary,
            )
            Text(
                text = stringResource(message),
                style = MaterialTheme.appTypography.bodySmall,
                color = colors.contentSecondary,
            )
        }
    }
}

@Composable
private fun SectionTitle(@StringRes text: Int) {
    Text(
        text = stringResource(text),
        style = MaterialTheme.appTypography.titleSmall,
        color = MaterialTheme.appColorScheme.contentPrimary,
    )
}

@Preview(showBackground = true)
@Composable
private fun WalletOverviewSectionsPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(MaterialTheme.appSpacing.large),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.appSpacing.large),
        ) {
            SupportedOperatorsCard(
                operators = OperatorCode.entries.map { OperatorUiModel(it, it.fallbackDisplayName, logoUrl = null) },
            )
            RechargeStepsCard()
        }
    }
}
