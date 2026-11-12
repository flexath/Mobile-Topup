package com.aungthiha.topup.core.common.session

/**
 * Clears locally cached data that belongs to a signed-in user (wallet cache, transaction history).
 *
 * Each module that owns user-scoped storage contributes one implementation to the DI graph;
 * sign-out runs all of them. Shared, non-personal caches (the telecom catalog) are not cleared.
 */
fun interface UserScopedDataCleaner {
    suspend fun clearUserData()
}
