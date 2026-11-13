package com.aungthiha.topup.feature.topup.domain.usecase

import com.aungthiha.topup.feature.topup.domain.repository.CatalogRepository

class SyncCatalogUseCase(private val repository: CatalogRepository) {
    /** [retry] = false starts the once-per-launch refresh; true is the explicit Retry action. */
    operator fun invoke(retry: Boolean = false) {
        if (retry) repository.retryRefresh() else repository.startLaunchRefresh()
    }
}
