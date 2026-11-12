package com.aungthiha.topup.feature.topup.data.repository

import com.aungthiha.topup.core.common.result.DataResult
import com.aungthiha.topup.core.common.result.NetworkError
import com.aungthiha.topup.core.network.StorageUrlResolver
import com.aungthiha.topup.core.network.toNetworkError
import com.aungthiha.topup.feature.topup.data.mapper.toReceipt
import com.aungthiha.topup.feature.topup.data.mapper.toRechargeError
import com.aungthiha.topup.feature.topup.data.remote.response.RechargeFailureDto
import com.aungthiha.topup.feature.topup.data.remote.response.RechargeRequestDto
import com.aungthiha.topup.feature.topup.data.remote.response.RechargeSuccessDto
import com.aungthiha.topup.feature.topup.data.remote.service.TopupRemoteService
import com.aungthiha.topup.feature.topup.domain.model.RechargeError
import com.aungthiha.topup.feature.topup.domain.model.RechargeReceipt
import com.aungthiha.topup.feature.topup.domain.model.RechargeRequest
import com.aungthiha.topup.feature.topup.domain.repository.RechargeRepository
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json

/**
 * Sends the recharge to the backend. Cancelling the calling coroutine (Back on the processing
 * screen) aborts the HTTP request, but a request the backend already accepted may still complete;
 * the idempotency key plus the next wallet/history sync reconcile that case.
 */
internal class RechargeRepositoryImpl(
    private val remote: TopupRemoteService,
    private val storage: StorageUrlResolver,
    private val json: Json,
) : RechargeRepository {

    override suspend fun submit(request: RechargeRequest): DataResult<RechargeReceipt, RechargeError> = try {
        val body = remote.processRecharge(
            RechargeRequestDto(
                phoneNumber = request.phoneNumber,
                operatorCode = request.operatorCode.code,
                productId = request.productId,
                customAmountMmk = request.customAmountMmk,
                idempotencyKey = request.idempotencyKey,
            ),
        )
        val response = json.decodeFromString<RechargeSuccessDto>(body)
        if (response.success) {
            DataResult.Success(response.toReceipt(storage))
        } else {
            DataResult.Error(RechargeError.Unknown)
        }
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (rest: RestException) {
        DataResult.Error(rest.toRechargeError())
    } catch (throwable: Throwable) {
        DataResult.Error(RechargeError.Network(throwable.toNetworkError()))
    }

    private fun RestException.toRechargeError(): RechargeError {
        val parsed = runCatching { json.decodeFromString<RechargeFailureDto>(error) }.getOrNull()
        if (parsed != null) return parsed.error.toRechargeError()
        return when {
            statusCode == HTTP_UNAUTHORIZED -> RechargeError.SessionExpired
            statusCode >= HTTP_SERVER_ERROR -> RechargeError.Network(NetworkError.SERVER)
            else -> RechargeError.Unknown
        }
    }

    private companion object {
        const val HTTP_UNAUTHORIZED = 401
        const val HTTP_SERVER_ERROR = 500
    }
}
