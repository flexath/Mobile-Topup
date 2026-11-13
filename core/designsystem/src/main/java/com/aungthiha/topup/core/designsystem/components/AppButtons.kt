package com.aungthiha.topup.core.designsystem.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.aungthiha.topup.core.designsystem.icons.AppIcons
import com.aungthiha.topup.core.designsystem.theme.AppTheme
import com.aungthiha.topup.core.designsystem.theme.appColorScheme
import com.aungthiha.topup.core.designsystem.theme.appShapes
import com.aungthiha.topup.core.designsystem.theme.appSpacing
import com.aungthiha.topup.core.designsystem.theme.appTypography

/** Full-width primary call to action. Shows a spinner instead of the label while [loading]. */
@Composable
fun AppPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    @DrawableRes leadingIcon: Int? = null,
) {
    val colors = MaterialTheme.appColorScheme
    val spacing = MaterialTheme.appSpacing
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = spacing.button),
        shape = MaterialTheme.appShapes.large,
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.primary,
            contentColor = colors.onPrimary,
            disabledContainerColor = if (loading) colors.primary else colors.surfaceVariant,
            disabledContentColor = if (loading) colors.onPrimary else colors.contentDisabled,
        ),
        contentPadding = PaddingValues(horizontal = spacing.extraLarge, vertical = spacing.medium),
    ) {
        AnimatedContent(
            targetState = loading,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "primaryButtonContent",
        ) { isLoading ->
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(spacing.extraLarge),
                    color = colors.onPrimary,
                    strokeWidth = spacing.extraExtraSmall,
                )
            } else {
                ButtonLabel(text = text, leadingIcon = leadingIcon)
            }
        }
    }
}

@Composable
fun AppSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    @DrawableRes leadingIcon: Int? = null,
    contentColor: Color = MaterialTheme.appColorScheme.primary,
) {
    val colors = MaterialTheme.appColorScheme
    val spacing = MaterialTheme.appSpacing
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = spacing.button),
        shape = MaterialTheme.appShapes.large,
        border = BorderStroke(spacing.hairline, if (enabled) colors.outline else colors.outlineVariant),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = contentColor,
            disabledContentColor = colors.contentDisabled,
        ),
        contentPadding = PaddingValues(horizontal = spacing.extraLarge, vertical = spacing.medium),
    ) {
        ButtonLabel(text = text, leadingIcon = leadingIcon)
    }
}

@Composable
fun AppTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    @DrawableRes leadingIcon: Int? = null,
) {
    val spacing = MaterialTheme.appSpacing
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.defaultMinSize(minHeight = spacing.control),
        shape = MaterialTheme.appShapes.medium,
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.appColorScheme.primary,
            disabledContentColor = MaterialTheme.appColorScheme.contentDisabled,
        ),
    ) {
        ButtonLabel(text = text, leadingIcon = leadingIcon)
    }
}

@Composable
private fun ButtonLabel(text: String, @DrawableRes leadingIcon: Int?) {
    val spacing = MaterialTheme.appSpacing
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (leadingIcon != null) {
            Icon(
                painter = painterResource(leadingIcon),
                contentDescription = null,
                modifier = Modifier.size(spacing.generous),
            )
            Spacer(Modifier.width(spacing.small))
        }
        Text(
            text = text,
            style = MaterialTheme.appTypography.titleSmall,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Composable
private fun AppButtonsPreview() {
    AppTheme {
        androidx.compose.foundation.layout.Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.appSpacing.medium),
        ) {
            AppPrimaryButton(text = "Proceed to Recharge", onClick = {})
            AppPrimaryButton(text = "Proceed to Recharge", onClick = {}, enabled = false)
            AppPrimaryButton(text = "Loading", onClick = {}, loading = true)
            AppSecondaryButton(text = "Share receipt", onClick = {}, leadingIcon = AppIcons.Share)
            AppTextButton(text = "Enter custom amount", onClick = {}, leadingIcon = AppIcons.Add)
        }
    }
}
