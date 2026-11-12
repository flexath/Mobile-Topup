package com.aungthiha.topup.feature.auth.presentation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.aungthiha.topup.core.designsystem.components.AppIconButton
import com.aungthiha.topup.core.designsystem.components.AppTextButton
import com.aungthiha.topup.core.designsystem.components.AppTextField
import com.aungthiha.topup.core.designsystem.icons.AppIcons
import com.aungthiha.topup.core.designsystem.theme.appColorScheme
import com.aungthiha.topup.core.designsystem.theme.appShapes
import com.aungthiha.topup.core.designsystem.theme.appSpacing
import com.aungthiha.topup.core.designsystem.theme.appTypography
import com.aungthiha.topup.feature.auth.R

/** Scrollable, width-capped column shared by Login and Register so both adapt to any window. */
@Composable
internal fun AuthScaffold(
    @StringRes title: Int,
    @StringRes subtitle: Int,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val spacing = MaterialTheme.appSpacing
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.appColorScheme.background)
            .imePadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = spacing.messageMaxWidth + spacing.huge)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.screenHorizontal, vertical = spacing.section),
            verticalArrangement = Arrangement.spacedBy(spacing.medium),
        ) {
            AuthBrandHeader(title = title, subtitle = subtitle)
            content()
        }
    }
}

@Composable
private fun AuthBrandHeader(@StringRes title: Int, @StringRes subtitle: Int) {
    val colors = MaterialTheme.appColorScheme
    val spacing = MaterialTheme.appSpacing
    Column(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
        Text(
            text = stringResource(title),
            style = MaterialTheme.appTypography.headlineSmall,
            color = colors.contentPrimary,
            modifier = Modifier.padding(top = spacing.large),
        )
        Text(
            text = stringResource(subtitle),
            style = MaterialTheme.appTypography.bodyMedium,
            color = colors.contentSecondary,
        )
    }
}

@Composable
internal fun EmailField(
    value: String,
    onValueChange: (String) -> Unit,
    errorText: String?,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    AppTextField(
        value = value,
        onValueChange = onValueChange,
        label = stringResource(R.string.auth_email_label),
        placeholder = stringResource(R.string.auth_email_placeholder),
        leadingIcon = AppIcons.Mail,
        isError = errorText != null,
        supportingText = errorText,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
        modifier = modifier,
    )
}

@Composable
internal fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    @StringRes label: Int,
    visible: Boolean,
    onToggleVisibility: () -> Unit,
    errorText: String?,
    enabled: Boolean,
    imeAction: ImeAction,
    onImeAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppTextField(
        value = value,
        onValueChange = onValueChange,
        label = stringResource(label),
        leadingIcon = AppIcons.Lock,
        isError = errorText != null,
        supportingText = errorText,
        enabled = enabled,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
        keyboardActions = KeyboardActions(onDone = { onImeAction() }, onNext = { onImeAction() }),
        trailingContent = {
            AppIconButton(
                icon = if (visible) AppIcons.VisibilityOff else AppIcons.Visibility,
                contentDescription = stringResource(if (visible) R.string.auth_hide_password else R.string.auth_show_password),
                onClick = onToggleVisibility,
            )
        },
        modifier = modifier,
    )
}

@Composable
internal fun AuthSwitchPrompt(
    @StringRes prompt: Int,
    @StringRes action: Int,
    onClick: () -> Unit,
    enabled: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(prompt),
            style = MaterialTheme.appTypography.bodyMedium,
            color = MaterialTheme.appColorScheme.contentSecondary,
        )
        AppTextButton(text = stringResource(action), onClick = onClick, enabled = enabled)
    }
}
