package com.aungthiha.topup.core.network

/** Builds public Supabase Storage URLs for files referenced by catalog and transaction rows. */
interface StorageUrlResolver {
    fun publicUrl(bucket: String, path: String): String
}
