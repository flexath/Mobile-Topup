package com.aungthiha.topup.core.network

/** Public client configuration (project URL + anon/publishable key). Never a secret key. */
data class SupabaseConfig(
    val url: String,
    val clientKey: String,
)
