package com.aungthiha.topup.feature.topup.domain.usecase

import com.aungthiha.topup.feature.topup.domain.model.CatalogSyncStatus
import com.aungthiha.topup.feature.topup.domain.repository.CatalogRepository
import kotlinx.coroutines.flow.StateFlow

class ObserveCatalogSyncStatusUseCase(private val repository: CatalogRepository) {
    operator fun invoke(): StateFlow<CatalogSyncStatus> = repository.syncStatus
}
