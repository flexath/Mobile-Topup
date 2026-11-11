package com.aungthiha.topup.feature.topup.presentation.screens.topup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.aungthiha.topup.core.designsystem.components.AppBanner
import com.aungthiha.topup.core.designsystem.components.AppBannerTone
import com.aungthiha.topup.core.designsystem.components.AppEmptyState
import com.aungthiha.topup.core.designsystem.components.AppIconButton
import com.aungthiha.topup.core.designsystem.components.AppPrimaryButton
import com.aungthiha.topup.core.designsystem.components.AppTopBar
import com.aungthiha.topup.core.designsystem.icons.AppIcons
import com.aungthiha.topup.core.designsystem.theme.AppTheme
import com.aungthiha.topup.core.designsystem.theme.appColorScheme
import com.aungthiha.topup.core.designsystem.theme.appSpacing
import com.aungthiha.topup.core.designsystem.theme.appTypography
import com.aungthiha.topup.core.designsystem.theme.deviceType
import com.aungthiha.topup.core.designsystem.utils.DeviceWidthClass
import com.aungthiha.topup.feature.topup.R
import com.aungthiha.topup.feature.topup.domain.model.RechargeBlocker
import com.aungthiha.topup.feature.topup.domain.model.RechargeTab
import com.aungthiha.topup.feature.topup.domain.phone.PhoneNumberStatus
import com.aungthiha.topup.feature.topup.presentation.components.DenominationOptions
import com.aungthiha.topup.feature.topup.presentation.states.CatalogUiState
import com.aungthiha.topup.feature.topup.presentation.states.TopupEvent
import com.aungthiha.topup.feature.topup.presentation.states.TopupUiState
import com.aungthiha.topup.feature.topup.presentation.states.WalletUiState
import com.aungthiha.topup.feature.topup.presentation.components.PackageOptions
import com.aungthiha.topup.feature.topup.presentation.components.PhoneNumberSection
import com.aungthiha.topup.feature.topup.presentation.components.RechargeConfirmationSheet
import com.aungthiha.topup.feature.topup.presentation.components.RechargeTabContent
import com.aungthiha.topup.feature.topup.presentation.components.RechargeTabRow
import com.aungthiha.topup.feature.topup.presentation.components.WalletBalanceCard
import com.aungthiha.topup.feature.topup.presentation.navigation.TopupNavCallbacks
import com.aungthiha.topup.feature.topup.presentation.navigation.TopupRoutes
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.topupScreen(
    modifier: Modifier,
    onNavRoute: (TopupNavCallbacks) -> Unit,
) {
    composable<TopupRoutes.TopupScreen> {
        val viewModel: TopupViewModel = koinViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()

        LifecycleStartEffect(viewModel) {
            viewModel.onScreenVisible()
            onStopOrDispose { }
        }
        LaunchedEffect(viewModel) {
            viewModel.topupEvents.collect { event ->
                when (event) {
                    is TopupEvent.StartRecharge -> onNavRoute(TopupNavCallbacks.TopupScreen.ToProcessing(event.route))
                }
            }
        }

        TopupScreen(
            state = state,
            actions = TopupActions(
                onSettings = { onNavRoute(TopupNavCallbacks.TopupScreen.ToSettings) },
                onPhoneNumberChange = viewModel::onPhoneNumberChange,
                onTabSelected = viewModel::onTabSelected,
                onProductSelected = viewModel::onProductSelected,
                onToggleCustomAmount = viewModel::onCustomAmountToggle,
                onCustomAmountChange = viewModel::onCustomAmountChange,
                onRetryCatalog = viewModel::onRetryCatalog,
                onProceed = viewModel::onProceed,
                onConfirm = viewModel::onConfirmRecharge,
                onDismissConfirmation = viewModel::onDismissConfirmation,
            ),
            modifier = modifier,
        )
    }
}

