package com.aungthiha.topup.core.network.di

import com.aungthiha.topup.core.network.StorageUrlResolver
import com.aungthiha.topup.core.network.SupabaseConfig
import com.aungthiha.topup.core.network.SupabaseStorageUrlResolver
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.logging.LogLevel
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.storage.Storage
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import kotlin.time.Duration.Companion.seconds

/**
 * Single [SupabaseClient] for the process. The Auth plugin persists the session on device and
 * refreshes it automatically, so a signed-in user stays signed in across app restarts.
 */
fun networkModule(config: SupabaseConfig) = module {
    single<SupabaseClient> {
        createSupabaseClient(supabaseUrl = config.url, supabaseKey = config.clientKey) {
            // WARNING keeps request headers (which carry access tokens) out of logcat.
            defaultLogLevel = LogLevel.WARNING
            defaultSerializer = KotlinXSerializer(
                Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                },
            )
            requestTimeout = 20.seconds
            install(Auth)
            install(Postgrest)
            install(Functions)
            install(Storage)
        }
    }
    single<StorageUrlResolver> { SupabaseStorageUrlResolver(get()) }
}
