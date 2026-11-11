package com.aungthiha.topup.core.network

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage

/** Builds public Supabase Storage URLs for files referenced by catalog and transaction rows. */
interface StorageUrlResolver {
    fun publicUrl(bucket: String, path: String): String
}

internal class SupabaseStorageUrlResolver(
    private val supabase: SupabaseClient,
) : StorageUrlResolver {
    override fun publicUrl(bucket: String, path: String): String =
        supabase.storage.from(bucket).publicUrl(path)
}
