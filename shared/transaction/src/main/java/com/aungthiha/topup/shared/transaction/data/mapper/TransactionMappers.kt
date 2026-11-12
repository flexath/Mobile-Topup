package com.aungthiha.topup.shared.transaction.data.mapper

import com.aungthiha.topup.core.network.StorageUrlResolver
import com.aungthiha.topup.shared.transaction.data.local.entity.TransactionEntity
import com.aungthiha.topup.shared.transaction.data.remote.response.TransactionDto
import com.aungthiha.topup.shared.transaction.domain.model.PurchasedProduct
import com.aungthiha.topup.shared.transaction.domain.model.RechargeProductType
import com.aungthiha.topup.shared.transaction.domain.model.Transaction
import com.aungthiha.topup.shared.transaction.domain.model.TransactionOperator
import com.aungthiha.topup.shared.transaction.domain.model.TransactionStatus
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
internal fun TransactionDto.toEntity(userId: String, storage: StorageUrlResolver) = TransactionEntity(
    id = id.lowercase(),
    userId = userId,
    phoneNumber = phoneNumber,
    operatorCode = operatorCode,
    operatorName = operatorName,
    operatorLogoUrl = storage.publicUrl(operatorLogoBucket, operatorLogoPath),
    productId = productId,
    productSku = productSku,
    productType = productType,
    productName = productName,
    dataAllowanceMb = dataAllowanceMb,
    callMinutes = callMinutes,
    services = services,
    validityDays = validityDays,
    amountMmk = amountMmk,
    currency = currency,
    status = status,
    balanceAfterMmk = balanceAfterMmk,
    createdAtMillis = Instant.parse(createdAt).toEpochMilliseconds(),
)

internal fun TransactionEntity.toDomain() = Transaction(
    id = id,
    phoneNumber = phoneNumber,
    operator = TransactionOperator(
        code = operatorCode,
        name = operatorName,
        logoUrl = operatorLogoUrl,
    ),
    product = PurchasedProduct(
        id = productId,
        sku = productSku,
        type = productType.toProductType(),
        name = productName,
        dataAllowanceMb = dataAllowanceMb,
        callMinutes = callMinutes,
        services = services,
        validityDays = validityDays,
    ),
    amountMmk = amountMmk,
    currency = currency,
    status = TransactionStatus.SUCCESS,
    balanceAfterMmk = balanceAfterMmk,
    createdAtMillis = createdAtMillis,
)

internal fun Transaction.toEntity(userId: String) = TransactionEntity(
    id = id.lowercase(),
    userId = userId,
    phoneNumber = phoneNumber,
    operatorCode = operator.code,
    operatorName = operator.name,
    operatorLogoUrl = operator.logoUrl,
    productId = product.id,
    productSku = product.sku,
    productType = product.type.name,
    productName = product.name,
    dataAllowanceMb = product.dataAllowanceMb,
    callMinutes = product.callMinutes,
    services = product.services,
    validityDays = product.validityDays,
    amountMmk = amountMmk,
    currency = currency,
    status = status.name,
    balanceAfterMmk = balanceAfterMmk,
    createdAtMillis = createdAtMillis,
)

internal fun String.toProductType(): RechargeProductType =
    RechargeProductType.entries.firstOrNull { it.name == this } ?: RechargeProductType.CUSTOM_AMOUNT
