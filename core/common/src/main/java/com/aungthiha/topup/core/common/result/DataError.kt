package com.aungthiha.topup.core.common.result

/**
 * Marker for every error carried by [DataResult.Error].
 *
 * Infrastructure failures use [NetworkError] / [LocalError]; product domains declare their own
 * subtypes (for example authentication or recharge errors) in the owning module.
 */
interface DataError

enum class NetworkError : DataError {
    NO_CONNECTION,
    TIMEOUT,
    UNAUTHORIZED,
    NOT_FOUND,
    SERVER,
    SERIALIZATION,
    UNKNOWN,
}

enum class LocalError : DataError {
    NO_ACTIVE_SESSION,
    UNKNOWN,
}
