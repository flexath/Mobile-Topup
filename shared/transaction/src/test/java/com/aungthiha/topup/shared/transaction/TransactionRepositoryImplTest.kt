package com.aungthiha.topup.shared.transaction

import com.aungthiha.topup.core.common.coroutines.DispatcherProvider
import com.aungthiha.topup.core.common.result.DataResult
import com.aungthiha.topup.core.common.result.LocalError
import com.aungthiha.topup.core.common.result.NetworkError
import com.aungthiha.topup.core.common.session.CurrentUserProvider
import com.aungthiha.topup.core.network.StorageUrlResolver
import com.aungthiha.topup.shared.transaction.data.local.dao.TransactionDao
import com.aungthiha.topup.shared.transaction.data.local.entity.TransactionEntity
import com.aungthiha.topup.shared.transaction.data.remote.response.TransactionDto
import com.aungthiha.topup.shared.transaction.data.remote.service.TransactionRemoteService
import com.aungthiha.topup.shared.transaction.data.repository.TransactionRepositoryImpl
import com.aungthiha.topup.shared.transaction.domain.model.DateRange
import com.aungthiha.topup.shared.transaction.domain.model.RechargeProductType
import com.aungthiha.topup.shared.transaction.domain.model.TransactionQuery
import com.aungthiha.topup.shared.transaction.domain.model.TransactionSortOrder
import com.aungthiha.topup.shared.transaction.domain.repository.TransactionPageSync
import io.github.jan.supabase.exceptions.HttpRequestException
import io.ktor.client.request.HttpRequestBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionRepositoryImplTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val dao = InMemoryTransactionDao()
    private val remote = FakeRemote()
    private val currentUser = FakeUser("user-a")
    private val repository = TransactionRepositoryImpl(
        dao = dao,
        remote = remote,
        currentUser = currentUser,
        storage = object : StorageUrlResolver {
            override fun publicUrl(bucket: String, path: String) = "https://storage.test/$bucket/$path"
        },
        dispatchers = DispatcherProvider(io = dispatcher, default = dispatcher, main = dispatcher),
    )

    @Test
    fun `sync requests the page window and caches rows for the current user`() = runTest(dispatcher) {
        remote.rows = (1..20).map { dto(id = "id-$it", createdAt = it) }
        val query = TransactionQuery(search = "0942", sortOrder = TransactionSortOrder.OLDEST_FIRST)

        val result = repository.syncPage(query, pageIndex = 1)

        assertEquals(DataResult.Success(TransactionPageSync(fetchedCount = 20, hasMore = true)), result)
        assertEquals(Triple(query, 20, 20), remote.lastCall)
        assertTrue(dao.rows.value.all { it.userId == "user-a" })
        assertEquals("https://storage.test/telecom/mpt.jpeg", dao.rows.value.first().operatorLogoUrl)
    }

    @Test
    fun `a short page means there is nothing more on the server`() = runTest(dispatcher) {
        remote.rows = listOf(dto("only"))

        val result = repository.syncPage(TransactionQuery(), 0) as DataResult.Success

        assertEquals(false, result.data.hasMore)
    }

    @Test
    fun `failure keeps the cache untouched`() = runTest(dispatcher) {
        remote.rows = listOf(dto("cached"))
        repository.syncPage(TransactionQuery(), 0)
        remote.error = HttpRequestException("offline", HttpRequestBuilder())

        val result = repository.syncPage(TransactionQuery(), 0)

        assertEquals(DataResult.Error(NetworkError.NO_CONNECTION), result)
        assertEquals(listOf("cached"), dao.rows.value.map { it.id })
    }

    @Test
    fun `cached rows are scoped to the signed-in user`() = runTest(dispatcher) {
        remote.rows = listOf(dto("a-1"))
        repository.syncPage(TransactionQuery(), 0)

        currentUser.state.value = "user-b"
        remote.rows = listOf(dto("b-1"))

        assertEquals(emptyList<Any>(), repository.observeTransactions(TransactionQuery(), 20).first())
        assertEquals(DataResult.Success(TransactionPageSync(1, false)), repository.syncPage(TransactionQuery(), 0))
        assertEquals(listOf("b-1"), repository.observeTransactions(TransactionQuery(), 20).first().map { it.id })
        assertEquals(setOf("user-a", "user-b"), dao.rows.value.map { it.userId }.toSet())
    }

    @Test
    fun `no session means no remote read`() = runTest(dispatcher) {
        currentUser.state.value = null
        remote.error = IllegalStateException("must not be called")

        assertEquals(DataResult.Error(LocalError.NO_ACTIVE_SESSION), repository.syncPage(TransactionQuery(), 0))
    }

    @Test
    fun `entity maps back to the purchase snapshot`() = runTest(dispatcher) {
        remote.rows = listOf(dto("pkg", productType = "DATA_PACKAGE"))
        repository.syncPage(TransactionQuery(), 0)

        val transaction = repository.observeTransaction("PKG").first()!!

        assertEquals(RechargeProductType.DATA_PACKAGE, transaction.product.type)
        assertEquals(listOf("YouTube", "TikTok"), transaction.product.services)
        assertEquals(5_120, transaction.product.dataAllowanceMb)
        assertEquals(30, transaction.product.validityDays)
        assertEquals(480_000L, transaction.balanceAfterMmk)
        assertEquals(1_794_384_481_646L, transaction.createdAtMillis)
    }

    @Test
    fun `query normalization keeps only phone and uuid characters`() {
        assertEquals("0942-ab", TransactionQuery.normalizeSearch("  09 42-AB%_' "))
        assertEquals(false, TransactionQuery().hasFilters)
        assertEquals(true, TransactionQuery(dateRange = DateRange(0, 1)).hasFilters)
    }

    private fun dto(id: String, createdAt: Int = 1, productType: String = "DENOMINATION") = TransactionDto(
        id = id.uppercase(),
        phoneNumber = "09420000001",
        operatorCode = "MPT",
        operatorName = "MPT",
        operatorLogoBucket = "telecom",
        operatorLogoPath = "mpt.jpeg",
        productType = productType,
        productName = "Social Pack",
        dataAllowanceMb = 5_120,
        services = listOf("YouTube", "TikTok"),
        validityDays = 30,
        amountMmk = 20_000,
        currency = "MMK",
        status = "SUCCESS",
        balanceAfterMmk = 480_000,
        createdAt = "2026-11-11T08:08:0${createdAt % 10}.646111+00:00",
    )

    private class FakeRemote : TransactionRemoteService {
        var rows: List<TransactionDto> = emptyList()
        var error: Throwable? = null
        var lastCall: Triple<TransactionQuery, Int, Int>? = null

        override suspend fun fetch(query: TransactionQuery, limit: Int, offset: Int): List<TransactionDto> {
            error?.let { throw it }
            lastCall = Triple(query, limit, offset)
            return rows
        }
    }

    private class FakeUser(initial: String?) : CurrentUserProvider {
        val state = MutableStateFlow(initial)
        override val userId: StateFlow<String?> = state
    }

    /** Minimal DAO semantics for repository tests; SQL behaviour is covered by TransactionDaoTest. */
    private class InMemoryTransactionDao : TransactionDao {
        val rows = MutableStateFlow<List<TransactionEntity>>(emptyList())

        private fun filtered(userId: String) = rows.map { list -> list.filter { it.userId == userId } }

        override fun observeNewestFirst(
            userId: String, search: String, filterByOperator: Boolean, operatorCodes: List<String>,
            fromMillis: Long, toMillis: Long, limit: Int,
        ): Flow<List<TransactionEntity>> = filtered(userId).map { it.sortedByDescending(TransactionEntity::createdAtMillis).take(limit) }

        override fun observeOldestFirst(
            userId: String, search: String, filterByOperator: Boolean, operatorCodes: List<String>,
            fromMillis: Long, toMillis: Long, limit: Int,
        ): Flow<List<TransactionEntity>> = filtered(userId).map { it.sortedBy(TransactionEntity::createdAtMillis).take(limit) }

        override fun observeCount(
            userId: String, search: String, filterByOperator: Boolean, operatorCodes: List<String>,
            fromMillis: Long, toMillis: Long,
        ): Flow<Int> = filtered(userId).map { it.size }

        override fun observeById(userId: String, transactionId: String): Flow<TransactionEntity?> =
            filtered(userId).map { list -> list.firstOrNull { it.id == transactionId } }

        override suspend fun upsertAll(transactions: List<TransactionEntity>) {
            val incoming = transactions.associateBy { it.id }
            rows.value = rows.value.filterNot { it.id in incoming } + transactions
        }

        override suspend fun deleteAll() {
            rows.value = emptyList()
        }
    }
}
