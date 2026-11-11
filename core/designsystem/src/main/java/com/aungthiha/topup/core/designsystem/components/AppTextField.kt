package com.aungthiha.topup.core.designsystem.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.VisualTransformation
import com.aungthiha.topup.core.designsystem.theme.appColorScheme
import com.aungthiha.topup.core.designsystem.theme.appShapes
import com.aungthiha.topup.core.designsystem.theme.appSpacing
import com.aungthiha.topup.core.designsystem.theme.appTypography

/**
 * Material 3 outlined text field themed with TopUp tokens.
 *
 * [supportingText] is shown in error color when [isError] is true, otherwise as helper text.
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    @DrawableRes leadingIcon: Int? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    val colors = MaterialTheme.appColorScheme
    val typography = MaterialTheme.appTypography
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = singleLine,
        isError = isError,
        textStyle = typography.bodyLarge,
        label = { Text(text = label, style = typography.bodyMedium) },
        placeholder = placeholder?.let { { Text(text = it, style = typography.bodyLarge) } },
        supportingText = supportingText?.let { { Text(text = it, style = typography.bodySmall) } },
        leadingIcon = leadingIcon?.let {
            {
                Icon(
                    painter = painterResource(it),
                    contentDescription = null,
                    modifier = Modifier.size(MaterialTheme.appSpacing.generous),
                )
            }
        },
        trailingIcon = trailingContent,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        shape = MaterialTheme.appShapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = colors.contentPrimary,
            unfocusedTextColor = colors.contentPrimary,
            disabledTextColor = colors.contentDisabled,
            errorTextColor = colors.contentPrimary,
            focusedContainerColor = colors.surface,
            unfocusedContainerColor = colors.surface,
            disabledContainerColor = colors.surfaceVariant,
            errorContainerColor = colors.surface,
            cursorColor = colors.primary,
            errorCursorColor = colors.error,
            focusedBorderColor = colors.primary,
            unfocusedBorderColor = colors.outline,
            disabledBorderColor = colors.outlineVariant,
            errorBorderColor = colors.error,
            focusedLeadingIconColor = colors.primary,
            unfocusedLeadingIconColor = colors.contentSecondary,
            errorLeadingIconColor = colors.error,
            focusedTrailingIconColor = colors.contentSecondary,
            unfocusedTrailingIconColor = colors.contentSecondary,
            focusedLabelColor = colors.primary,
            unfocusedLabelColor = colors.contentSecondary,
            errorLabelColor = colors.error,
            focusedPlaceholderColor = colors.contentDisabled,
            unfocusedPlaceholderColor = colors.contentDisabled,
            focusedSupportingTextColor = colors.contentSecondary,
            unfocusedSupportingTextColor = colors.contentSecondary,
            errorSupportingTextColor = colors.error,
        ),
    )
}
