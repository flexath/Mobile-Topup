package com.aungthiha.topup.feature.auth.domain.model

import com.aungthiha.topup.core.common.result.DataError

enum class AuthError : DataError {
    INVALID_CREDENTIALS,
    EMAIL_NOT_CONFIRMED,
    EMAIL_ALREADY_REGISTERED,
    WEAK_PASSWORD,
    INVALID_EMAIL,
    SIGNUP_DISABLED,
    RATE_LIMITED,
    NO_CONNECTION,
    UNKNOWN,
}