data class TopupActions(
    val onSettings: () -> Unit = {},
    val onPhoneNumberChange: (String) -> Unit = {},
    val onTabSelected: (RechargeTab) -> Unit = {},
    val onProductSelected: (String) -> Unit = {},
    val onToggleCustomAmount: () -> Unit = {},
    val onCustomAmountChange: (String) -> Unit = {},
    val onRetryCatalog: () -> Unit = {},
    val onProceed: () -> Unit = {},
    val onConfirm: () -> Unit = {},
    val onDismissConfirmation: () -> Unit = {},
)

@Composable
fun TopupScreen(
    state: TopupUiState,
    actions: TopupActions,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.appSpacing
    val usesTwoPanes = MaterialTheme.deviceType.widthClass >= DeviceWidthClass.Medium

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.appColorScheme.background)
            .imePadding(),
    ) {
        AppTopBar(
            title = stringResource(R.string.topup_title),
            actions = {
                AppIconButton(
                    icon = AppIcons.Settings,
                    contentDescription = stringResource(R.string.topup_settings),
                    onClick = actions.onSettings,
                )
            },
        )

        if (usesTwoPanes) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = spacing.screenHorizontal),
                horizontalArrangement = Arrangement.spacedBy(spacing.pane),
            ) {
                ScrollingPane(modifier = Modifier.weight(PRIMARY_PANE_WEIGHT)) {
                    StatusBanners(state)
                    WalletBalanceCard(wallet = state.wallet)
                    PhoneNumberSection(
                        phoneInput = state.form.phoneInput,
                        status = state.phoneStatus,
                        operator = state.operator,
                        onPhoneNumberChange = actions.onPhoneNumberChange,
                    )
                }
                ScrollingPane(modifier = Modifier.weight(SECONDARY_PANE_WEIGHT)) {
                    RechargeSection(state = state, actions = actions)
                    ProceedSection(state = state, onProceed = actions.onProceed)
                }
            }
        } else {
            ScrollingPane(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = spacing.screenHorizontal),
            ) {
                StatusBanners(state)
                WalletBalanceCard(wallet = state.wallet)
                PhoneNumberSection(
                    phoneInput = state.form.phoneInput,
                    status = state.phoneStatus,
                    operator = state.operator,
                    onPhoneNumberChange = actions.onPhoneNumberChange,
                )
                RechargeSection(state = state, actions = actions)
                ProceedSection(state = state, onProceed = actions.onProceed)
            }
        }
    }

    state.confirmation?.let { confirmation ->
        RechargeConfirmationSheet(
            confirmation = confirmation,
            onConfirm = actions.onConfirm,
            onDismiss = actions.onDismissConfirmation,
        )
    }
}

@Composable
private fun ScrollingPane(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val spacing = MaterialTheme.appSpacing
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(top = spacing.small, bottom = spacing.bottomNavigationClearance)
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(spacing.section),
        content = content,
    )
}

@Composable
private fun StatusBanners(state: TopupUiState) {
    val spacing = MaterialTheme.appSpacing
    val showOffline = !state.isOnline
    val showStaleCatalog = state.isCatalogStale && state.isOnline
    if (!showOffline && !showStaleCatalog) return
    Column(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
        AnimatedVisibility(visible = showOffline) {
            AppBanner(message = stringResource(R.string.topup_offline), tone = AppBannerTone.Warning)
        }
        AnimatedVisibility(visible = showStaleCatalog) {
            AppBanner(message = stringResource(R.string.topup_catalog_stale), tone = AppBannerTone.Warning)
        }
    }
}

