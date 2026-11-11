package com.aungthiha.topup.feature.transactionhistory.presentation.screens.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aungthiha.topup.core.common.network.NetworkMonitor
import com.aungthiha.topup.core.common.result.DataResult
import com.aungthiha.topup.feature.transactionhistory.presentation.model.HistoryFilterState
import com.aungthiha.topup.shared.transaction.domain.model.Transaction
import com.aungthiha.topup.shared.transaction.domain.model.TransactionQuery
import com.aungthiha.topup.shared.transaction.domain.model.TransactionSortOrder
import com.aungthiha.topup.shared.transaction.domain.usecase.ObserveTransactionCountUseCase
import com.aungthiha.topup.shared.transaction.domain.usecase.ObserveTransactionHistoryUseCase
import com.aungthiha.topup.shared.transaction.domain.usecase.SyncTransactionPageUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

data class HistorySyncState(
    val isSyncing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val lastSyncFailed: Boolean = false,
    /** Whether the last remote page for [query] was full (more rows may exist on the server). */
    val remoteHasMore: Boolean = false,
    val query: TransactionQuery? = null,
)

data class TransactionHistoryUiState(
    val filters: HistoryFilterState = HistoryFilterState(),
    val transactions: List<Transaction> = emptyList(),
    val matchingCachedCount: Int = 0,
    val isInitialLoad: Boolean = true,
    val isSyncing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val showSyncWarning: Boolean = false,
    val isOnline: Boolean = true,
    val canLoadMore: Boolean = false,
)

/**
 * Room is the source of truth for the list. Supabase pages are synced in the background with the
 * same filters (never the full history) and upserted into Room.
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class TransactionHistoryViewModel(
    private val savedStateHandle: SavedStateHandle,
    observeHistory: ObserveTransactionHistoryUseCase,
    observeCount: ObserveTransactionCountUseCase,
    private val syncPage: SyncTransactionPageUseCase,
    networkMonitor: NetworkMonitor,
    private val clock: () -> Long = System::currentTimeMillis,
) : ViewModel() {

    private val filters = MutableStateFlow(restoreFilters())
    private val pageCount = MutableStateFlow(savedStateHandle[KEY_PAGES] ?: 1)
    private val syncState = MutableStateFlow(HistorySyncState())
    private var refreshJob: Job? = null
    private var loadMoreJob: Job? = null
    private var lastRefreshAt = 0L

    private val query = filters.map { it.toQuery() }.distinctUntilChanged()

    private val transactions = combine(query, pageCount) { q, pages -> q to pages }
        .flatMapLatest { (q, pages) -> observeHistory(q, pages).map { list -> list to (q to pages) } }

    private val cachedCount = query.flatMapLatest { observeCount(it) }

    val uiState: StateFlow<TransactionHistoryUiState> = combine(
        filters,
        transactions,
        cachedCount,
        syncState,
        networkMonitor.isOnline,
    ) { filterState, (list, source), count, sync, online ->
        val (listQuery, pages) = source
        val pageFull = list.size >= pages * TransactionQuery.PAGE_SIZE
        val remoteMore = sync.query == listQuery && sync.remoteHasMore
        TransactionHistoryUiState(
            filters = filterState,
            transactions = list,
            matchingCachedCount = count,
            isInitialLoad = false,
            isSyncing = sync.isSyncing,
            isLoadingMore = sync.isLoadingMore,
            showSyncWarning = !online || sync.lastSyncFailed,
            isOnline = online,
            canLoadMore = list.size < count || (pageFull && remoteMore),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), TransactionHistoryUiState(filters = filters.value))

    init {
        refreshFirstPage(force = true)
        viewModelScope.launch {
            // Filter edits re-sync page 0 for the new query; typing is debounced.
            query.drop(1).debounce(SEARCH_DEBOUNCE_MILLIS).collect { refreshFirstPage(force = true) }
        }
    }

    fun onScreenVisible() = refreshFirstPage(force = false)

    fun onSearchChange(value: String) = updateFilters { it.copy(search = value.take(MAX_SEARCH_LENGTH)) }

    fun onClearSearch() = updateFilters { it.copy(search = "") }

    fun onOperatorToggle(code: String) = updateFilters { state ->
        val codes = if (code in state.operatorCodes) state.operatorCodes - code else state.operatorCodes + code
        state.copy(operatorCodes = codes)
    }

    fun onDateRangeSelected(startUtcMillis: Long?, endUtcMillis: Long?) = updateFilters {
        it.copy(selectedStartUtcMillis = startUtcMillis, selectedEndUtcMillis = endUtcMillis ?: startUtcMillis)
    }

    fun onSortToggle() = updateFilters {
        it.copy(
            sortOrder = when (it.sortOrder) {
                TransactionSortOrder.NEWEST_FIRST -> TransactionSortOrder.OLDEST_FIRST
                TransactionSortOrder.OLDEST_FIRST -> TransactionSortOrder.NEWEST_FIRST
            },
        )
    }

    fun onClearFilters() = updateFilters { HistoryFilterState(sortOrder = it.sortOrder) }

    fun onLoadMore() {
        val state = uiState.value
        if (!state.canLoadMore || loadMoreJob?.isActive == true) return
        val nextPages = pageCount.updateAndGet { it + 1 }
        savedStateHandle[KEY_PAGES] = nextPages
        val currentQuery = filters.value.toQuery()
        loadMoreJob = viewModelScope.launch {
            syncState.update { it.copy(isLoadingMore = true) }
            val result = syncPage(currentQuery, pageIndex = nextPages - 1)
            syncState.update { current ->
                when (result) {
                    is DataResult.Success -> current.copy(
                        isLoadingMore = false,
                        lastSyncFailed = false,
                        remoteHasMore = result.data.hasMore,
                        query = currentQuery,
                    )
                    else -> current.copy(isLoadingMore = false, lastSyncFailed = true)
                }
            }
        }
    }

    private fun refreshFirstPage(force: Boolean) {
        val now = clock()
        if (!force && now - lastRefreshAt < REFRESH_THROTTLE_MILLIS) return
        lastRefreshAt = now
        refreshJob?.cancel()
        val currentQuery = filters.value.toQuery()
        refreshJob = viewModelScope.launch {
            syncState.update { it.copy(isSyncing = true) }
            val result = syncPage(currentQuery, pageIndex = 0)
            syncState.update { current ->
                when (result) {
                    is DataResult.Success -> current.copy(
                        isSyncing = false,
                        lastSyncFailed = false,
                        remoteHasMore = result.data.hasMore,
                        query = currentQuery,
                    )
                    else -> current.copy(isSyncing = false, lastSyncFailed = true)
                }
            }
        }
    }

    private fun updateFilters(transform: (HistoryFilterState) -> HistoryFilterState) {
        val before = filters.value
        val after = filters.updateAndGet(transform)
        if (after == before) return
        savedStateHandle[KEY_FILTERS] = Json.encodeToString(after)
        pageCount.value = 1
        savedStateHandle[KEY_PAGES] = 1
    }

    private fun restoreFilters(): HistoryFilterState =
        savedStateHandle.get<String>(KEY_FILTERS)
            ?.let { runCatching { Json.decodeFromString<HistoryFilterState>(it) }.getOrNull() }
            ?: HistoryFilterState()

    private companion object {
        const val KEY_FILTERS = "history_filters"
        const val KEY_PAGES = "history_pages"
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val SEARCH_DEBOUNCE_MILLIS = 350L
        const val REFRESH_THROTTLE_MILLIS = 10_000L
        const val MAX_SEARCH_LENGTH = 64
    }
}
