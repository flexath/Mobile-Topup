package com.aungthiha.topup.feature.topup.domain.repository

import com.aungthiha.topup.core.common.result.DataResult
import com.aungthiha.topup.feature.topup.domain.model.RechargeError
import com.aungthiha.topup.feature.topup.domain.model.RechargeReceipt
import com.aungthiha.topup.feature.topup.domain.model.RechargeRequest

interface RechargeRepository {
    suspend fun submit(request: RechargeRequest): DataResult<RechargeReceipt, RechargeError>
}
