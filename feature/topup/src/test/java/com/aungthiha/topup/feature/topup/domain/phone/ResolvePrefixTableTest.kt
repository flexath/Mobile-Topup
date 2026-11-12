package com.aungthiha.topup.feature.topup.domain.phone

import com.aungthiha.topup.feature.topup.domain.model.OperatorCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class ResolvePrefixTableTest {

    @Test
    fun `empty room prefixes use the fallback table`() {
        val fallback = OperatorPrefixTable.Default
        assertSame(fallback, resolvePrefixTable(emptyList(), fallback))
        assertEquals(OperatorCode.MPT, resolvePrefixTable(emptyList()).match("09421234567"))
    }

    @Test
    fun `non empty room prefixes prefer the room table`() {
        val room = listOf(OperatorPrefix("0999", OperatorCode.MYTEL))
        val table = resolvePrefixTable(room)
        assertEquals(OperatorCode.MYTEL, table.match("09991234567"))
        assertEquals(null, table.match("09421234567"))
    }
}
