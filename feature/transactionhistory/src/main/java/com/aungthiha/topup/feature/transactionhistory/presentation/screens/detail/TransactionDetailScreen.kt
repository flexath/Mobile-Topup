package com.aungthiha.topup.feature.transactionhistory.presentation.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.aungthiha.topup.core.common.format.HIGHLIGHT_SEPARATOR
import com.aungthiha.topup.core.common.format.formatDateTime
import com.aungthiha.topup.core.common.format.formatKyat
import com.aungthiha.topup.core.common.format.formatValidity
import com.aungthiha.topup.core.common.format.packageHighlights
import com.aungthiha.topup.core.common.share.ImageSharer
import com.aungthiha.topup.core.designsystem.components.AppCard
import com.aungthiha.topup.core.designsystem.components.AppEmptyState
import com.aungthiha.topup.core.designsystem.components.AppIconButton
import com.aungthiha.topup.core.designsystem.components.AppPrimaryButton
import com.aungthiha.topup.core.designsystem.components.AppTopBar
import com.aungthiha.topup.core.designsystem.components.AppTransactionReceipt
import com.aungthiha.topup.core.designsystem.components.AppTransactionReceiptContent
import com.aungthiha.topup.core.designsystem.icons.AppIcons
import com.aungthiha.topup.core.designsystem.theme.appColorScheme
import com.aungthiha.topup.core.designsystem.theme.appSpacing
import com.aungthiha.topup.core.designsystem.theme.appTypography
import com.aungthiha.topup.core.designsystem.utils.CaptureController
import com.aungthiha.topup.core.designsystem.utils.rememberCaptureController
import com.aungthiha.topup.feature.transactionhistory.R
import com.aungthiha.topup.feature.transactionhistory.presentation.components.packageTitle
import com.aungthiha.topup.feature.transactionhistory.presentation.navigation.TransactionHistoryNavCallbacks
import com.aungthiha.topup.feature.transactionhistory.presentation.navigation.TransactionHistoryRoutes
import com.aungthiha.topup.shared.transaction.domain.model.Transaction
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.transactionDetailScreen(
    modifier: Modifier,
    onNavRoute: (TransactionHistoryNavCallbacks) -> Unit,
) {
    composable<TransactionHistoryRoutes.TransactionDetailScreen> {
        val viewModel: TransactionDetailViewModel = koinViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        val imageSharer: ImageSharer = koinInject()
        val captureController = rememberCaptureController()
        val scope = rememberCoroutineScope()
        val chooserTitle = stringResource(R.string.history_share_chooser)
        var isSharing by remember { mutableStateOf(false) }
        val share: () -> Unit = {
            if (!isSharing && state is TransactionDetailUiState.Ready) {
                isSharing = true
                scope.launch {
                    runCatching {
                        imageSharer.share(captureController.capture(), fileNamePrefix = "topup-receipt", chooserTitle = chooserTitle)
                    }
                    isSharing = false
                }
            }
        }

        TransactionDetailScreen(
            state = state,
            captureController = captureController,
            isSharing = isSharing,
            onShare = share,
            onNavigateUp = { onNavRoute(TransactionHistoryNavCallbacks.NavigateUp) },
            modifier = modifier,
        )
    }
}

@Composable
fun TransactionDetailScreen(
    state: TransactionDetailUiState,
    captureController: CaptureController,
    isSharing: Boolean,
    onShare: () -> Unit,
    onNavigateUp: () -> Unit,
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
            title = stringResource(R.string.history_detail_title),
            onNavigateUp = onNavigateUp,
            navigateUpContentDescription = stringResource(R.string.history_back),
            actions = {
                if (state is TransactionDetailUiState.Ready) {
                    AppIconButton(
                        icon = AppIcons.Share,
                        contentDescription = stringResource(R.string.history_detail_share),
                        onClick = onShare,
                        enabled = !isSharing,
                    )
                }
            },
        )
        when (state) {
            TransactionDetailUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.appColorScheme.primary)
            }
            TransactionDetailUiState.NotFound -> AppEmptyState(
                icon = AppIcons.Receipt,
                title = stringResource(R.string.history_detail_not_found_title),
                message = stringResource(R.string.history_detail_not_found_message),
                modifier = Modifier.padding(horizontal = spacing.screenHorizontal),
            )
            is TransactionDetailUiState.Ready -> Column(
                modifier = Modifier
                    .widthIn(max = spacing.readableMaxWidth)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = spacing.screenHorizontal, vertical = spacing.large),
                verticalArrangement = Arrangement.spacedBy(spacing.large),
            ) {
                AppTransactionReceipt(
                    content = state.transaction.toReceiptContent(),
                    captureController = captureController,
                )
                BalanceAfterCard(state.transaction.balanceAfterMmk)
                AppPrimaryButton(
                    text = stringResource(R.string.history_detail_share),
                    onClick = onShare,
                    loading = isSharing,
                    leadingIcon = AppIcons.Share,
                )
            }
        }
    }
}

@Composable
private fun BalanceAfterCard(balanceMmk: Long) {
    val colors = MaterialTheme.appColorScheme
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.history_detail_balance_after),
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
private fun Transaction.toReceiptContent() = AppTransactionReceiptContent(
    transactionId = id,
    dateTime = formatDateTime(createdAtMillis),
    phoneNumber = phoneNumber,
    operatorName = operator.name,
    operatorLogoUrl = operator.logoUrl,
    packageName = packageTitle(),
    packageDetails = packageHighlights(
        services = product.services,
        dataAllowanceMb = product.dataAllowanceMb,
        callMinutes = product.callMinutes,
        validityDays = null,
    ).joinToString(HIGHLIGHT_SEPARATOR).ifEmpty { null },
    validity = product.validityDays?.let(::formatValidity),
    amount = formatKyat(amountMmk),
)
