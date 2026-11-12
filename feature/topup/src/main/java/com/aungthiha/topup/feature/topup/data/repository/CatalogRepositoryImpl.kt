package com.aungthiha.topup.feature.topup.data.repository

import com.aungthiha.topup.core.common.coroutines.DispatcherProvider
import com.aungthiha.topup.core.common.result.NetworkError
import com.aungthiha.topup.core.network.StorageUrlResolver
import com.aungthiha.topup.core.network.toNetworkError
import com.aungthiha.topup.feature.topup.data.mapper.toDomain
import com.aungthiha.topup.feature.topup.data.remote.response.OperatorDto
import com.aungthiha.topup.feature.topup.data.remote.service.TopupRemoteService
import com.aungthiha.topup.feature.topup.domain.cache.CatalogCache
import com.aungthiha.topup.feature.topup.domain.model.CatalogSyncStatus
import com.aungthiha.topup.feature.topup.domain.model.OperatorCatalog
import com.aungthiha.topup.feature.topup.domain.model.OperatorCode
import com.aungthiha.topup.feature.topup.domain.repository.CatalogRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * Catalog cache coordinator.
 *
 * - The cold-launch refresh runs once per process in [appScope], so screens, rotation and
 *   operator changes never trigger extra requests.
 * - Each request takes a generation number; only the newest generation may write to cache, so a
 *   slow, superseded response can never overwrite newer data.
 * - Cache is only replaced by a valid, non-empty response; failures keep the cached catalog.
 */
internal class CatalogRepositoryImpl(
    private val cache: CatalogCache,
    private val remote: TopupRemoteService,
    private val storage: StorageUrlResolver,
    private val appScope: CoroutineScope,
    private val dispatchers: DispatcherProvider,
) : CatalogRepository {

    private val launchRefreshStarted = AtomicBoolean(false)
    private val latestGeneration = AtomicLong(0)
    private val writeMutex = Mutex()
    private val _syncStatus = MutableStateFlow<CatalogSyncStatus>(CatalogSyncStatus.Idle)

    override val syncStatus: StateFlow<CatalogSyncStatus> = _syncStatus.asStateFlow()

    override fun observeCatalog(): Flow<List<OperatorCatalog>> =
        cache.observeCatalog().flowOn(dispatchers.io)

    override fun startLaunchRefresh() {
        if (launchRefreshStarted.compareAndSet(false, true)) launchRefresh()
    }

    override fun retryRefresh() {
        launchRefreshStarted.set(true)
        launchRefresh()
    }

    private fun launchRefresh() {
        val generation = latestGeneration.incrementAndGet()
        _syncStatus.value = CatalogSyncStatus.Syncing
        appScope.launch(dispatchers.io) { refresh(generation) }
    }

    internal suspend fun refresh(generation: Long) {
        val outcome = try {
            val catalog = remote.fetchCatalog()
            if (!catalog.isValid()) {
                CatalogSyncStatus.Failed(NetworkError.SERIALIZATION)
            } else {
                val mapped = catalog.mapNotNull { it.toDomain(storage) }
                if (mapped.isEmpty()) {
                    CatalogSyncStatus.Failed(NetworkError.SERIALIZATION)
                } else {
                    writeMutex.withLock {
                        if (generation == latestGeneration.get()) {
                            cache.replaceCatalog(mapped)
                        }
                    }
                    CatalogSyncStatus.Synced
                }
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            CatalogSyncStatus.Failed(throwable.toNetworkError())
        }
        if (generation == latestGeneration.get()) _syncStatus.value = outcome
    }

    private fun List<OperatorDto>.isValid(): Boolean =
        isNotEmpty() &&
            all { operator -> OperatorCode.fromCode(operator.code) != null } &&
            all { operator -> operator.products.all { it.priceMmk > 0 && it.operatorCode == operator.code } }
}
