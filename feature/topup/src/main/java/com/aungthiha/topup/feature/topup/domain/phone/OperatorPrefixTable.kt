package com.aungthiha.topup.feature.topup.domain.phone

import com.aungthiha.topup.feature.topup.domain.model.OperatorCode

data class OperatorPrefix(
    val prefix: String,
    val operator: OperatorCode,
)

/**
 * Offline prefix → operator lookup. When prefixes overlap, the longest matching prefix wins.
 *
 * [Default] is the PRD's initial common-prefix dataset. It is not an exhaustive or authoritative
 * numbering plan; numbers outside it are reported as unsupported rather than guessed.
 */
class OperatorPrefixTable(prefixes: List<OperatorPrefix>) {

    private val byLengthDescending = prefixes.sortedByDescending { it.prefix.length }

    fun match(localNumber: String): OperatorCode? =
        byLengthDescending.firstOrNull { localNumber.startsWith(it.prefix) }?.operator

    companion object {
        val Default = OperatorPrefixTable(
            buildList {
                // MPT: 09 20–25, 09 260–268, 09 40–45, 09 47, 09 49, 09 50–56, 09 73, 09 83–89, 09 91
                addRange(20..25, OperatorCode.MPT)
                addRange(260..268, OperatorCode.MPT)
                addRange(40..45, OperatorCode.MPT)
                addRange(47..47, OperatorCode.MPT)
                addRange(49..49, OperatorCode.MPT)
                addRange(50..56, OperatorCode.MPT)
                addRange(73..73, OperatorCode.MPT)
                addRange(83..89, OperatorCode.MPT)
                addRange(91..91, OperatorCode.MPT)
                // ATOM: 09 75–79
                addRange(75..79, OperatorCode.ATOM)
                // Ooredoo: 09 95–97
                addRange(95..97, OperatorCode.OOREDOO)
                // Mytel: 09 68–69
                addRange(68..69, OperatorCode.MYTEL)
            },
        )

        private fun MutableList<OperatorPrefix>.addRange(range: IntRange, operator: OperatorCode) {
            range.forEach { add(OperatorPrefix(prefix = "09$it", operator = operator)) }
        }
    }
}
