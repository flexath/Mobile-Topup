package com.aungthiha.topup.feature.topup.domain.amount

import com.aungthiha.topup.feature.topup.domain.model.CustomAmountRule
import org.junit.Assert.assertEquals
import org.junit.Test

class CustomAmountValidatorTest {

    private val rule = CustomAmountRule(minMmk = 1_000, maxMmk = 50_000, stepMmk = 100)

    @Test
    fun `accepts amounts inside the range on the step`() {
        assertEquals(CustomAmountValidation.Valid(1_000), CustomAmountValidator.validate("1000", rule))
        assertEquals(CustomAmountValidation.Valid(1_500), CustomAmountValidator.validate("1,500", rule))
        assertEquals(CustomAmountValidation.Valid(50_000), CustomAmountValidator.validate("50000", rule))
    }

    @Test
    fun `rejects empty, non numeric, out of range and off step amounts`() {
        assertEquals(CustomAmountValidation.Empty, CustomAmountValidator.validate(" ", rule))
        assertEquals(CustomAmountValidation.NotANumber, CustomAmountValidator.validate("12a", rule))
        assertEquals(CustomAmountValidation.NotANumber, CustomAmountValidator.validate("-500", rule))
        assertEquals(CustomAmountValidation.BelowMinimum(1_000), CustomAmountValidator.validate("900", rule))
        assertEquals(CustomAmountValidation.AboveMaximum(50_000), CustomAmountValidator.validate("50100", rule))
        assertEquals(CustomAmountValidation.InvalidStep(100), CustomAmountValidator.validate("1050", rule))
    }

    @Test
    fun `sanitize keeps digits only and caps length`() {
        assertEquals("1500", CustomAmountValidator.sanitize("1,5 00 Ks"))
        assertEquals("123456789", CustomAmountValidator.sanitize("12345678901234"))
    }
}
