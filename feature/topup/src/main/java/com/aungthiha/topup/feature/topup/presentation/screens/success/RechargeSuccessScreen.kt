package com.aungthiha.topup.feature.topup.presentation.screens.success

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.aungthiha.topup.core.common.format.formatKyat
import com.aungthiha.topup.core.common.share.ImageSharer
import com.aungthiha.topup.core.designsystem.components.AppAnimatedCheckmark
import com.aungthiha.topup.core.designsystem.components.AppCard
import com.aungthiha.topup.core.designsystem.components.AppPrimaryButton
import com.aungthiha.topup.core.designsystem.components.AppSecondaryButton
import com.aungthiha.topup.core.designsystem.icons.AppIcons
import com.aungthiha.topup.core.designsystem.theme.appColorScheme
import com.aungthiha.topup.core.designsystem.theme.appSpacing
import com.aungthiha.topup.core.designsystem.theme.appTypography
import com.aungthiha.topup.core.designsystem.theme.deviceType
import com.aungthiha.topup.core.designsystem.utils.CaptureController
import com.aungthiha.topup.core.designsystem.utils.DeviceWidthClass
import com.aungthiha.topup.core.designsystem.utils.rememberCaptureController
import com.aungthiha.topup.feature.topup.R
import com.aungthiha.topup.feature.topup.presentation.components.TransactionReceipt
import com.aungthiha.topup.feature.topup.presentation.navigation.TopupNavCallbacks
import com.aungthiha.topup.feature.topup.presentation.navigation.TopupRoutes
import com.aungthiha.topup.shared.transaction.domain.model.Transaction
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.rechargeSuccessScreen(
    modifier: Modifier,
    onNavRoute: (TopupNavCallbacks) -> Unit,
) {
    composable<TopupRoutes.RechargeSuccess> {
        val viewModel: RechargeSuccessViewModel = koinViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        val imageSharer: ImageSharer = koinInject()
        val captureController = rememberCaptureController()
        val scope = rememberCoroutineScope()
        val chooserTitle = stringResource(R.string.topup_share_chooser)
        var isSharing by remember { mutableStateOf(false) }

        BackHandler { onNavRoute(TopupNavCallbacks.SuccessScreen.BackToTopup) }

        RechargeSuccessScreen(
            state = state,
            captureController = captureController,
            isSharing = isSharing,
            onShare = {
                if (!isSharing) {
                    isSharing = true
                    scope.launch {
                        runCatching {
                            imageSharer.share(captureController.capture(), fileNamePrefix = "topup-receipt", chooserTitle = chooserTitle)
                        }
                        isSharing = false
                    }
                }
            },
            onBackToTopup = { onNavRoute(TopupNavCallbacks.SuccessScreen.BackToTopup) },
            onOpenHistory = { onNavRoute(TopupNavCallbacks.SuccessScreen.ToHistory) },
            modifier = modifier,
        )
    }
}

@Composable
fun RechargeSuccessScreen(
    state: RechargeSuccessUiState,
    captureController: CaptureController,
    isSharing: Boolean,
    onShare: () -> Unit,
    onBackToTopup: () -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.appSpacing
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.appColorScheme.background),
        contentAlignment = Alignment.TopCenter,
    ) {
        when (state) {
            RechargeSuccessUiState.Loading -> Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.appColorScheme.primary)
                Text(
                    text = stringResource(R.string.topup_success_loading),
                    style = MaterialTheme.appTypography.bodyMedium,
                    color = MaterialTheme.appColorScheme.contentSecondary,
                    modifier = Modifier.padding(top = spacing.medium),
                )
            }
            is RechargeSuccessUiState.Ready -> {
                val transaction = state.transaction
                if (MaterialTheme.deviceType.widthClass >= DeviceWidthClass.Medium) {
                    Row(
                        modifier = Modifier
                            .widthIn(max = spacing.readableMaxWidth + spacing.illustration)
                            .fillMaxSize()
                            .padding(horizontal = spacing.screenHorizontal),
                        horizontalArrangement = Arrangement.spacedBy(spacing.pane),
                    ) {
                        SuccessColumn(Modifier.weight(1f)) {
                            SuccessHeader(transaction)
                            SuccessActions(isSharing, onShare, onBackToTopup, onOpenHistory)
                        }
                        SuccessColumn(Modifier.weight(1f)) {
                            TransactionReceipt(transaction = transaction, captureController = captureController)
                            RemainingBalanceCard(transaction.balanceAfterMmk)
                        }
                    }
                } else {
                    SuccessColumn(
                        Modifier
                            .widthIn(max = spacing.readableMaxWidth)
                            .padding(horizontal = spacing.screenHorizontal),
                    ) {
                        SuccessHeader(transaction)
                        TransactionReceipt(transaction = transaction, captureController = captureController)
                        RemainingBalanceCard(transaction.balanceAfterMmk)
                        SuccessActions(isSharing, onShare, onBackToTopup, onOpenHistory)
                    }
                }
            }
        }
    }
}

@Composable
private fun SuccessColumn(modifier: Modifier, content: @Composable ColumnScope.() -> Unit) {
    val spacing = MaterialTheme.appSpacing
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(vertical = spacing.section),
        verticalArrangement = Arrangement.spacedBy(spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content,
    )
}

@Composable
private fun SuccessHeader(transaction: Transaction) {
    val colors = MaterialTheme.appColorScheme
    AppAnimatedCheckmark(contentDescription = stringResource(R.string.topup_success_checkmark))
    Text(
        text = stringResource(R.string.topup_success_title),
        style = MaterialTheme.appTypography.headlineSmall,
        color = colors.contentPrimary,
        textAlign = TextAlign.Center,
    )
    Text(
        text = stringResource(R.string.topup_success_message, transaction.phoneNumber, formatKyat(transaction.amountMmk)),
        style = MaterialTheme.appTypography.bodyMedium,
        color = colors.contentSecondary,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun RemainingBalanceCard(balanceMmk: Long) {
    val colors = MaterialTheme.appColorScheme
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.topup_success_remaining),
                style = MaterialTheme.appTypography.bodyMedium,
                color = colors.contentSecondary,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = formatKyat(balanceMmk),
                style = MaterialTheme.appTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.contentPrimary,
            )
        }
    }
}

@Composable
private fun SuccessActions(
    isSharing: Boolean,
    onShare: () -> Unit,
    onBackToTopup: () -> Unit,
    onOpenHistory: () -> Unit,
) {
    val spacing = MaterialTheme.appSpacing
    Column(verticalArrangement = Arrangement.spacedBy(spacing.medium)) {
        AppPrimaryButton(text = stringResource(R.string.topup_success_back_to_topup), onClick = onBackToTopup)
        AppSecondaryButton(
            text = stringResource(R.string.topup_share_receipt),
            onClick = onShare,
            enabled = !isSharing,
            leadingIcon = AppIcons.Share,
        )
        AppSecondaryButton(
            text = stringResource(R.string.topup_success_open_history),
            onClick = onOpenHistory,
            leadingIcon = AppIcons.History,
        )
    }
}
