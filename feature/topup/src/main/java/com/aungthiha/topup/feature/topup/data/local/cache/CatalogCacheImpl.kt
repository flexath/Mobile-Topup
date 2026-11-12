package com.aungthiha.topup.feature.topup.data.local.cache

import com.aungthiha.topup.feature.topup.data.local.dao.CatalogDao
import com.aungthiha.topup.feature.topup.data.mapper.toDomain
import com.aungthiha.topup.feature.topup.data.mapper.toEntities
import com.aungthiha.topup.feature.topup.domain.cache.CatalogCache
import com.aungthiha.topup.feature.topup.domain.model.OperatorCatalog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class CatalogCacheImpl(
    private val dao: CatalogDao,
) : CatalogCache {

    override fun observeCatalog(): Flow<List<OperatorCatalog>> =
        dao.observeCatalog().map { rows -> rows.mapNotNull { it.toDomain() } }

    override suspend fun replaceCatalog(catalog: List<OperatorCatalog>) {
        val entities = catalog.map { it.toEntities() }
        dao.replaceCatalog(
            operators = entities.map { it.first },
            products = entities.flatMap { it.second },
        )
    }
}
