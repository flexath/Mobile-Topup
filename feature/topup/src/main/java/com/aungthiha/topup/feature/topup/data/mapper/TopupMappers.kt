package com.aungthiha.topup.feature.topup.data.mapper

import com.aungthiha.topup.core.network.StorageUrlResolver
import com.aungthiha.topup.feature.topup.data.local.CatalogProductEntity
import com.aungthiha.topup.feature.topup.data.local.OperatorEntity
import com.aungthiha.topup.feature.topup.data.local.OperatorWithProducts
import com.aungthiha.topup.feature.topup.data.remote.CatalogProductDto
import com.aungthiha.topup.feature.topup.data.remote.OperatorDto
import com.aungthiha.topup.feature.topup.data.remote.RechargeFailureBodyDto
import com.aungthiha.topup.feature.topup.data.remote.RechargeSuccessDto
import com.aungthiha.topup.feature.topup.data.remote.RechargeTransactionDto
import com.aungthiha.topup.feature.topup.domain.model.CatalogProduct
import com.aungthiha.topup.feature.topup.domain.model.CatalogProductType
import com.aungthiha.topup.feature.topup.domain.model.CustomAmountRule
import com.aungthiha.topup.feature.topup.domain.model.OperatorCatalog
import com.aungthiha.topup.feature.topup.domain.model.OperatorCode
import com.aungthiha.topup.feature.topup.domain.model.RechargeError
import com.aungthiha.topup.feature.topup.domain.model.RechargeReceipt
import com.aungthiha.topup.feature.topup.domain.model.TelecomOperator
import com.aungthiha.topup.shared.transaction.domain.model.PurchasedProduct
import com.aungthiha.topup.shared.transaction.domain.model.RechargeProductType
import com.aungthiha.topup.shared.transaction.domain.model.Transaction
import com.aungthiha.topup.shared.transaction.domain.model.TransactionOperator
import com.aungthiha.topup.shared.transaction.domain.model.TransactionStatus
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

internal fun OperatorDto.toEntity(storage: StorageUrlResolver) = OperatorEntity(
    code = code,
    displayName = displayName,
    fullName = name,
    logoUrl = storage.publicUrl(logoBucket, logoPath),
    customAmountMinMmk = customAmountMinMmk,
    customAmountMaxMmk = customAmountMaxMmk,
    customAmountStepMmk = customAmountStepMmk,
    sortOrder = sortOrder,
)

internal fun CatalogProductDto.toEntity() = CatalogProductEntity(
    id = id.lowercase(),
    sku = sku,
    operatorCode = operatorCode,
    productType = productType,
    name = name,
    priceMmk = priceMmk,
    dataAllowanceMb = dataAllowanceMb,
    callMinutes = callMinutes,
    services = services,
    validityDays = validityDays,
    isDemo = isDemo,
    sortOrder = sortOrder,
)

/** Rows for unknown operators or product types are skipped rather than guessed. */
internal fun OperatorWithProducts.toDomain(): OperatorCatalog? {
    val code = OperatorCode.fromCode(operator.code) ?: return null
    val products = products
        .sortedWith(compareBy({ it.sortOrder }, { it.priceMmk }))
        .mapNotNull { it.toDomain(code) }
    return OperatorCatalog(
        operator = TelecomOperator(
            code = code,
            displayName = operator.displayName,
            fullName = operator.fullName,
            logoUrl = operator.logoUrl,
            customAmountRule = CustomAmountRule(
                minMmk = operator.customAmountMinMmk,
                maxMmk = operator.customAmountMaxMmk,
                stepMmk = operator.customAmountStepMmk,
            ),
        ),
        denominations = products.filter { it.type == CatalogProductType.DENOMINATION },
        packages = products.filter { it.type == CatalogProductType.DATA_PACKAGE },
    )
}

private fun CatalogProductEntity.toDomain(operatorCode: OperatorCode): CatalogProduct? {
    val type = CatalogProductType.entries.firstOrNull { it.name == productType } ?: return null
    return CatalogProduct(
        id = id,
        sku = sku,
        operatorCode = operatorCode,
        type = type,
        name = name,
        priceMmk = priceMmk,
        dataAllowanceMb = dataAllowanceMb,
        callMinutes = callMinutes,
        services = services,
        validityDays = validityDays,
        isDemo = isDemo,
    )
}

@OptIn(ExperimentalTime::class)
internal fun RechargeTransactionDto.toDomain(storage: StorageUrlResolver) = Transaction(
    id = id.lowercase(),
    phoneNumber = phoneNumber,
    operator = TransactionOperator(
        code = operatorCode,
        name = operatorName,
        logoUrl = storage.publicUrl(operatorLogoBucket, operatorLogoPath),
    ),
    product = PurchasedProduct(
        id = productId,
        sku = productSku,
        type = RechargeProductType.entries.firstOrNull { it.name == productType } ?: RechargeProductType.CUSTOM_AMOUNT,
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
    createdAtMillis = Instant.parse(createdAt).toEpochMilliseconds(),
)

internal fun RechargeSuccessDto.toReceipt(storage: StorageUrlResolver) = RechargeReceipt(
    transaction = transaction.toDomain(storage),
    walletBalanceMmk = wallet.balanceMmk,
    replayed = replayed,
)

internal fun RechargeFailureBodyDto.toRechargeError(): RechargeError = when (code) {
    "INSUFFICIENT_BALANCE" -> RechargeError.InsufficientBalance(balanceMmk = walletBalanceMmk, amountMmk = amountMmk)
    "INVALID_PHONE_NUMBER" -> RechargeError.InvalidPhoneNumber
    "UNSUPPORTED_OPERATOR", "OPERATOR_MISMATCH" -> RechargeError.UnsupportedOperator
    "PRODUCT_NOT_AVAILABLE", "PRODUCT_OPERATOR_MISMATCH" -> RechargeError.ProductUnavailable
    "INVALID_AMOUNT" -> RechargeError.InvalidAmount
    "IDEMPOTENCY_KEY_REUSED" -> RechargeError.DuplicateRequest
    "UNAUTHORIZED" -> RechargeError.SessionExpired
    "WALLET_NOT_FOUND" -> RechargeError.WalletNotFound
    else -> RechargeError.Unknown
}
