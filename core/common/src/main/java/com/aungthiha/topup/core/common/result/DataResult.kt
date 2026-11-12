package com.aungthiha.topup.core.common.result

/**
 * Single result wrapper for repository and use-case outcomes.
 *
 * [Idle] and [Loading] are lifecycle states for flows that model a request; suspend functions
 * return only [Success] or [Error].
 */
sealed interface DataResult<out D, out E : DataError> {
    data object Idle : DataResult<Nothing, Nothing>

    data object Loading : DataResult<Nothing, Nothing>

    data class Success<out D>(val data: D) : DataResult<D, Nothing>

    data class Error<out E : DataError>(val error: E) : DataResult<Nothing, E>
}

inline fun <D, E : DataError, R> DataResult<D, E>.map(transform: (D) -> R): DataResult<R, E> =
    when (this) {
        is DataResult.Success -> DataResult.Success(transform(data))
        is DataResult.Error -> this
        DataResult.Idle -> DataResult.Idle
        DataResult.Loading -> DataResult.Loading
    }

inline fun <D, E : DataError> DataResult<D, E>.onSuccess(action: (D) -> Unit): DataResult<D, E> {
    if (this is DataResult.Success) action(data)
    return this
}

inline fun <D, E : DataError> DataResult<D, E>.onError(action: (E) -> Unit): DataResult<D, E> {
    if (this is DataResult.Error) action(error)
    return this
}

fun <D, E : DataError> DataResult<D, E>.asEmptyResult(): DataResult<Unit, E> = map { }
