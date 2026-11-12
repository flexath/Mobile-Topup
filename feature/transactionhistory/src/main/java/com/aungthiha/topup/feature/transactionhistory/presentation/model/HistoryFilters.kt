package com.aungthiha.topup.feature.transactionhistory.presentation.model

import com.aungthiha.topup.shared.transaction.domain.model.DateRange
import com.aungthiha.topup.shared.transaction.domain.model.TransactionQuery
import com.aungthiha.topup.shared.transaction.domain.model.TransactionSortOrder
import kotlinx.serialization.Serializable
import java.util.Calendar
import java.util.TimeZone

/** Operators offered as history filters; codes match the backend `operators.code`. */
enum class HistoryOperatorFilter(val code: String, val label: String) {
    MPT("MPT", "MPT"),
    ATOM("ATOM", "ATOM"),
    OOREDOO("OOREDOO", "Ooredoo"),
    MYTEL("MYTEL", "Mytel"),
}

/**
 * History filter selection, persisted across rotation / process death.
 * [selectedStartUtcMillis] / [selectedEndUtcMillis] are the raw date-picker values (UTC midnight).
 */
@Serializable
data class HistoryFilterState(
    val search: String = "",
    val operatorCodes: Set<String> = emptySet(),
    val selectedStartUtcMillis: Long? = null,
    val selectedEndUtcMillis: Long? = null,
    val sortOrder: TransactionSortOrder = TransactionSortOrder.NEWEST_FIRST,
) {
    val hasFilters: Boolean
        get() = search.isNotBlank() || operatorCodes.isNotEmpty() || selectedStartUtcMillis != null

    fun toQuery(timeZone: TimeZone = TimeZone.getDefault()) = TransactionQuery(
        search = search,
        operatorCodes = operatorCodes,
        dateRange = DatePickerRange.toLocalDayRange(selectedStartUtcMillis, selectedEndUtcMillis, timeZone),
        sortOrder = sortOrder,
    )
}

object DatePickerRange {
    private const val MILLIS_PER_DAY = 24L * 60 * 60 * 1000

    /**
     * Material date pickers return UTC-midnight millis for the chosen calendar days. Converts the
     * inclusive day range to [start of first day, start of the day after the last day) in the
     * device time zone, so "11 Nov – 11 Nov" covers that whole local day.
     */
    fun toLocalDayRange(startUtcMillis: Long?, endUtcMillis: Long?, timeZone: TimeZone): DateRange? {
        if (startUtcMillis == null) return null
        val endDay = endUtcMillis ?: startUtcMillis
        val start = localMidnight(startUtcMillis, timeZone)
        val endExclusive = localMidnight(endDay + MILLIS_PER_DAY, timeZone)
        return if (endExclusive > start) DateRange(start, endExclusive) else null
    }

    private fun localMidnight(utcDayMillis: Long, timeZone: TimeZone): Long {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcDayMillis }
        return Calendar.getInstance(timeZone).apply {
            clear()
            set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
        }.timeInMillis
    }
}
