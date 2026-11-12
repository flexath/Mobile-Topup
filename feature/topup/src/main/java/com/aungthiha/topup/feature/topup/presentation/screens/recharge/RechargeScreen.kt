package com.aungthiha.topup.feature.topup.presentation.screens.recharge

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.aungthiha.topup.core.common.format.formatKyat
import com.aungthiha.topup.core.designsystem.components.AppBanner
import com.aungthiha.topup.core.designsystem.components.AppBannerTone
import com.aungthiha.topup.core.designsystem.components.AppDivider
import com.aungthiha.topup.core.designsystem.components.AppEmptyState
import com.aungthiha.topup.core.designsystem.components.AppPrimaryButton
import com.aungthiha.topup.core.designsystem.components.AppTopBar
import com.aungthiha.topup.core.designsystem.icons.AppIcons
import com.aungthiha.topup.core.designsystem.theme.AppTheme
import com.aungthiha.topup.core.designsystem.theme.appColorScheme
import com.aungthiha.topup.core.designsystem.theme.appShapes
import com.aungthiha.topup.core.designsystem.theme.appSpacing
import com.aungthiha.topup.core.designsystem.theme.appTypography
import com.aungthiha.topup.core.designsystem.theme.deviceType
import com.aungthiha.topup.core.designsystem.utils.DeviceWidthClass
import com.aungthiha.topup.feature.topup.R
import com.aungthiha.topup.feature.topup.domain.model.RechargeBlocker
import com.aungthiha.topup.feature.topup.domain.model.RechargeTab
import com.aungthiha.topup.feature.topup.domain.phone.PhoneNumberStatus
import com.aungthiha.topup.feature.topup.presentation.actions.RechargeActions
import com.aungthiha.topup.feature.topup.presentation.components.DenominationOptions
import com.aungthiha.topup.feature.topup.presentation.components.PackageOptions
import com.aungthiha.topup.feature.topup.presentation.components.PhoneNumberSection
import com.aungthiha.topup.feature.topup.presentation.components.RechargeConfirmationSheet
import com.aungthiha.topup.feature.topup.presentation.components.RechargeTabContent
import com.aungthiha.topup.feature.topup.presentation.components.RechargeTabRow
import com.aungthiha.topup.feature.topup.presentation.navigation.TopupNavCallbacks
import com.aungthiha.topup.feature.topup.presentation.navigation.TopupRoutes
import com.aungthiha.topup.feature.topup.presentation.states.CatalogUiState
import com.aungthiha.topup.feature.topup.presentation.states.RechargeEvent
import com.aungthiha.topup.feature.topup.presentation.states.RechargeUiState
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.rechargeScreen(
    modifier: Modifier,
    onNavRoute: (TopupNavCallbacks) -> Unit,
) {
    composable<TopupRoutes.RechargeScreen> {
        val viewModel: RechargeViewModel = koinViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()

        LaunchedEffect(viewModel) {
            viewModel.rechargeEvents.collect { event ->
                when (event) {
                    is RechargeEvent.StartRecharge ->
                        onNavRoute(TopupNavCallbacks.RechargeScreen.ToProcessing(event.route))
                }
            }
        }

        RechargeScreen(
            state = state,
            actions = RechargeActions(
                onNavigateUp = { onNavRoute(TopupNavCallbacks.NavigateUp) },
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

/**
 * The complete mobile top-up flow. App navigation chrome is hidden here, so the Proceed CTA is
 * pinned to the bottom (above the keyboard and system navigation bar).
 */
@Composable
fun RechargeScreen(
    state: RechargeUiState,
    actions: RechargeActions,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.appSpacing
    val usesTwoPanes = MaterialTheme.deviceType.widthClass >= DeviceWidthClass.Medium

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.appColorScheme.background)
            .navigationBarsPadding()
            .imePadding(),
    ) {
        AppTopBar(
            title = stringResource(R.string.topup_recharge_title),
            onNavigateUp = actions.onNavigateUp,
            navigateUpContentDescription = stringResource(R.string.topup_back),
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
                    NumberSection(state = state, onPhoneNumberChange = actions.onPhoneNumberChange)
                }
                Column(modifier = Modifier.weight(SECONDARY_PANE_WEIGHT)) {
                    ScrollingPane(modifier = Modifier.weight(1f)) {
                        RechargeSection(state = state, actions = actions)
                    }
                    ProceedSection(state = state, onProceed = actions.onProceed)
                }
            }
        } else {
            ScrollingPane(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = spacing.screenHorizontal),
            ) {
                NumberSection(state = state, onPhoneNumberChange = actions.onPhoneNumberChange)
                RechargeSection(state = state, actions = actions)
            }
            AppDivider()
            ProceedSection(
                state = state,
                onProceed = actions.onProceed,
                modifier = Modifier.padding(horizontal = spacing.screenHorizontal),
            )
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
            .padding(top = spacing.small, bottom = spacing.large)
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(spacing.section),
        content = content,
    )
}

