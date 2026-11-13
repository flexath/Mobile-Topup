package com.aungthiha.topup.core.network

import com.aungthiha.topup.core.common.result.NetworkError
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.serialization.SerializationException
import java.io.IOException

private const val HTTP_UNAUTHORIZED = 401
private const val HTTP_FORBIDDEN = 403
private const val HTTP_NOT_FOUND = 404
private const val HTTP_SERVER_ERROR = 500

/** Maps Supabase / Ktor failures to [NetworkError]. Never call with a CancellationException. */
fun Throwable.toNetworkError(): NetworkError = when (this) {
    is HttpRequestTimeoutException -> NetworkError.TIMEOUT
    is HttpRequestException -> NetworkError.NO_CONNECTION
    is RestException -> when {
        statusCode == HTTP_UNAUTHORIZED || statusCode == HTTP_FORBIDDEN -> NetworkError.UNAUTHORIZED
        statusCode == HTTP_NOT_FOUND -> NetworkError.NOT_FOUND
        statusCode >= HTTP_SERVER_ERROR -> NetworkError.SERVER
        else -> NetworkError.UNKNOWN
    }
    is SerializationException, is IllegalArgumentException -> NetworkError.SERIALIZATION
    is IOException -> NetworkError.NO_CONNECTION
    else -> NetworkError.UNKNOWN
}
