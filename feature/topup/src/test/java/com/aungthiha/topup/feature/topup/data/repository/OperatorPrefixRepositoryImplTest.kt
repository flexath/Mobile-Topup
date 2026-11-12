package com.aungthiha.topup.feature.topup.data.repository

import com.aungthiha.topup.core.common.coroutines.DispatcherProvider
import com.aungthiha.topup.feature.topup.data.FakeOperatorPrefixDao
import com.aungthiha.topup.feature.topup.data.FakeTopupRemoteService
import com.aungthiha.topup.feature.topup.data.local.entity.OperatorPrefixEntity
import com.aungthiha.topup.feature.topup.data.remote.response.OperatorPrefixDto
import com.aungthiha.topup.feature.topup.domain.model.OperatorCode
import com.aungthiha.topup.feature.topup.domain.phone.OperatorPrefix
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
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OperatorPrefixRepositoryImplTest {

    private val dispatcher = StandardTestDispatcher()
    private val dao = FakeOperatorPrefixDao()
    private val remote = FakeTopupRemoteService()

    private fun TestScope.newRepository() = OperatorPrefixRepositoryImpl(
        dao = dao,
        remote = remote,
        appScope = backgroundScope,
        dispatchers = DispatcherProvider(io = dispatcher, default = dispatcher, main = dispatcher),
    )

    @Test
    fun `launch refresh runs once per process and writes mapped prefixes`() = runTest(dispatcher) {
        val repository = newRepository()
        remote.prefixResponses += {
            listOf(
                OperatorPrefixDto("0942", "MPT"),
                OperatorPrefixDto("0977", "ATOM"),
                OperatorPrefixDto("0999", "UNKNOWN"),
            )
        }

        repository.startLaunchRefresh()
        repository.startLaunchRefresh()
        runCurrent()

        assertEquals(1, remote.prefixCalls)
        assertEquals(1, dao.replaceCount)
        assertEquals(
            listOf(
                OperatorPrefix("0942", OperatorCode.MPT),
                OperatorPrefix("0977", OperatorCode.ATOM),
            ),
            repository.observePrefixes().first(),
        )
    }

    @Test
    fun `failed refresh keeps the cached prefixes`() = runTest(dispatcher) {
        val repository = newRepository()
        remote.prefixResponses += { listOf(OperatorPrefixDto("0942", "MPT")) }
        remote.prefixResponses += { throw HttpRequestException("offline", HttpRequestBuilder()) }

        repository.startLaunchRefresh()
        runCurrent()
        repository.retryRefresh()
        runCurrent()

        assertEquals(1, dao.replaceCount)
        assertEquals(listOf(OperatorPrefix("0942", OperatorCode.MPT)), repository.observePrefixes().first())
    }

    @Test
    fun `empty or invalid response does not clear the cache`() = runTest(dispatcher) {
        val repository = newRepository()
        remote.prefixResponses += { listOf(OperatorPrefixDto("0942", "MPT")) }
        remote.prefixResponses += { emptyList() }
        remote.prefixResponses += { listOf(OperatorPrefixDto("bad", "MPT"), OperatorPrefixDto("0999", "UNKNOWN")) }

        repository.startLaunchRefresh()
        runCurrent()
        repository.retryRefresh()
        runCurrent()
        repository.retryRefresh()
        runCurrent()

        assertEquals(1, dao.replaceCount)
        assertEquals(listOf(OperatorPrefix("0942", OperatorCode.MPT)), repository.observePrefixes().first())
    }

    @Test
    fun `identical payload skips a second Room write`() = runTest(dispatcher) {
        val repository = newRepository()
        val payload = listOf(OperatorPrefixDto("0942", "MPT"), OperatorPrefixDto("0977", "ATOM"))
        remote.prefixResponses += { payload }
        remote.prefixResponses += { payload }

        repository.startLaunchRefresh()
        runCurrent()
        repository.retryRefresh()
        runCurrent()

        assertEquals(2, remote.prefixCalls)
        assertEquals(1, dao.replaceCount)
    }

    @Test
    fun `a superseded slower response never overwrites the latest prefixes`() = runTest(dispatcher) {
        val repository = newRepository()
        val slowFirst = CompletableDeferred<Unit>()
        remote.prefixResponses += {
            slowFirst.await()
            listOf(OperatorPrefixDto("0942", "MPT"))
        }
        remote.prefixResponses += { listOf(OperatorPrefixDto("0977", "ATOM")) }

        repository.startLaunchRefresh()
        runCurrent()
        repository.retryRefresh()
        runCurrent()
        slowFirst.complete(Unit)
        runCurrent()

        assertEquals(1, dao.replaceCount)
        assertEquals(
            listOf(OperatorPrefixEntity("0977", "ATOM")),
            dao.getAll(),
        )
    }
}
