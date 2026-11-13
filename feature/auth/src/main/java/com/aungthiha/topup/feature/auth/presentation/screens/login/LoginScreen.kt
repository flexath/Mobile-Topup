package com.aungthiha.topup.feature.auth.presentation.screens.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.aungthiha.topup.core.designsystem.components.AppBanner
import com.aungthiha.topup.core.designsystem.components.AppBannerTone
import com.aungthiha.topup.core.designsystem.components.AppPrimaryButton
import com.aungthiha.topup.core.designsystem.theme.AppTheme
import com.aungthiha.topup.feature.auth.R
import com.aungthiha.topup.feature.auth.presentation.components.AuthLanguagePicker
import com.aungthiha.topup.feature.auth.presentation.components.AuthScaffold
import com.aungthiha.topup.feature.auth.presentation.components.AuthSwitchPrompt
import com.aungthiha.topup.feature.auth.presentation.components.EmailField
import com.aungthiha.topup.feature.auth.presentation.components.PasswordField
import com.aungthiha.topup.feature.auth.presentation.components.messageRes
import com.aungthiha.topup.feature.auth.presentation.navigation.AuthNavCallbacks
import com.aungthiha.topup.feature.auth.presentation.navigation.AuthRoutes
import com.aungthiha.topup.feature.auth.presentation.states.LoginUiState
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.loginScreen(
    modifier: Modifier,
    onNavRoute: (AuthNavCallbacks) -> Unit,
) {
    composable<AuthRoutes.LoginScreen> {
        val viewModel: LoginViewModel = koinViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        LoginScreen(
            state = state,
            onEmailChange = viewModel::onEmailChange,
            onPasswordChange = viewModel::onPasswordChange,
            onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
            onSubmit = viewModel::onSubmit,
            onRegister = { onNavRoute(AuthNavCallbacks.LoginScreen.ToRegister) },
            modifier = modifier,
        )
    }
}

@Composable
fun LoginScreen(
    state: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSubmit: () -> Unit,
    onRegister: () -> Unit,
    modifier: Modifier = Modifier,
    languagePicker: @Composable () -> Unit = { AuthLanguagePicker() },
) {
    val focusManager = LocalFocusManager.current
    AuthScaffold(
        title = R.string.auth_login_title,
        subtitle = R.string.auth_login_subtitle,
        modifier = modifier,
        languagePicker = languagePicker,
    ) {
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
            errorText = state.passwordValidation.messageRes()?.takeIf { state.showValidation }?.let { stringResource(it) },
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
            text = stringResource(R.string.auth_sign_in),
            onClick = {
                focusManager.clearFocus()
                onSubmit()
            },
            loading = state.isSubmitting,
        )
        AuthSwitchPrompt(
            prompt = R.string.auth_no_account,
            action = R.string.auth_register_link,
            onClick = onRegister,
            enabled = !state.isSubmitting,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    AppTheme {
        LoginScreen(
            state = LoginUiState(email = "aung@example.com"),
            onEmailChange = {},
            onPasswordChange = {},
            onTogglePasswordVisibility = {},
            onSubmit = {},
            onRegister = {},
            languagePicker = {},
        )
    }
}
