package com.aungthiha.topup.feature.topup.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.aungthiha.topup.core.designsystem.components.AppOperatorLogo
import com.aungthiha.topup.core.designsystem.components.AppTextField
import com.aungthiha.topup.core.designsystem.icons.AppIcons
import com.aungthiha.topup.core.designsystem.theme.appColorScheme
import com.aungthiha.topup.core.designsystem.theme.appShapes
import com.aungthiha.topup.core.designsystem.theme.appSpacing
import com.aungthiha.topup.core.designsystem.theme.appTypography
import com.aungthiha.topup.feature.topup.R
import com.aungthiha.topup.feature.topup.domain.phone.InvalidPhoneReason
import com.aungthiha.topup.feature.topup.domain.phone.PhoneNumberStatus
import com.aungthiha.topup.feature.topup.presentation.screens.topup.OperatorUiModel

@Composable
internal fun PhoneNumberSection(
    phoneInput: String,
    status: PhoneNumberStatus,
    operator: OperatorUiModel?,
    onPhoneNumberChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val spacing = MaterialTheme.appSpacing
    val (supporting, isError) = status.supportingText()

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(spacing.small)) {
        AppTextField(
            value = phoneInput,
            onValueChange = onPhoneNumberChange,
            label = stringResource(R.string.topup_phone_label),
            placeholder = stringResource(R.string.topup_phone_placeholder),
            leadingIcon = AppIcons.Smartphone,
            supportingText = supporting,
            isError = isError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        )

        AnimatedVisibility(
            visible = operator != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            operator?.let { DetectedOperatorRow(operator = it) }
        }
    }
}

@Composable
private fun DetectedOperatorRow(operator: OperatorUiModel) {
    val colors = MaterialTheme.appColorScheme
    val spacing = MaterialTheme.appSpacing
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.appShapes.medium)
            .background(colors.surfaceVariant)
            .padding(horizontal = spacing.medium, vertical = spacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        AppOperatorLogo(url = operator.logoUrl, operatorName = operator.displayName, size = spacing.extended)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.topup_operator_detected),
                style = MaterialTheme.appTypography.labelSmall,
                color = colors.contentSecondary,
            )
            Text(
                text = operator.displayName,
                style = MaterialTheme.appTypography.titleSmall,
                color = colors.contentPrimary,
            )
        }
        Icon(
            painter = painterResource(AppIcons.CheckCircle),
            contentDescription = null,
            tint = colors.success,
            modifier = Modifier.size(spacing.generous),
        )
    }
}

@Composable
private fun PhoneNumberStatus.supportingText(): Pair<String?, Boolean> = when (this) {
    PhoneNumberStatus.Empty, is PhoneNumberStatus.Incomplete -> stringResource(R.string.topup_phone_helper) to false
    is PhoneNumberStatus.Invalid -> stringResource(
        when (reason) {
            InvalidPhoneReason.INVALID_CHARACTERS -> R.string.topup_phone_invalid_characters
            InvalidPhoneReason.NOT_LOCAL_MOBILE_FORMAT -> R.string.topup_phone_invalid_format
            InvalidPhoneReason.TOO_LONG -> R.string.topup_phone_too_long
        },
    ) to true
    is PhoneNumberStatus.Unsupported -> stringResource(R.string.topup_phone_unsupported) to true
    is PhoneNumberStatus.Detected -> null to false
}
