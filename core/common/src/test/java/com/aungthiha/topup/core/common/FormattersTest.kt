package com.aungthiha.topup.core.common

import com.aungthiha.topup.core.common.format.formatDataAllowance
import com.aungthiha.topup.core.common.format.formatKyat
import com.aungthiha.topup.core.common.format.packageHighlights
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class FormattersTest {

    @Test
    fun `formats kyat with grouping`() {
        val previous = Locale.getDefault()
        try {
            Locale.setDefault(Locale.US)
            assertEquals("500,000 Ks", formatKyat(500_000))
            assertEquals("700 Ks", formatKyat(700))
        } finally {
            Locale.setDefault(previous)
        }
    }

    @Test
    fun `formats data allowances`() {
        assertEquals("600 MB", formatDataAllowance(600))
        assertEquals("5 GB", formatDataAllowance(5_120))
        assertEquals("1.5 GB", formatDataAllowance(1_536))
    }

    @Test
    fun `package highlights only include provided fields in display order`() {
        assertEquals(
            listOf("YouTube + TikTok", "5 GB", "50 Minutes", "30 Days"),
            packageHighlights(listOf("YouTube", "TikTok"), 5_120, 50, 30),
        )
        assertEquals(listOf("Daily Data", "600 MB", "1 Day"), packageHighlights(emptyList(), 600, null, 1, "Daily Data"))
        assertEquals(emptyList<String>(), packageHighlights(emptyList(), null, null, null))
    }
}
