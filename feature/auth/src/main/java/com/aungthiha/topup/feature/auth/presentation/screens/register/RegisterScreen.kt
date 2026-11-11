package com.aungthiha.topup.feature.auth.presentation.screens.register

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.aungthiha.topup.core.designsystem.components.AppBanner
import com.aungthiha.topup.core.designsystem.components.AppBannerTone
import com.aungthiha.topup.core.designsystem.components.AppCard
import com.aungthiha.topup.core.designsystem.components.AppPrimaryButton
import com.aungthiha.topup.core.designsystem.icons.AppIcons
import com.aungthiha.topup.core.designsystem.theme.AppTheme
import com.aungthiha.topup.core.designsystem.theme.appColorScheme
import com.aungthiha.topup.core.designsystem.theme.appSpacing
import com.aungthiha.topup.core.designsystem.theme.appTypography
import com.aungthiha.topup.feature.auth.R
import com.aungthiha.topup.feature.auth.presentation.components.AuthScaffold
import com.aungthiha.topup.feature.auth.presentation.components.AuthSwitchPrompt
import com.aungthiha.topup.feature.auth.presentation.components.EmailField
import com.aungthiha.topup.feature.auth.presentation.components.PasswordField
import com.aungthiha.topup.feature.auth.presentation.components.messageRes
import com.aungthiha.topup.feature.auth.presentation.navigation.AuthNavCallbacks
import com.aungthiha.topup.feature.auth.presentation.navigation.AuthRoutes
import com.aungthiha.topup.shared.auth.domain.model.CredentialsValidator
import com.aungthiha.topup.shared.auth.domain.model.PasswordValidation
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.registerScreen(
    modifier: Modifier,
    onNavRoute: (AuthNavCallbacks) -> Unit,
) {
    composable<AuthRoutes.RegisterScreen> {
        val viewModel: RegisterViewModel = koinViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        RegisterScreen(
            state = state,
            onEmailChange = viewModel::onEmailChange,
            onPasswordChange = viewModel::onPasswordChange,
            onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
            onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
            onSubmit = viewModel::onSubmit,
            onSignIn = { onNavRoute(AuthNavCallbacks.RegisterScreen.ToLogin) },
            modifier = modifier,
        )
    }
}

@Composable
fun RegisterScreen(
    state: RegisterUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSubmit: () -> Unit,
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    AuthScaffold(
        title = R.string.auth_register_title,
        subtitle = R.string.auth_register_subtitle,
        modifier = modifier,
    ) {
        AnimatedContent(
            targetState = state.confirmationSentTo,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "registerContent",
        ) { confirmationEmail ->
            if (confirmationEmail != null) {
                ConfirmationSentCard(email = confirmationEmail, onSignIn = onSignIn)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.appSpacing.large)) {
                    EmailField(
                        value = state.email,
                        onValueChange = onEmailChange,
                        errorText = state.emailValidation.messageRes()?.takeIf { state.showValidation }?.let { stringResource(it) },
                        enabled = !state.isSubmitting,
                    )
                    PasswordField(
                        value = state.password,
                        onValueChange = onPasswordChange,
                        label = R.string.auth_password_label,
                        visible = state.isPasswordVisible,
                        onToggleVisibility = onTogglePasswordVisibility,
                        errorText = state.passwordValidation.errorText(state.showValidation),
                        enabled = !state.isSubmitting,
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                    )
                    PasswordField(
                        value = state.confirmPassword,
                        onValueChange = onConfirmPasswordChange,
                        label = R.string.auth_confirm_password_label,
                        visible = state.isPasswordVisible,
                        onToggleVisibility = onTogglePasswordVisibility,
                        errorText = state.confirmationValidation.messageRes()?.takeIf { state.showValidation }?.let { stringResource(it) },
                        enabled = !state.isSubmitting,
                        imeAction = ImeAction.Done,
                        onImeAction = {
                            focusManager.clearFocus()
                            onSubmit()
                        },
                    )
                    AnimatedVisibility(visible = state.error != null) {
                        state.error?.let { AppBanner(message = stringResource(it.messageRes()), tone = AppBannerTone.Error) }
                    }
                    AppPrimaryButton(
                        text = stringResource(R.string.auth_create_account),
                        onClick = {
                            focusManager.clearFocus()
                            onSubmit()
                        },
                        loading = state.isSubmitting,
                    )
                    AuthSwitchPrompt(
                        prompt = R.string.auth_have_account,
                        action = R.string.auth_sign_in_link,
                        onClick = onSignIn,
                        enabled = !state.isSubmitting,
                    )
                }
            }
        }
    }
}

@Composable
private fun PasswordValidation.errorText(showValidation: Boolean): String? {
    if (!showValidation) return null
    return when (this) {
        PasswordValidation.TOO_SHORT -> stringResource(R.string.auth_error_password_short, CredentialsValidator.MIN_PASSWORD_LENGTH)
        else -> messageRes()?.let { stringResource(it) }
    }
}

@Composable
private fun ConfirmationSentCard(email: String, onSignIn: () -> Unit) {
    val spacing = MaterialTheme.appSpacing
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.medium)) {
            AppBanner(
                message = stringResource(R.string.auth_confirmation_title),
                tone = AppBannerTone.Info,
                icon = AppIcons.Mail,
            )
            Text(
                text = stringResource(R.string.auth_confirmation_message, email),
                style = MaterialTheme.appTypography.bodyMedium,
                color = MaterialTheme.appColorScheme.contentSecondary,
            )
            AppPrimaryButton(text = stringResource(R.string.auth_go_to_sign_in), onClick = onSignIn)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterScreenPreview() {
    AppTheme {
        RegisterScreen(
            state = RegisterUiState(email = "aung@example.com"),
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onTogglePasswordVisibility = {},
            onSubmit = {},
            onSignIn = {},
        )
    }
}
