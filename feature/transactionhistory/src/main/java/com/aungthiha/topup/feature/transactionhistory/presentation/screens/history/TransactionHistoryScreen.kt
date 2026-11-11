package com.aungthiha.topup.feature.transactionhistory.presentation.screens.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.aungthiha.topup.core.designsystem.components.AppBanner
import com.aungthiha.topup.core.designsystem.components.AppBannerTone
import com.aungthiha.topup.core.designsystem.components.AppEmptyState
import com.aungthiha.topup.core.designsystem.components.AppSecondaryButton
import com.aungthiha.topup.core.designsystem.components.AppTopBar
import com.aungthiha.topup.core.designsystem.icons.AppIcons
import com.aungthiha.topup.core.designsystem.theme.AppTheme
import com.aungthiha.topup.core.designsystem.theme.appColorScheme
import com.aungthiha.topup.core.designsystem.theme.appSpacing
import com.aungthiha.topup.core.designsystem.theme.appTypography
import com.aungthiha.topup.feature.transactionhistory.R
import com.aungthiha.topup.feature.transactionhistory.presentation.components.HistoryDateRangeDialog
import com.aungthiha.topup.feature.transactionhistory.presentation.components.HistoryFilterRow
import com.aungthiha.topup.feature.transactionhistory.presentation.components.HistorySearchField
import com.aungthiha.topup.feature.transactionhistory.presentation.components.TransactionRow
import com.aungthiha.topup.feature.transactionhistory.presentation.navigation.TransactionHistoryNavCallbacks
import com.aungthiha.topup.feature.transactionhistory.presentation.navigation.TransactionHistoryRoutes
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.transactionHistoryScreen(
    modifier: Modifier,
    onNavRoute: (TransactionHistoryNavCallbacks) -> Unit,
) {
    composable<TransactionHistoryRoutes.TransactionHistoryScreen> {
        val viewModel: TransactionHistoryViewModel = koinViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()

        LifecycleStartEffect(viewModel) {
            viewModel.onScreenVisible()
            onStopOrDispose { }
        }

        TransactionHistoryScreen(
            state = state,
            actions = TransactionHistoryActions(
                onSearchChange = viewModel::onSearchChange,
                onClearSearch = viewModel::onClearSearch,
                onOperatorToggle = viewModel::onOperatorToggle,
                onDateRangeSelected = viewModel::onDateRangeSelected,
                onSortToggle = viewModel::onSortToggle,
                onClearFilters = viewModel::onClearFilters,
                onLoadMore = viewModel::onLoadMore,
                onTransactionClick = { onNavRoute(TransactionHistoryNavCallbacks.TransactionHistoryScreen.ToDetail(it)) },
                onMakeTopup = { onNavRoute(TransactionHistoryNavCallbacks.TransactionHistoryScreen.ToTopup) },
            ),
            modifier = modifier,
        )
    }
}

data class TransactionHistoryActions(
    val onSearchChange: (String) -> Unit = {},
    val onClearSearch: () -> Unit = {},
    val onOperatorToggle: (String) -> Unit = {},
    val onDateRangeSelected: (Long?, Long?) -> Unit = { _, _ -> },
    val onSortToggle: () -> Unit = {},
    val onClearFilters: () -> Unit = {},
    val onLoadMore: () -> Unit = {},
    val onTransactionClick: (String) -> Unit = {},
    val onMakeTopup: () -> Unit = {},
)

