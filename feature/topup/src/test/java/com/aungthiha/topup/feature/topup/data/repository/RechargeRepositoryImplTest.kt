package com.aungthiha.topup.feature.topup.data.repository

import com.aungthiha.topup.core.common.result.DataResult
import com.aungthiha.topup.core.common.result.NetworkError
import com.aungthiha.topup.feature.topup.data.FakeStorage
import com.aungthiha.topup.feature.topup.data.FakeTopupRemoteService
import com.aungthiha.topup.feature.topup.data.mapper.toRechargeError
import com.aungthiha.topup.feature.topup.data.remote.RechargeFailureBodyDto
import com.aungthiha.topup.feature.topup.data.remote.RechargeRequestDto
import com.aungthiha.topup.feature.topup.domain.model.OperatorCode
import com.aungthiha.topup.feature.topup.domain.model.RechargeError
import com.aungthiha.topup.feature.topup.domain.model.RechargeRequest
import com.aungthiha.topup.shared.transaction.domain.model.RechargeProductType
import io.github.jan.supabase.exceptions.HttpRequestException
import io.ktor.client.request.HttpRequestBuilder
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RechargeRepositoryImplTest {

    private val remote = FakeTopupRemoteService()
    private val repository = RechargeRepositoryImpl(remote, FakeStorage(), Json { ignoreUnknownKeys = true })
    private val request = RechargeRequest(
        phoneNumber = "09420000001",
        operatorCode = OperatorCode.MPT,
        productId = "f60081cc-4c64-4d31-874b-76c9c2510215",
        customAmountMmk = null,
        idempotencyKey = "4f9a6e8e-5b8f-4a4e-9a7e-3c9d1e2f3a4b",
    )

    @Test
    fun `sends ids only and maps the backend transaction snapshot`() = runTest {
        var sent: RechargeRequestDto? = null
        remote.rechargeBody = { body ->
            sent = body
            SUCCESS_BODY
        }

        val result = repository.submit(request)

        assertEquals(
            RechargeRequestDto(
                phoneNumber = "09420000001",
                operatorCode = "MPT",
                productId = request.productId,
                customAmountMmk = null,
                idempotencyKey = request.idempotencyKey,
            ),
            sent,
        )
        val receipt = (result as DataResult.Success).data
        assertEquals(499_000L, receipt.walletBalanceMmk)
        assertEquals("380b7498-6985-43d5-804e-f4298c936f0d", receipt.transaction.id)
        assertEquals(1_000L, receipt.transaction.amountMmk)
        assertEquals(RechargeProductType.DENOMINATION, receipt.transaction.product.type)
        assertEquals("https://storage.test/telecom/mpt.jpeg", receipt.transaction.operator.logoUrl)
        assertEquals(1_794_384_481_646L, receipt.transaction.createdAtMillis)
    }

    @Test
    fun `network failures are reported as network errors`() = runTest {
        remote.rechargeBody = { throw HttpRequestException("offline", HttpRequestBuilder()) }

        val result = repository.submit(request)

        assertEquals(DataResult.Error(RechargeError.Network(NetworkError.NO_CONNECTION)), result)
    }

    @Test
    fun `backend error codes map to recharge errors`() {
        assertEquals(
            RechargeError.InsufficientBalance(balanceMmk = 19_000, amountMmk = 30_000),
            RechargeFailureBodyDto("INSUFFICIENT_BALANCE", walletBalanceMmk = 19_000, amountMmk = 30_000).toRechargeError(),
        )
        assertEquals(RechargeError.SessionExpired, RechargeFailureBodyDto("UNAUTHORIZED").toRechargeError())
        assertEquals(RechargeError.ProductUnavailable, RechargeFailureBodyDto("PRODUCT_NOT_AVAILABLE").toRechargeError())
        assertEquals(RechargeError.UnsupportedOperator, RechargeFailureBodyDto("OPERATOR_MISMATCH").toRechargeError())
        assertEquals(RechargeError.DuplicateRequest, RechargeFailureBodyDto("IDEMPOTENCY_KEY_REUSED").toRechargeError())
        assertTrue(RechargeFailureBodyDto("SOMETHING_NEW").toRechargeError() is RechargeError.Unknown)
    }

    private companion object {
        const val SUCCESS_BODY = """
            {"success":true,"replayed":false,
             "transaction":{"id":"380B7498-6985-43d5-804e-f4298c936f0d","status":"SUCCESS","currency":"MMK",
               "services":[],"amount_mmk":1000,"created_at":"2026-11-11T08:08:01.646111+00:00",
               "product_id":"f60081cc-4c64-4d31-874b-76c9c2510215","product_sku":"MPT-AIRTIME-1000",
               "call_minutes":null,"phone_number":"09420000001","product_name":"Airtime top-up",
               "product_type":"DENOMINATION","operator_code":"MPT","operator_name":"MPT","validity_days":null,
               "balance_after_mmk":499000,"data_allowance_mb":null,"operator_logo_path":"mpt.jpeg",
               "operator_logo_bucket":"telecom"},
             "wallet":{"balance_mmk":499000,"currency":"MMK"}}
        """
    }
}
