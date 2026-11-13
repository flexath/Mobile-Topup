package com.aungthiha.topup.core.datastore.model

/** Last wallet balance confirmed by the backend for one user. */
data class CachedWalletBalance(
    val balanceMmk: Long,
    val syncedAtMillis: Long,
)
