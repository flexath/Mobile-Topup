package com.aungthiha.topup.core.common.di

import com.aungthiha.topup.core.common.coroutines.DispatcherProvider
import com.aungthiha.topup.core.common.network.ConnectivityNetworkMonitor
import com.aungthiha.topup.core.common.network.NetworkMonitor
import com.aungthiha.topup.core.common.share.ImageSharer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Process-wide scope for work that must outlive a screen (session observation, cache sync). */
val ApplicationScope = named("ApplicationScope")

val commonModule = module {
    single { DispatcherProvider() }
    single<CoroutineScope>(ApplicationScope) { CoroutineScope(SupervisorJob() + get<DispatcherProvider>().default) }
    single<NetworkMonitor> { ConnectivityNetworkMonitor(androidContext(), get(ApplicationScope)) }
    factory { ImageSharer(androidContext(), get()) }
}
