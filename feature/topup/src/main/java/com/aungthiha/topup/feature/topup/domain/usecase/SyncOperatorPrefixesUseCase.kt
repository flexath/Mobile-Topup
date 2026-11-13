package com.aungthiha.topup.feature.topup.domain.usecase

import com.aungthiha.topup.feature.topup.domain.repository.OperatorPrefixRepository

class SyncOperatorPrefixesUseCase(private val repository: OperatorPrefixRepository) {
    /** [retry] = false starts the once-per-launch refresh; true forces another attempt. */
    operator fun invoke(retry: Boolean = false) {
        if (retry) repository.retryRefresh() else repository.startLaunchRefresh()
    }
}