@Composable
private fun RechargeSection(state: TopupUiState, actions: TopupActions) {
    val spacing = MaterialTheme.appSpacing
    Column(verticalArrangement = Arrangement.spacedBy(spacing.large)) {
        RechargeTabRow(activeTab = state.form.activeTab, onTabSelected = actions.onTabSelected)
        when {
            state.catalogState is CatalogUiState.Unavailable -> AppEmptyState(
                icon = AppIcons.CloudOff,
                title = stringResource(R.string.topup_catalog_error_title),
                message = stringResource(R.string.topup_catalog_error_message),
                iconTint = MaterialTheme.appColorScheme.warning,
                actionLabel = stringResource(R.string.topup_retry),
                onAction = actions.onRetryCatalog,
            )
            state.catalogState is CatalogUiState.Loading -> CatalogLoading()
            state.phoneStatus is PhoneNumberStatus.Unsupported -> SectionHint(stringResource(R.string.topup_unsupported_hint))
            state.phoneStatus !is PhoneNumberStatus.Detected -> SectionHint(stringResource(R.string.topup_enter_number_hint))
            else -> RechargeTabContent(activeTab = state.form.activeTab) { tab ->
                when (tab) {
                    RechargeTab.DENOMINATIONS -> DenominationOptions(
                        denominations = state.denominations,
                        selectedProductId = state.form.selectedProductId,
                        isCustomAmountVisible = state.form.isCustomAmountVisible,
                        customAmountInput = state.form.customAmountInput,
                        customAmountValidation = state.customAmountValidation,
                        customAmountRule = state.customAmountRule,
                        onSelect = actions.onProductSelected,
                        onToggleCustomAmount = actions.onToggleCustomAmount,
                        onCustomAmountChange = actions.onCustomAmountChange,
                    )
                    RechargeTab.PACKAGES -> PackageOptions(
                        packages = state.packages,
                        selectedProductId = state.form.selectedProductId,
                        showDemoNote = state.hasDemoPackages,
                        onSelect = actions.onProductSelected,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProceedSection(state: TopupUiState, onProceed: () -> Unit) {
    val spacing = MaterialTheme.appSpacing
    Column(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
        AppPrimaryButton(
            text = stringResource(R.string.topup_proceed),
            onClick = onProceed,
            enabled = state.canProceed,
        )
        state.blocker?.let { blocker ->
            Text(
                text = stringResource(blocker.messageRes()),
                style = MaterialTheme.appTypography.bodySmall,
                color = MaterialTheme.appColorScheme.contentSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun CatalogLoading() {
    val spacing = MaterialTheme.appSpacing
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = spacing.extraLarge),
        horizontalArrangement = Arrangement.spacedBy(spacing.medium, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(spacing.extraLarge),
            color = MaterialTheme.appColorScheme.primary,
            strokeWidth = spacing.extraExtraSmall,
        )
        Text(
            text = stringResource(R.string.topup_catalog_loading),
            style = MaterialTheme.appTypography.bodyMedium,
            color = MaterialTheme.appColorScheme.contentSecondary,
        )
    }
}

@Composable
private fun SectionHint(text: String) {
    Text(
        text = text,
        style = MaterialTheme.appTypography.bodyMedium,
        color = MaterialTheme.appColorScheme.contentSecondary,
        modifier = Modifier.padding(vertical = MaterialTheme.appSpacing.large),
    )
}

private fun RechargeBlocker.messageRes(): Int = when (this) {
    RechargeBlocker.INVALID_NUMBER -> R.string.topup_blocker_invalid_number
    RechargeBlocker.UNSUPPORTED_OPERATOR -> R.string.topup_blocker_unsupported
    RechargeBlocker.CATALOG_UNAVAILABLE -> R.string.topup_blocker_catalog
    RechargeBlocker.NO_SELECTION -> R.string.topup_blocker_no_selection
    RechargeBlocker.INVALID_CUSTOM_AMOUNT -> R.string.topup_blocker_invalid_amount
    RechargeBlocker.OFFLINE -> R.string.topup_blocker_offline
}

private const val PRIMARY_PANE_WEIGHT = 0.42f
private const val SECONDARY_PANE_WEIGHT = 0.58f

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun TopupScreenPreview() {
    AppTheme {
        TopupScreen(
            state = TopupUiState(
                wallet = WalletUiState(balanceMmk = 500_000),
                catalogState = CatalogUiState.Ready,
            ),
            actions = TopupActions(),
        )
    }
}
