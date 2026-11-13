package com.aungthiha.topup.feature.topup.domain.usecase

import com.aungthiha.topup.feature.topup.domain.model.OperatorCatalog
import com.aungthiha.topup.feature.topup.domain.repository.CatalogRepository
import kotlinx.coroutines.flow.Flow

class ObserveCatalogUseCase(private val repository: CatalogRepository) {
    operator fun invoke(): Flow<List<OperatorCatalog>> = repository.observeCatalog()
}
