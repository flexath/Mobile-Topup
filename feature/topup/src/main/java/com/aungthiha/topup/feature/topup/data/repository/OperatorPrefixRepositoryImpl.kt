package com.aungthiha.topup.feature.topup.data.repository

import com.aungthiha.topup.core.common.coroutines.DispatcherProvider
import com.aungthiha.topup.feature.topup.data.mapper.toDomainOrNull
import com.aungthiha.topup.feature.topup.data.remote.service.TopupRemoteService
import com.aungthiha.topup.feature.topup.domain.cache.OperatorPrefixCache
import com.aungthiha.topup.feature.topup.domain.phone.OperatorPrefix
import com.aungthiha.topup.feature.topup.domain.repository.OperatorPrefixRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * Prefix cache coordinator.
 *
 * - Cold-launch refresh runs once per process in [appScope].
 * - Only the newest generation may write to cache.
 * - Cache is only replaced by a valid, non-empty mapped response; failures keep the cache.
 */
internal class OperatorPrefixRepositoryImpl(
    private val cache: OperatorPrefixCache,
    private val remote: TopupRemoteService,
    private val appScope: CoroutineScope,
    private val dispatchers: DispatcherProvider,
) : OperatorPrefixRepository {

    private val launchRefreshStarted = AtomicBoolean(false)
    private val latestGeneration = AtomicLong(0)
    private val writeMutex = Mutex()

    override fun observePrefixes(): Flow<List<OperatorPrefix>> =
        cache.observePrefixes().flowOn(dispatchers.io)

    override fun startLaunchRefresh() {
        if (launchRefreshStarted.compareAndSet(false, true)) launchRefresh()
    }

    override fun retryRefresh() {
        launchRefreshStarted.set(true)
        launchRefresh()
    }

    private fun launchRefresh() {
        val generation = latestGeneration.incrementAndGet()
        appScope.launch(dispatchers.io) { refresh(generation) }
    }

    internal suspend fun refresh(generation: Long) {
        try {
            val mapped = remote.fetchOperatorPrefixes().mapNotNull { it.toDomainOrNull() }
            if (mapped.isEmpty()) return
            writeMutex.withLock {
                if (generation != latestGeneration.get()) return
                if (mapped.sortedBy { it.prefix } == cache.getPrefixes()) return
                cache.replacePrefixes(mapped)
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Throwable) {
            // Keep previously cached prefixes; detection falls back to OperatorPrefixTable if empty.
        }
    }
}
