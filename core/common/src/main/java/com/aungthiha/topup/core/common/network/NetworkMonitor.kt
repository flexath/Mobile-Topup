package com.aungthiha.topup.core.common.network

import kotlinx.coroutines.flow.StateFlow

/** Observes whether the device currently has a validated internet connection. */
interface NetworkMonitor {
    val isOnline: StateFlow<Boolean>
}
