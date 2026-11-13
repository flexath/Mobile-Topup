package com.aungthiha.topup.feature.topup.domain.eligibility

import com.aungthiha.topup.feature.topup.TestCatalog
import com.aungthiha.topup.feature.topup.domain.amount.CustomAmountValidation
import com.aungthiha.topup.feature.topup.domain.model.OperatorCode
import com.aungthiha.topup.feature.topup.domain.model.RechargeBlocker
import com.aungthiha.topup.feature.topup.domain.model.RechargeSelection
import com.aungthiha.topup.feature.topup.domain.phone.PhoneNumberStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RechargeEligibilityTest {

    private val mpt = PhoneNumberStatus.Detected("09420000001", OperatorCode.MPT)
    private val mptCatalog = TestCatalog.operatorCatalog(OperatorCode.MPT)
    private val mptDenomination = RechargeSelection.Product(mptCatalog.denominations.first())

    @Test
    fun `eligible when number, operator catalog, selection and network are valid`() {
        assertNull(RechargeEligibility.blocker(mpt, mptCatalog, mptDenomination, null, isOnline = true))
        assertNull(RechargeEligibility.blocker(mpt, mptCatalog, RechargeSelection.CustomAmount(1_500), null, isOnline = true))
    }

    @Test
    fun `number problems block first`() {
        assertEquals(
            RechargeBlocker.INVALID_NUMBER,
            RechargeEligibility.blocker(PhoneNumberStatus.Incomplete("0942"), mptCatalog, mptDenomination, null, true),
        )
        assertEquals(
            RechargeBlocker.UNSUPPORTED_OPERATOR,
            RechargeEligibility.blocker(PhoneNumberStatus.Unsupported("09311234567"), mptCatalog, mptDenomination, null, true),
        )
    }

    @Test
    fun `requires the detected operator's catalog`() {
        assertEquals(RechargeBlocker.CATALOG_UNAVAILABLE, RechargeEligibility.blocker(mpt, null, null, null, true))
        val atomCatalog = TestCatalog.operatorCatalog(OperatorCode.ATOM)
        assertEquals(RechargeBlocker.CATALOG_UNAVAILABLE, RechargeEligibility.blocker(mpt, atomCatalog, null, null, true))
    }

    @Test
    fun `requires a valid selection that belongs to the operator`() {
        assertEquals(RechargeBlocker.NO_SELECTION, RechargeEligibility.blocker(mpt, mptCatalog, null, null, true))
        assertEquals(
            RechargeBlocker.INVALID_CUSTOM_AMOUNT,
            RechargeEligibility.blocker(mpt, mptCatalog, null, CustomAmountValidation.InvalidStep(100), true),
        )
        val atomProduct = RechargeSelection.Product(TestCatalog.denomination(OperatorCode.ATOM, 1_000))
        assertEquals(RechargeBlocker.NO_SELECTION, RechargeEligibility.blocker(mpt, mptCatalog, atomProduct, null, true))
    }

    @Test
    fun `offline blocks an otherwise valid recharge`() {
        assertEquals(RechargeBlocker.OFFLINE, RechargeEligibility.blocker(mpt, mptCatalog, mptDenomination, null, isOnline = false))
    }
}
