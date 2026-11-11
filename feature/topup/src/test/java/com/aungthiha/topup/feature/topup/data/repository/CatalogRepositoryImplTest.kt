package com.aungthiha.topup.feature.topup.data.repository

import com.aungthiha.topup.core.common.coroutines.DispatcherProvider
import com.aungthiha.topup.core.common.result.NetworkError
import com.aungthiha.topup.feature.topup.data.FakeCatalogDao
import com.aungthiha.topup.feature.topup.data.FakeStorage
import com.aungthiha.topup.feature.topup.data.FakeTopupRemoteService
import com.aungthiha.topup.feature.topup.data.operatorDto
import com.aungthiha.topup.feature.topup.domain.model.CatalogSyncStatus
import com.aungthiha.topup.feature.topup.domain.model.OperatorCode
import io.github.jan.supabase.exceptions.HttpRequestException
import io.ktor.client.request.HttpRequestBuilder
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogRepositoryImplTest {

    private val dispatcher = StandardTestDispatcher()
    private val dao = FakeCatalogDao()
    private val remote = FakeTopupRemoteService()

    private fun TestScope.newRepository() = CatalogRepositoryImpl(
        dao = dao,
        remote = remote,
        storage = FakeStorage(),
        appScope = backgroundScope,
        dispatchers = DispatcherProvider(io = dispatcher, default = dispatcher, main = dispatcher),
    )

    @Test
    fun `launch refresh runs once per process and maps the unified catalog`() = runTest(dispatcher) {
        val repository = newRepository()
        remote.catalogResponses += { listOf(operatorDto("MPT", listOf(1_000, 2_000)), operatorDto("ATOM", listOf(1_000), 2)) }

        repository.startLaunchRefresh()
        repository.startLaunchRefresh()
        runCurrent()

        assertEquals(1, remote.catalogCalls)
        assertEquals(CatalogSyncStatus.Synced, repository.syncStatus.value)
        val catalog = repository.observeCatalog().first()
        assertEquals(listOf(OperatorCode.MPT, OperatorCode.ATOM), catalog.map { it.operator.code })
        assertEquals(listOf(1_000L, 2_000L), catalog.first().denominations.map { it.priceMmk })
        assertEquals("https://storage.test/telecom/mpt.jpeg", catalog.first().operator.logoUrl)
    }

    @Test
    fun `a superseded slower response never overwrites the latest catalog`() = runTest(dispatcher) {
        val repository = newRepository()
        val slowFirst = CompletableDeferred<Unit>()
        remote.catalogResponses += {
            slowFirst.await()
            listOf(operatorDto("MPT", listOf(9_999)))
        }
        remote.catalogResponses += { listOf(operatorDto("MPT", listOf(1_000))) }

        repository.startLaunchRefresh()
        runCurrent()
        repository.retryRefresh()
        runCurrent()
        slowFirst.complete(Unit)
        runCurrent()

        assertEquals(1, dao.replaceCount)
        assertEquals(listOf(1_000L), repository.observeCatalog().first().single().denominations.map { it.priceMmk })
        assertEquals(CatalogSyncStatus.Synced, repository.syncStatus.value)
    }

    @Test
    fun `failed refresh keeps the cached catalog and reports the error`() = runTest(dispatcher) {
        val repository = newRepository()
        remote.catalogResponses += { listOf(operatorDto("MPT", listOf(1_000))) }
        remote.catalogResponses += { throw HttpRequestException("offline", HttpRequestBuilder()) }

        repository.startLaunchRefresh()
        runCurrent()
        repository.retryRefresh()
        runCurrent()

        assertEquals(CatalogSyncStatus.Failed(NetworkError.NO_CONNECTION), repository.syncStatus.value)
        assertEquals(1, repository.observeCatalog().first().size)
    }

    @Test
    fun `an empty or invalid response is rejected without touching the cache`() = runTest(dispatcher) {
        val repository = newRepository()
        remote.catalogResponses += { listOf(operatorDto("MPT", listOf(1_000))) }
        remote.catalogResponses += { emptyList() }
        remote.catalogResponses += { listOf(operatorDto("UNKNOWN", listOf(1_000))) }

        repository.startLaunchRefresh()
        runCurrent()
        repository.retryRefresh()
        runCurrent()
        assertTrue(repository.syncStatus.value is CatalogSyncStatus.Failed)
        repository.retryRefresh()
        runCurrent()

        assertEquals(1, dao.replaceCount)
        assertEquals(OperatorCode.MPT, repository.observeCatalog().first().single().operator.code)
    }
}
