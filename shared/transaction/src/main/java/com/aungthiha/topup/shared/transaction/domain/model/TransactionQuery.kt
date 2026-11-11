package com.aungthiha.topup.shared.transaction.domain.model

enum class TransactionSortOrder { NEWEST_FIRST, OLDEST_FIRST }

/** Inclusive start, exclusive end, both epoch millis. */
data class DateRange(
    val startMillis: Long,
    val endMillisExclusive: Long,
) {
    init {
        require(endMillisExclusive > startMillis) { "Date range end must be after its start." }
    }
}

/**
 * Combined history search / filter / sort. Applied identically to the Room cache and to the
 * server query, so pagination of a filtered view never needs the full history locally.
 */
data class TransactionQuery(
    val search: String = "",
    val operatorCodes: Set<String> = emptySet(),
    val dateRange: DateRange? = null,
    val sortOrder: TransactionSortOrder = TransactionSortOrder.NEWEST_FIRST,
) {
    /** Lower-cased, trimmed search restricted to characters that can appear in phones / UUIDs. */
    val normalizedSearch: String = normalizeSearch(search)

    val hasFilters: Boolean
        get() = normalizedSearch.isNotEmpty() || operatorCodes.isNotEmpty() || dateRange != null

    companion object {
        const val PAGE_SIZE = 20

        fun normalizeSearch(raw: String): String = raw.trim()
            .lowercase()
            .filter { it.isDigit() || it in 'a'..'f' || it == '-' }
    }
}
