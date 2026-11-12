package com.aungthiha.topup.feature.topup.domain.phone

import com.aungthiha.topup.feature.topup.domain.model.OperatorCode

enum class InvalidPhoneReason {
    /** Contains characters other than digits and common separators. */
    INVALID_CHARACTERS,

    /** Does not start with the Myanmar local mobile prefix `09`. */
    NOT_LOCAL_MOBILE_FORMAT,

    /** Longer than the `09xxxxxxxxx` format allows. */
    TOO_LONG,
}

sealed interface PhoneNumberStatus {
    val normalized: String

    data object Empty : PhoneNumberStatus {
        override val normalized: String = ""
    }

    /** Still typing a number that can become valid. */
    data class Incomplete(override val normalized: String) : PhoneNumberStatus

    data class Invalid(override val normalized: String, val reason: InvalidPhoneReason) : PhoneNumberStatus

    /** Valid `09xxxxxxxxx` number whose prefix is not in the supported dataset. */
    data class Unsupported(override val normalized: String) : PhoneNumberStatus

    data class Detected(override val normalized: String, val operator: OperatorCode) : PhoneNumberStatus
}

object MyanmarPhoneNumber {

    /** `09` followed by nine digits — the local format specified by the PRD. */
    const val LOCAL_LENGTH = 11
    const val LOCAL_PREFIX = "09"

    private const val MYANMAR_DIGIT_ZERO = '\u1040'
    private const val MYANMAR_DIGIT_NINE = '\u1049'
    private val SEPARATORS = setOf(' ', '-', '(', ')', '.', '\u00A0')

    /**
     * Converts user input to local digits: drops separators, maps Myanmar digits (၀–၉) to ASCII,
     * and rewrites the international forms `+959…` / `00959…` / `959…` to `09…`.
     * Unknown characters are kept so validation can report them.
     */
    fun normalize(raw: String): String {
        val compact = buildString(raw.length) {
            raw.trim().forEach { char ->
                when {
                    char in SEPARATORS -> Unit
                    char in MYANMAR_DIGIT_ZERO..MYANMAR_DIGIT_NINE -> append('0' + (char - MYANMAR_DIGIT_ZERO))
                    else -> append(char)
                }
            }
        }
        return when {
            compact.startsWith("+959") -> "0" + compact.removePrefix("+95")
            compact.startsWith("00959") -> "0" + compact.removePrefix("0095")
            compact.startsWith("959") && compact.length > LOCAL_LENGTH - 2 -> "0" + compact.removePrefix("95")
            else -> compact
        }
    }

    fun classify(raw: String, prefixes: OperatorPrefixTable = OperatorPrefixTable.Default): PhoneNumberStatus {
        val number = normalize(raw)
        if (number.isEmpty()) return PhoneNumberStatus.Empty
        if (!number.all(Char::isDigit)) return PhoneNumberStatus.Invalid(number, InvalidPhoneReason.INVALID_CHARACTERS)
        if (!LOCAL_PREFIX.startsWith(number.take(LOCAL_PREFIX.length))) {
            return PhoneNumberStatus.Invalid(number, InvalidPhoneReason.NOT_LOCAL_MOBILE_FORMAT)
        }
        if (number.length > LOCAL_LENGTH) return PhoneNumberStatus.Invalid(number, InvalidPhoneReason.TOO_LONG)
        if (number.length < LOCAL_LENGTH) return PhoneNumberStatus.Incomplete(number)

        val operator = prefixes.match(number) ?: return PhoneNumberStatus.Unsupported(number)
        return PhoneNumberStatus.Detected(number, operator)
    }
}