@Composable
private fun NumberSection(state: RechargeUiState, onPhoneNumberChange: (String) -> Unit) {
    val spacing = MaterialTheme.appSpacing
    Column(verticalArrangement = Arrangement.spacedBy(spacing.large)) {
        StatusBanners(state)
        WalletBalanceStrip(balanceMmk = state.walletBalanceMmk)
        PhoneNumberSection(
            phoneInput = state.form.phoneInput,
            status = state.phoneStatus,
            operator = state.operator,
            onPhoneNumberChange = onPhoneNumberChange,
        )
    }
}

/** Compact reminder of the cached balance while choosing an amount. */
@Composable
private fun WalletBalanceStrip(balanceMmk: Long?) {
    val colors = MaterialTheme.appColorScheme
    val spacing = MaterialTheme.appSpacing
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.appShapes.medium)
            .background(colors.primaryContainer)
            .padding(horizontal = spacing.medium, vertical = spacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.small),
    ) {
        Icon(
            painter = painterResource(AppIcons.Wallet),
            contentDescription = null,
            tint = colors.onPrimaryContainer,
            modifier = Modifier.size(spacing.generous),
        )
        Text(
            text = stringResource(R.string.topup_wallet_label),
            style = MaterialTheme.appTypography.labelLarge,
            color = colors.contentSecondary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = balanceMmk?.let(::formatKyat) ?: stringResource(R.string.topup_wallet_unavailable),
            style = MaterialTheme.appTypography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = colors.contentPrimary,
        )
    }
}

@Composable
private fun StatusBanners(state: RechargeUiState) {
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
private fun RechargeSection(state: RechargeUiState, actions: RechargeActions) {
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
private fun ProceedSection(
    state: RechargeUiState,
    onProceed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.appSpacing
    // Keeps the last message on screen while it animates out, so its height does not change mid-exit.
    var lastBlocker by remember { mutableStateOf(state.blocker) }
    if (state.blocker != null) lastBlocker = state.blocker
    // The gap below the blocker lives inside the animated content so it collapses with the text.
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = spacing.medium),
    ) {
        AnimatedVisibility(
            visible = state.blocker != null,
            enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
        ) {
            Text(
                text = lastBlocker?.let { stringResource(it.messageRes()) }.orEmpty(),
                style = MaterialTheme.appTypography.bodySmall,
                color = MaterialTheme.appColorScheme.contentSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = spacing.small),
            )
        }
        AppPrimaryButton(
            text = stringResource(R.string.topup_proceed),
            onClick = onProceed,
            enabled = state.canProceed,
        )
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
private fun RechargeScreenPreview() {
    AppTheme {
        RechargeScreen(
            state = RechargeUiState(
                walletBalanceMmk = 500_000,
                catalogState = CatalogUiState.Ready,
            ),
            actions = RechargeActions(),
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun RechargeScreenDarkPreview() {
    AppTheme(darkTheme = true) {
        RechargeScreen(
            state = RechargeUiState(
                walletBalanceMmk = 500_000,
                catalogState = CatalogUiState.Ready,
            ),
            actions = RechargeActions(),
        )
    }
}
