package com.aungthiha.topup.shared.transaction.data.repository

import com.aungthiha.topup.core.common.coroutines.DispatcherProvider
import com.aungthiha.topup.core.common.result.DataError
import com.aungthiha.topup.core.common.result.DataResult
import com.aungthiha.topup.core.common.result.LocalError
import com.aungthiha.topup.core.common.session.CurrentUserProvider
import com.aungthiha.topup.core.network.StorageUrlResolver
import com.aungthiha.topup.core.network.toNetworkError
import com.aungthiha.topup.shared.transaction.data.local.TransactionDao
import com.aungthiha.topup.shared.transaction.data.mapper.toDomain
import com.aungthiha.topup.shared.transaction.data.mapper.toEntity
import com.aungthiha.topup.shared.transaction.data.remote.TransactionRemoteService
import com.aungthiha.topup.shared.transaction.domain.model.Transaction
import com.aungthiha.topup.shared.transaction.domain.model.TransactionQuery
import com.aungthiha.topup.shared.transaction.domain.model.TransactionSortOrder
import com.aungthiha.topup.shared.transaction.domain.repository.TransactionPageSync
import com.aungthiha.topup.shared.transaction.domain.repository.TransactionRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
internal class TransactionRepositoryImpl(
    private val dao: TransactionDao,
    private val remote: TransactionRemoteService,
    private val currentUser: CurrentUserProvider,
    private val storage: StorageUrlResolver,
    private val dispatchers: DispatcherProvider,
) : TransactionRepository {

    override fun observeTransactions(query: TransactionQuery, limit: Int): Flow<List<Transaction>> =
        currentUser.userId.flatMapLatest { userId ->
            if (userId == null) return@flatMapLatest flowOf(emptyList())
            val filter = query.toLocalFilter()
            val source = when (query.sortOrder) {
                TransactionSortOrder.NEWEST_FIRST -> dao.observeNewestFirst(
                    userId, filter.search, filter.filterByOperator, filter.operatorCodes,
                    filter.fromMillis, filter.toMillis, limit,
                )
                TransactionSortOrder.OLDEST_FIRST -> dao.observeOldestFirst(
                    userId, filter.search, filter.filterByOperator, filter.operatorCodes,
                    filter.fromMillis, filter.toMillis, limit,
                )
            }
            source.map { entities -> entities.map { it.toDomain() } }
        }.flowOn(dispatchers.io)

    override fun observeTransactionCount(query: TransactionQuery): Flow<Int> =
        currentUser.userId.flatMapLatest { userId ->
            if (userId == null) return@flatMapLatest flowOf(0)
            val filter = query.toLocalFilter()
            dao.observeCount(
                userId, filter.search, filter.filterByOperator, filter.operatorCodes,
                filter.fromMillis, filter.toMillis,
            )
        }.flowOn(dispatchers.io)

    override fun observeTransaction(transactionId: String): Flow<Transaction?> =
        currentUser.userId.flatMapLatest { userId ->
            if (userId == null) flowOf(null) else dao.observeById(userId, transactionId.lowercase()).map { it?.toDomain() }
        }.flowOn(dispatchers.io)

    override suspend fun syncPage(
        query: TransactionQuery,
        pageIndex: Int,
    ): DataResult<TransactionPageSync, DataError> = withContext(dispatchers.io) {
        val userId = currentUser.userId.value ?: return@withContext DataResult.Error(LocalError.NO_ACTIVE_SESSION)
        try {
            val page = remote.fetch(
                query = query,
                limit = TransactionQuery.PAGE_SIZE,
                offset = pageIndex * TransactionQuery.PAGE_SIZE,
            )
            // The account may have changed while the request was in flight.
            if (currentUser.userId.value != userId) {
                return@withContext DataResult.Error(LocalError.NO_ACTIVE_SESSION)
            }
            dao.upsertAll(page.map { it.toEntity(userId, storage) })
            DataResult.Success(
                TransactionPageSync(
                    fetchedCount = page.size,
                    hasMore = page.size == TransactionQuery.PAGE_SIZE,
                ),
            )
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            DataResult.Error(throwable.toNetworkError())
        }
    }

    override suspend fun saveConfirmedTransaction(transaction: Transaction) = withContext(dispatchers.io) {
        val userId = currentUser.userId.value ?: return@withContext
        dao.upsertAll(listOf(transaction.toEntity(userId)))
    }

    override suspend fun clearCache() = withContext(dispatchers.io) { dao.deleteAll() }

    private data class LocalFilter(
        val search: String,
        val filterByOperator: Boolean,
        val operatorCodes: List<String>,
        val fromMillis: Long,
        val toMillis: Long,
    )

    private fun TransactionQuery.toLocalFilter() = LocalFilter(
        search = normalizedSearch,
        filterByOperator = operatorCodes.isNotEmpty(),
        // Room expands an empty list to `IN ()`; a placeholder keeps the SQL unambiguous.
        operatorCodes = operatorCodes.toList().ifEmpty { listOf("") },
        fromMillis = dateRange?.startMillis ?: Long.MIN_VALUE,
        toMillis = dateRange?.endMillisExclusive ?: Long.MAX_VALUE,
    )
}
