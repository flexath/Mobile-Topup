package com.aungthiha.topup.feature.topup.presentation.screens.topup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.aungthiha.topup.core.designsystem.components.AppBanner
import com.aungthiha.topup.core.designsystem.components.AppBannerTone
import com.aungthiha.topup.core.designsystem.components.AppIconButton
import com.aungthiha.topup.core.designsystem.components.AppTopBar
import com.aungthiha.topup.core.designsystem.icons.AppIcons
import com.aungthiha.topup.core.designsystem.theme.AppTheme
import com.aungthiha.topup.core.designsystem.theme.appColorScheme
import com.aungthiha.topup.core.designsystem.theme.appSpacing
import com.aungthiha.topup.core.designsystem.theme.deviceType
import com.aungthiha.topup.core.designsystem.utils.DeviceWidthClass
import com.aungthiha.topup.feature.topup.R
import com.aungthiha.topup.feature.topup.domain.model.OperatorCode
import com.aungthiha.topup.feature.topup.presentation.components.RechargeEntryCard
import com.aungthiha.topup.feature.topup.presentation.components.RechargeStepsCard
import com.aungthiha.topup.feature.topup.presentation.components.SupportedOperatorsCard
import com.aungthiha.topup.feature.topup.presentation.components.WalletBalanceCard
import com.aungthiha.topup.feature.topup.presentation.navigation.TopupNavCallbacks
import com.aungthiha.topup.feature.topup.presentation.navigation.TopupRoutes
import com.aungthiha.topup.feature.topup.presentation.states.OperatorUiModel
import com.aungthiha.topup.feature.topup.presentation.states.TopupUiState
import com.aungthiha.topup.feature.topup.presentation.states.WalletUiState
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

        TopupScreen(
            state = state,
            onSettings = { onNavRoute(TopupNavCallbacks.TopupScreen.ToSettings) },
            onStartTopup = { onNavRoute(TopupNavCallbacks.TopupScreen.ToRecharge) },
            modifier = modifier,
        )
    }
}

/** Wallet overview and entry point into the dedicated recharge screen. */
@Composable
fun TopupScreen(
    state: TopupUiState,
    onSettings: () -> Unit,
    onStartTopup: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.appSpacing
    val usesTwoPanes = MaterialTheme.deviceType.widthClass >= DeviceWidthClass.Medium

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.appColorScheme.background),
    ) {
        AppTopBar(
            title = stringResource(R.string.topup_title),
            actions = {
                AppIconButton(
                    icon = AppIcons.Settings,
                    contentDescription = stringResource(R.string.topup_settings),
                    onClick = onSettings,
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
                ScrollingPane(modifier = Modifier.weight(1f)) {
                    OfflineBanner(isOnline = state.isOnline)
                    WalletBalanceCard(wallet = state.wallet)
                    RevealOnFirstEntry { RechargeEntryCard(onStartTopup = onStartTopup) }
                }
                ScrollingPane(modifier = Modifier.weight(1f)) {
                    SupportingSections(operators = state.operators)
                }
            }
        } else {
            ScrollingPane(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = spacing.screenHorizontal),
            ) {
                OfflineBanner(isOnline = state.isOnline)
                WalletBalanceCard(wallet = state.wallet)
                RevealOnFirstEntry { RechargeEntryCard(onStartTopup = onStartTopup) }
                SupportingSections(operators = state.operators)
            }
        }
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
            .padding(top = spacing.small, bottom = spacing.bottomNavigationClearance),
        verticalArrangement = Arrangement.spacedBy(spacing.section),
        content = content,
    )
}

@Composable
private fun ColumnScope.SupportingSections(operators: List<OperatorUiModel>) {
    if (operators.isNotEmpty()) SupportedOperatorsCard(operators = operators)
    RechargeStepsCard()
}

@Composable
private fun ColumnScope.OfflineBanner(isOnline: Boolean) {
    AnimatedVisibility(visible = !isOnline) {
        AppBanner(message = stringResource(R.string.topup_overview_offline), tone = AppBannerTone.Warning)
    }
}

/** Fades the content in the first time the screen is shown; not replayed when returning to it. */
@Composable
private fun RevealOnFirstEntry(content: @Composable () -> Unit) {
    var revealed by rememberSaveable { mutableStateOf(false) }
    val visibleState = remember { MutableTransitionState(revealed).apply { targetState = true } }
    LaunchedEffect(Unit) { revealed = true }
    AnimatedVisibility(
        visibleState = visibleState,
        enter = fadeIn() + slideInVertically { it / REVEAL_OFFSET_DIVISOR },
    ) {
        content()
    }
}

private const val REVEAL_OFFSET_DIVISOR = 6

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun TopupScreenPreview() {
    AppTheme {
        TopupScreen(
            state = previewState(),
            onSettings = {},
            onStartTopup = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun TopupScreenDarkPreview() {
    AppTheme(darkTheme = true) {
        TopupScreen(
            state = previewState(),
            onSettings = {},
            onStartTopup = {},
        )
    }
}

private fun previewState() = TopupUiState(
    wallet = WalletUiState(balanceMmk = 500_000),
    operators = OperatorCode.entries.map { OperatorUiModel(it, it.fallbackDisplayName, logoUrl = null) },
)
