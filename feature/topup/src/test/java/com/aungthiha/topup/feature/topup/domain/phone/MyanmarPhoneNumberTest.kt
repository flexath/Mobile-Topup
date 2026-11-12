package com.aungthiha.topup.feature.topup.domain.phone

import com.aungthiha.topup.feature.topup.domain.model.OperatorCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MyanmarPhoneNumberTest {

    private fun detected(number: String) = MyanmarPhoneNumber.classify(number) as? PhoneNumberStatus.Detected

    @Test
    fun `detects every MPT prefix range from the PRD`() {
        val mptPrefixes = (20..25) + (40..45) + listOf(47, 49) + (50..56) + listOf(73) + (83..89) + listOf(91)
        mptPrefixes.forEach { prefix ->
            val number = "09$prefix" + "1234567".take(11 - 4)
            assertEquals("09$prefix", OperatorCode.MPT, detected(number)?.operator)
        }
        (260..268).forEach { prefix ->
            assertEquals("09$prefix", OperatorCode.MPT, detected("09${prefix}123456")?.operator)
        }
    }

    @Test
    fun `detects ATOM Ooredoo and Mytel prefixes`() {
        (75..79).forEach { assertEquals(OperatorCode.ATOM, detected("09${it}1234567")?.operator) }
        (95..97).forEach { assertEquals(OperatorCode.OOREDOO, detected("09${it}1234567")?.operator) }
        (68..69).forEach { assertEquals(OperatorCode.MYTEL, detected("09${it}1234567")?.operator) }
    }

    @Test
    fun `valid number with unknown prefix is unsupported`() {
        assertEquals(PhoneNumberStatus.Unsupported("09311234567"), MyanmarPhoneNumber.classify("09311234567"))
        assertEquals(PhoneNumberStatus.Unsupported("09981234567"), MyanmarPhoneNumber.classify("09981234567"))
    }

    @Test
    fun `0926 only matches its listed three digit sub prefixes`() {
        assertEquals(OperatorCode.MPT, detected("09268123456")?.operator)
        assertEquals(PhoneNumberStatus.Unsupported("09269123456"), MyanmarPhoneNumber.classify("09269123456"))
    }

    @Test
    fun `longest matching prefix wins when prefixes overlap`() {
        val table = OperatorPrefixTable(
            listOf(
                OperatorPrefix("0926", OperatorCode.ATOM),
                OperatorPrefix("09260", OperatorCode.MPT),
            ),
        )
        assertEquals(OperatorCode.MPT, table.match("09260123456"))
        assertEquals(OperatorCode.ATOM, table.match("09261123456"))
        assertNull(table.match("09751234567"))
    }

    @Test
    fun `incomplete numbers are reported while typing`() {
        assertEquals(PhoneNumberStatus.Empty, MyanmarPhoneNumber.classify(""))
        assertEquals(PhoneNumberStatus.Incomplete("0"), MyanmarPhoneNumber.classify("0"))
        assertEquals(PhoneNumberStatus.Incomplete("0977"), MyanmarPhoneNumber.classify("0977"))
        assertEquals(PhoneNumberStatus.Incomplete("0977123456"), MyanmarPhoneNumber.classify("0977123456"))
    }

    @Test
    fun `invalid numbers report the reason`() {
        assertEquals(
            PhoneNumberStatus.Invalid("08123", InvalidPhoneReason.NOT_LOCAL_MOBILE_FORMAT),
            MyanmarPhoneNumber.classify("08123"),
        )
        assertEquals(
            PhoneNumberStatus.Invalid("1", InvalidPhoneReason.NOT_LOCAL_MOBILE_FORMAT),
            MyanmarPhoneNumber.classify("1"),
        )
        assertEquals(
            PhoneNumberStatus.Invalid("097712345678", InvalidPhoneReason.TOO_LONG),
            MyanmarPhoneNumber.classify("097712345678"),
        )
        assertEquals(
            PhoneNumberStatus.Invalid("0977abc", InvalidPhoneReason.INVALID_CHARACTERS),
            MyanmarPhoneNumber.classify("0977abc"),
        )
    }

    @Test
    fun `normalization removes separators and converts international and Myanmar digits`() {
        assertEquals("09771234567", MyanmarPhoneNumber.normalize(" 09 77-123 (4567) "))
        assertEquals("09771234567", MyanmarPhoneNumber.normalize("+959771234567"))
        assertEquals("09771234567", MyanmarPhoneNumber.normalize("00959771234567"))
        assertEquals("09771234567", MyanmarPhoneNumber.normalize("959771234567"))
        assertEquals("09771234567", MyanmarPhoneNumber.normalize("၀၉၇၇၁၂၃၄၅၆၇"))
        assertEquals(OperatorCode.ATOM, detected("+95 9 77 123 4567")?.operator)
    }
}
