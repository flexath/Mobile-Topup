package com.aungthiha.topup.feature.topup.domain.repository

import com.aungthiha.topup.feature.topup.domain.phone.OperatorPrefix
import kotlinx.coroutines.flow.Flow

/** Room-backed mobile prefixes. Supabase refreshes them in the background. */
interface OperatorPrefixRepository {
    /** Cached prefixes only (empty until the first successful sync). */
    fun observePrefixes(): Flow<List<OperatorPrefix>>

    /** Starts the cold-launch refresh once per process; later calls are no-ops. */
    fun startLaunchRefresh()

    /** Explicit retry. Superseded requests never write. */
    fun retryRefresh()
}
