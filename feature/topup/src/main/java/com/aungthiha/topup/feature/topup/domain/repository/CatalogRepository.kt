package com.aungthiha.topup.feature.topup.domain.repository

import com.aungthiha.topup.feature.topup.domain.model.CatalogSyncStatus
import com.aungthiha.topup.feature.topup.domain.model.OperatorCatalog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/** Unified telecom catalog. Room is the source of truth; Supabase refreshes it in the background. */
interface CatalogRepository {
    fun observeCatalog(): Flow<List<OperatorCatalog>>

    val syncStatus: StateFlow<CatalogSyncStatus>

    /** Starts the cold-launch refresh once per process; later calls are no-ops. */
    fun startLaunchRefresh()

    /** Explicit retry (used only when no cached catalog exists). Superseded requests never write. */
    fun retryRefresh()
}
