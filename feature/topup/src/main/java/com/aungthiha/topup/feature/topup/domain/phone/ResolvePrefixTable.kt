package com.aungthiha.topup.feature.topup.domain.phone

/**
 * Room is preferred when it has usable rows; otherwise the bundled PRD table is used so
 * detection keeps working offline before the first successful sync.
 */
fun resolvePrefixTable(
    roomPrefixes: List<OperatorPrefix>,
    fallback: OperatorPrefixTable = OperatorPrefixTable.Default,
): OperatorPrefixTable =
    if (roomPrefixes.isNotEmpty()) OperatorPrefixTable(roomPrefixes) else fallback
