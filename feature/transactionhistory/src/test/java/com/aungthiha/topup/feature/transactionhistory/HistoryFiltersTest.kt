package com.aungthiha.topup.feature.transactionhistory

import com.aungthiha.topup.feature.transactionhistory.presentation.model.DatePickerRange
import com.aungthiha.topup.feature.transactionhistory.presentation.model.HistoryFilterState
import com.aungthiha.topup.shared.transaction.domain.model.DateRange
import com.aungthiha.topup.shared.transaction.domain.model.TransactionSortOrder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.TimeZone

class HistoryFiltersTest {

    private val yangon = TimeZone.getTimeZone("Asia/Yangon")

    // 2026-11-11T00:00:00Z and 2026-11-13T00:00:00Z as returned by the Material date picker.
    private val nov11Utc = 1_794_355_200_000L
    private val nov13Utc = nov11Utc + 2 * DAY

    @Test
    fun `picker days become whole local days with an exclusive end`() {
        val range = DatePickerRange.toLocalDayRange(nov11Utc, nov13Utc, yangon)

        // Yangon is UTC+06:30: local midnight of 11 Nov is 10 Nov 17:30 UTC.
        assertEquals(DateRange(nov11Utc - SIX_THIRTY, nov13Utc + DAY - SIX_THIRTY), range)
    }

    @Test
    fun `single day selection covers that day only`() {
        val range = DatePickerRange.toLocalDayRange(nov11Utc, null, TimeZone.getTimeZone("UTC"))

        assertEquals(DateRange(nov11Utc, nov11Utc + DAY), range)
    }

    @Test
    fun `no start date means no date filter`() {
        assertNull(DatePickerRange.toLocalDayRange(null, nov13Utc, yangon))
    }

    @Test
    fun `filter state maps to a combined transaction query`() {
        val state = HistoryFilterState(
            search = " 0977 ",
            operatorCodes = setOf("ATOM", "MYTEL"),
            selectedStartUtcMillis = nov11Utc,
            selectedEndUtcMillis = nov11Utc,
            sortOrder = TransactionSortOrder.OLDEST_FIRST,
        )

        val query = state.toQuery(TimeZone.getTimeZone("UTC"))

        assertEquals("0977", query.normalizedSearch)
        assertEquals(setOf("ATOM", "MYTEL"), query.operatorCodes)
        assertEquals(DateRange(nov11Utc, nov11Utc + DAY), query.dateRange)
        assertEquals(TransactionSortOrder.OLDEST_FIRST, query.sortOrder)
        assertEquals(true, state.hasFilters)
        assertEquals(false, HistoryFilterState(sortOrder = TransactionSortOrder.OLDEST_FIRST).hasFilters)
    }

    private companion object {
        const val DAY = 24L * 60 * 60 * 1000
        const val SIX_THIRTY = (6 * 60 + 30) * 60 * 1000L
    }
}
