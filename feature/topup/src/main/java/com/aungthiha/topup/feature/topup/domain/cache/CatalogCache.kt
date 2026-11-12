package com.aungthiha.topup.feature.topup.domain.cache

import com.aungthiha.topup.feature.topup.domain.model.OperatorCatalog
import kotlinx.coroutines.flow.Flow

interface CatalogCache {
    fun observeCatalog(): Flow<List<OperatorCatalog>>

    suspend fun replaceCatalog(catalog: List<OperatorCatalog>)
}