@Composable
fun TransactionHistoryScreen(
    state: TransactionHistoryUiState,
    actions: TransactionHistoryActions,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.appSpacing
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.appColorScheme.background)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppTopBar(title = stringResource(R.string.history_title))
        Column(
            modifier = Modifier
                .widthIn(max = spacing.readableMaxWidth)
                .fillMaxWidth()
                .weight(1f),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = spacing.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(spacing.small),
            ) {
                HistorySearchField(
                    value = state.filters.search,
                    onValueChange = actions.onSearchChange,
                    onClear = actions.onClearSearch,
                )
                HistoryFilterRow(
                    filters = state.filters,
                    onDateClick = { showDatePicker = true },
                    onOperatorToggle = actions.onOperatorToggle,
                    onSortToggle = actions.onSortToggle,
                    onClearFilters = actions.onClearFilters,
                )
                AnimatedVisibility(visible = state.showSyncWarning) {
                    AppBanner(
                        message = stringResource(if (state.isOnline) R.string.history_sync_failed else R.string.history_offline),
                        tone = AppBannerTone.Warning,
                    )
                }
            }
            AnimatedVisibility(visible = state.isSyncing && state.transactions.isNotEmpty()) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.screenHorizontal, vertical = spacing.extraSmall),
                    color = MaterialTheme.appColorScheme.primary,
                    trackColor = MaterialTheme.appColorScheme.surfaceVariant,
                )
            }
            HistoryContent(state = state, actions = actions, modifier = Modifier.weight(1f))
        }
    }

    if (showDatePicker) {
        HistoryDateRangeDialog(
            initialStartUtcMillis = state.filters.selectedStartUtcMillis,
            initialEndUtcMillis = state.filters.selectedEndUtcMillis,
            onApply = { start, end ->
                actions.onDateRangeSelected(start, end)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
        )
    }
}

@Composable
private fun HistoryContent(
    state: TransactionHistoryUiState,
    actions: TransactionHistoryActions,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.appSpacing
    val listState = rememberLazyListState()
    val firstTransactionId = state.transactions.firstOrNull()?.id
    LaunchedEffect(state.filters) { listState.scrollToItem(0) }
    LaunchedEffect(firstTransactionId) {
        // Newer rows synced above the viewport: keep the list pinned to the top if it was there.
        if (listState.firstVisibleItemIndex <= 1) listState.animateScrollToItem(0)
    }
    when {
        state.transactions.isEmpty() && (state.isSyncing || state.isInitialLoad) -> Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = MaterialTheme.appColorScheme.primary)
                Text(
                    text = stringResource(R.string.history_syncing),
                    style = MaterialTheme.appTypography.bodyMedium,
                    color = MaterialTheme.appColorScheme.contentSecondary,
                    modifier = Modifier.padding(top = spacing.medium),
                )
            }
        }
        state.transactions.isEmpty() -> Box(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = spacing.screenHorizontal),
            contentAlignment = Alignment.TopCenter,
        ) {
            if (state.filters.hasFilters) {
                AppEmptyState(
                    icon = AppIcons.Search,
                    title = stringResource(R.string.history_no_results_title),
                    message = stringResource(R.string.history_no_results_message),
                    actionLabel = stringResource(R.string.history_clear_filters),
                    onAction = actions.onClearFilters,
                )
            } else {
                AppEmptyState(
                    icon = AppIcons.Receipt,
                    title = stringResource(R.string.history_empty_title),
                    message = stringResource(R.string.history_empty_message),
                    actionLabel = stringResource(R.string.history_empty_action),
                    onAction = actions.onMakeTopup,
                )
            }
        }
        else -> LazyColumn(
            state = listState,
            modifier = modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(
                start = spacing.screenHorizontal,
                end = spacing.screenHorizontal,
                top = spacing.medium,
                bottom = spacing.bottomNavigationClearance,
            ),
            verticalArrangement = Arrangement.spacedBy(spacing.medium),
        ) {
            items(items = state.transactions, key = { it.id }) { transaction ->
                TransactionRow(
                    transaction = transaction,
                    onClick = { actions.onTransactionClick(transaction.id) },
                    modifier = Modifier.animateItem(),
                )
            }
            item(key = "footer") {
                HistoryFooter(state = state, onLoadMore = actions.onLoadMore, modifier = Modifier.animateItem())
            }
        }
    }
}

@Composable
private fun HistoryFooter(
    state: TransactionHistoryUiState,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.appSpacing
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.small),
    ) {
        when {
            state.isLoadingMore -> Row(
                modifier = Modifier.padding(vertical = spacing.medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(spacing.extraLarge),
                    color = MaterialTheme.appColorScheme.primary,
                    strokeWidth = spacing.extraExtraSmall,
                )
            }
            state.canLoadMore -> AppSecondaryButton(
                text = stringResource(R.string.history_load_more),
                onClick = onLoadMore,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionHistoryEmptyPreview() {
    AppTheme {
        TransactionHistoryScreen(
            state = TransactionHistoryUiState(isInitialLoad = false),
            actions = TransactionHistoryActions(),
        )
    }
}
