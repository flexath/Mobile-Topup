package com.aungthiha.topup.feature.topup.domain.model

/** Operators supported by this app. [code] matches `public.operators.code` on the backend. */
enum class OperatorCode(val code: String, val fallbackDisplayName: String) {
    MPT("MPT", "MPT"),
    ATOM("ATOM", "ATOM"),
    OOREDOO("OOREDOO", "Ooredoo"),
    MYTEL("MYTEL", "Mytel"),
    ;

    companion object {
        fun fromCode(code: String): OperatorCode? = entries.firstOrNull { it.code == code }
    }
}
