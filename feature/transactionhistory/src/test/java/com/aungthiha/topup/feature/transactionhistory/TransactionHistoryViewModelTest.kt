package com.aungthiha.topup.feature.transactionhistory

import androidx.lifecycle.SavedStateHandle
import com.aungthiha.topup.core.common.network.NetworkMonitor
import com.aungthiha.topup.core.common.result.DataError
import com.aungthiha.topup.core.common.result.DataResult
import com.aungthiha.topup.core.common.result.NetworkError
import com.aungthiha.topup.feature.transactionhistory.presentation.screens.history.TransactionHistoryViewModel
import com.aungthiha.topup.shared.transaction.domain.model.PurchasedProduct
import com.aungthiha.topup.shared.transaction.domain.model.RechargeProductType
import com.aungthiha.topup.shared.transaction.domain.model.Transaction
import com.aungthiha.topup.shared.transaction.domain.model.TransactionOperator
import com.aungthiha.topup.shared.transaction.domain.model.TransactionQuery
import com.aungthiha.topup.shared.transaction.domain.model.TransactionSortOrder
import com.aungthiha.topup.shared.transaction.domain.model.TransactionStatus
import com.aungthiha.topup.shared.transaction.domain.repository.TransactionPageSync
import com.aungthiha.topup.shared.transaction.domain.repository.TransactionRepository
import com.aungthiha.topup.shared.transaction.domain.usecase.ObserveTransactionCountUseCase
import com.aungthiha.topup.shared.transaction.domain.usecase.ObserveTransactionHistoryUseCase
import com.aungthiha.topup.shared.transaction.domain.usecase.SyncTransactionPageUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionHistoryViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val repository = FakeTransactionRepository()
    private val network = object : NetworkMonitor {
        override val isOnline: StateFlow<Boolean> = MutableStateFlow(true)
    }

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun TestScope.viewModel(): TransactionHistoryViewModel {
        val vm = TransactionHistoryViewModel(
            savedStateHandle = SavedStateHandle(),
            observeHistory = ObserveTransactionHistoryUseCase(repository),
            observeCount = ObserveTransactionCountUseCase(repository),
            syncPage = SyncTransactionPageUseCase(repository),
            networkMonitor = network,
            clock = { 0L },
        )
        backgroundScope.launch(dispatcher) { vm.uiState.collect { } }
        return vm
    }

    @Test
    fun `shows the first 20 cached rows and syncs page 0 on open`() = runTest(dispatcher) {
        repository.remotePages = mapOf(0 to TransactionPageSync(20, hasMore = true))
        repository.cached.value = (1..25).map(::transaction)

        val vm = viewModel()

        assertEquals(20, vm.uiState.value.transactions.size)
        assertEquals(listOf(0), repository.syncedPages)
        assertTrue(vm.uiState.value.canLoadMore)
    }

    @Test
    fun `load more shows the next 20 and syncs the next page`() = runTest(dispatcher) {
        repository.remotePages = mapOf(0 to TransactionPageSync(20, true), 1 to TransactionPageSync(5, false))
        repository.cached.value = (1..25).map(::transaction)
        val vm = viewModel()

        vm.onLoadMore()

        assertEquals(25, vm.uiState.value.transactions.size)
        assertEquals(listOf(0, 1), repository.syncedPages)
        assertFalse(vm.uiState.value.canLoadMore)
    }

    @Test
    fun `load more is hidden when the server reports no more rows and all cached rows are shown`() = runTest(dispatcher) {
        repository.remotePages = mapOf(0 to TransactionPageSync(3, false))
        repository.cached.value = (1..3).map(::transaction)

        val vm = viewModel()

        assertFalse(vm.uiState.value.canLoadMore)
    }

    @Test
    fun `changing filters resets paging and re-syncs the filtered first page`() = runTest(dispatcher) {
        repository.remotePages = mapOf(0 to TransactionPageSync(20, true), 1 to TransactionPageSync(20, true))
        repository.cached.value = (1..45).map(::transaction)
        val vm = viewModel()
        vm.onLoadMore()

        vm.onOperatorToggle("ATOM")
        vm.onSortToggle()
        advanceTimeBy(1_000)

        assertEquals(20, vm.uiState.value.transactions.size)
        val lastQuery = repository.syncQueries.last()
        assertEquals(setOf("ATOM"), lastQuery.operatorCodes)
        assertEquals(TransactionSortOrder.OLDEST_FIRST, lastQuery.sortOrder)
        assertEquals(0, repository.syncedPages.last())
    }

    @Test
    fun `sync failure keeps cached rows and shows a warning`() = runTest(dispatcher) {
        repository.failSync = true
        repository.cached.value = (1..2).map(::transaction)

        val vm = viewModel()

        assertEquals(2, vm.uiState.value.transactions.size)
        assertTrue(vm.uiState.value.showSyncWarning)
    }

    private fun transaction(index: Int) = Transaction(
        id = "tx-$index",
        phoneNumber = "0942000${index.toString().padStart(4, '0')}",
        operator = TransactionOperator("MPT", "MPT", null),
        product = PurchasedProduct(null, null, RechargeProductType.DENOMINATION, "Airtime top-up", null, null, emptyList(), null),
        amountMmk = 1_000,
        currency = "MMK",
        status = TransactionStatus.SUCCESS,
        balanceAfterMmk = 0,
        createdAtMillis = index.toLong(),
    )

    private class FakeTransactionRepository : TransactionRepository {
        val cached = MutableStateFlow<List<Transaction>>(emptyList())
        var remotePages: Map<Int, TransactionPageSync> = emptyMap()
        var failSync = false
        val syncedPages = mutableListOf<Int>()
        val syncQueries = mutableListOf<TransactionQuery>()

        override fun observeTransactions(query: TransactionQuery, limit: Int): Flow<List<Transaction>> =
            cached.map { it.take(limit) }

        override fun observeTransactionCount(query: TransactionQuery): Flow<Int> = cached.map { it.size }

        override fun observeTransaction(transactionId: String): Flow<Transaction?> =
            cached.map { list -> list.firstOrNull { it.id == transactionId } }

        override suspend fun syncPage(query: TransactionQuery, pageIndex: Int): DataResult<TransactionPageSync, DataError> {
            syncedPages += pageIndex
            syncQueries += query
            if (failSync) return DataResult.Error(NetworkError.NO_CONNECTION)
            return DataResult.Success(remotePages[pageIndex] ?: TransactionPageSync(0, false))
        }

        override suspend fun saveConfirmedTransaction(transaction: Transaction) = Unit
        override suspend fun clearCache() = Unit
    }
}
