package com.aungthiha.topup.shared.transaction.data.remote

import com.aungthiha.topup.shared.transaction.domain.model.TransactionQuery

/**
 * Remote gateway for the caller's transaction history.
 * Implementation: [SupabaseTransactionRemoteService].
 */
internal interface TransactionRemoteService {
    /** The caller's own history page, filtered and sorted server-side. */
    suspend fun fetch(query: TransactionQuery, limit: Int, offset: Int): List<TransactionDto>
}
