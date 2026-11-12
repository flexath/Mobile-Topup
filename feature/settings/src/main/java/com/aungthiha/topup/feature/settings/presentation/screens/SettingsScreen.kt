package com.aungthiha.topup.feature.settings.presentation.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.aungthiha.topup.core.datastore.model.AppearanceMode
import com.aungthiha.topup.core.designsystem.components.AppCard
import com.aungthiha.topup.feature.settings.presentation.states.SettingsUiState
import com.aungthiha.topup.core.designsystem.components.AppDivider
import com.aungthiha.topup.core.designsystem.components.AppSecondaryButton
import com.aungthiha.topup.core.designsystem.components.AppTopBar
import com.aungthiha.topup.core.designsystem.icons.AppIcons
import com.aungthiha.topup.core.designsystem.theme.AppTheme
import com.aungthiha.topup.core.designsystem.theme.appColorScheme
import com.aungthiha.topup.core.designsystem.theme.appSpacing
import com.aungthiha.topup.core.designsystem.theme.appTypography
import com.aungthiha.topup.feature.settings.R
import com.aungthiha.topup.feature.settings.presentation.navigation.SettingsNavCallbacks
import com.aungthiha.topup.feature.settings.presentation.navigation.SettingsRoutes
import org.koin.compose.viewmodel.koinViewModel

internal fun NavGraphBuilder.settingsScreen(
    appVersion: String,
    modifier: Modifier,
    onNavRoute: (SettingsNavCallbacks) -> Unit,
) {
    composable<SettingsRoutes.SettingsScreen> {
        val viewModel: SettingsViewModel = koinViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        SettingsScreen(
            state = state,
            appVersion = appVersion,
            onNavigateUp = { onNavRoute(SettingsNavCallbacks.NavigateUp) },
            onAppearanceSelected = viewModel::onAppearanceSelected,
            onLogoutClick = viewModel::onLogoutClick,
            onConfirmLogout = viewModel::onConfirmLogout,
            onDismissLogout = viewModel::onDismissLogout,
            modifier = modifier,
        )
    }
}

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    appVersion: String,
    onNavigateUp: () -> Unit,
    onAppearanceSelected: (AppearanceMode) -> Unit,
    onLogoutClick: () -> Unit,
    onConfirmLogout: () -> Unit,
    onDismissLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.appSpacing
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.appColorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppTopBar(
            title = stringResource(R.string.settings_title),
            onNavigateUp = onNavigateUp,
            navigateUpContentDescription = stringResource(R.string.settings_back),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = spacing.screenHorizontal, vertical = spacing.large),
            verticalArrangement = Arrangement.spacedBy(spacing.section),
        ) {
            SettingsSection(title = R.string.settings_appearance) {
                Column(Modifier.selectableGroup()) {
                    AppearanceOption(
                        label = R.string.settings_appearance_system,
                        description = R.string.settings_appearance_system_description,
                        selected = state.appearanceMode == AppearanceMode.SYSTEM,
                        onClick = { onAppearanceSelected(AppearanceMode.SYSTEM) },
                    )
                    AppearanceOption(
                        label = R.string.settings_appearance_light,
                        selected = state.appearanceMode == AppearanceMode.LIGHT,
                        onClick = { onAppearanceSelected(AppearanceMode.LIGHT) },
                    )
                    AppearanceOption(
                        label = R.string.settings_appearance_dark,
                        selected = state.appearanceMode == AppearanceMode.DARK,
                        onClick = { onAppearanceSelected(AppearanceMode.DARK) },
                    )
                }
            }

            SettingsSection(title = R.string.settings_account) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.medium),
                ) {
                    Icon(
                        painter = painterResource(AppIcons.User),
                        contentDescription = null,
                        tint = MaterialTheme.appColorScheme.primary,
                        modifier = Modifier.size(spacing.extraLarge),
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.settings_signed_in_as),
                            style = MaterialTheme.appTypography.bodySmall,
                            color = MaterialTheme.appColorScheme.contentSecondary,
                        )
                        Text(
                            text = state.email ?: stringResource(R.string.settings_email_unknown),
                            style = MaterialTheme.appTypography.bodyLarge,
                            color = MaterialTheme.appColorScheme.contentPrimary,
                        )
                    }
                }
                AppDivider(Modifier.padding(vertical = spacing.medium))
                AppSecondaryButton(
                    text = stringResource(R.string.settings_logout),
                    onClick = onLogoutClick,
                    enabled = !state.isSigningOut,
                    leadingIcon = AppIcons.Logout,
                    contentColor = MaterialTheme.appColorScheme.error,
                )
            }

            Text(
                text = stringResource(R.string.settings_version, appVersion),
                style = MaterialTheme.appTypography.bodyMedium,
                color = MaterialTheme.appColorScheme.contentSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (state.isLogoutConfirmationVisible) {
        AlertDialog(
            onDismissRequest = onDismissLogout,
            title = {
                Text(
                    stringResource(R.string.settings_logout_confirm_title),
                    style = MaterialTheme.appTypography.titleLarge
                )
            },
            text = {
                Text(
                    stringResource(R.string.settings_logout_confirm_message),
                    style = MaterialTheme.appTypography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = onConfirmLogout) {
                    Text(
                        stringResource(R.string.settings_logout_confirm),
                        color = MaterialTheme.appColorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissLogout) { Text(stringResource(R.string.settings_logout_cancel)) }
            },
            containerColor = MaterialTheme.appColorScheme.surfaceElevated,
        )
    }
}

@Composable
private fun SettingsSection(@StringRes title: Int, content: @Composable ColumnScope.() -> Unit) {
    val spacing = MaterialTheme.appSpacing
    Column(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
        Text(
            text = stringResource(title),
            style = MaterialTheme.appTypography.titleSmall,
            color = MaterialTheme.appColorScheme.contentSecondary,
            modifier = Modifier.padding(horizontal = spacing.extraSmall),
        )
        AppCard(modifier = Modifier.fillMaxWidth(), content = content)
    }
}

@Composable
private fun AppearanceOption(
    @StringRes label: Int,
    selected: Boolean,
    onClick: () -> Unit,
    @StringRes description: Int? = null,
) {
    val colors = MaterialTheme.appColorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(vertical = MaterialTheme.appSpacing.extraSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = colors.primary,
                unselectedColor = colors.outline
            ),
        )
        Text(
            text = stringResource(label),
            style = MaterialTheme.appTypography.bodyLarge,
            color = colors.contentPrimary,
            modifier = Modifier.padding(start = MaterialTheme.appSpacing.medium)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    AppTheme {
        SettingsScreen(
            state = SettingsUiState(email = "aung@example.com"),
            appVersion = "1.0.0",
            onNavigateUp = {},
            onAppearanceSelected = {},
            onLogoutClick = {},
            onConfirmLogout = {},
            onDismissLogout = {},
        )
    }
}
