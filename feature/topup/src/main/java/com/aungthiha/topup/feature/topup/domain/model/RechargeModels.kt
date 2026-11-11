package com.aungthiha.topup.feature.topup.domain.model

import com.aungthiha.topup.core.common.result.DataError
import com.aungthiha.topup.core.common.result.NetworkError
import com.aungthiha.topup.shared.transaction.domain.model.Transaction

enum class RechargeTab { DENOMINATIONS, PACKAGES }

/** The single active recharge choice. A denomination and a package can never be combined. */
sealed interface RechargeSelection {
    val amountMmk: Long

    data class Product(val product: CatalogProduct) : RechargeSelection {
        override val amountMmk: Long get() = product.priceMmk
    }

    data class CustomAmount(override val amountMmk: Long) : RechargeSelection
}

/** First reason, in priority order, why recharge cannot proceed. */
enum class RechargeBlocker {
    INVALID_NUMBER,
    UNSUPPORTED_OPERATOR,
    CATALOG_UNAVAILABLE,
    NO_SELECTION,
    INVALID_CUSTOM_AMOUNT,
    OFFLINE,
}

data class RechargeRequest(
    val phoneNumber: String,
    val operatorCode: OperatorCode,
    val productId: String?,
    val customAmountMmk: Long?,
    /** Reused on retry so a request the backend already processed is never charged twice. */
    val idempotencyKey: String,
)

data class RechargeReceipt(
    val transaction: Transaction,
    val walletBalanceMmk: Long,
    val replayed: Boolean,
)

sealed interface RechargeError : DataError {
    data class InsufficientBalance(val balanceMmk: Long?, val amountMmk: Long?) : RechargeError
    data object InvalidPhoneNumber : RechargeError
    data object UnsupportedOperator : RechargeError
    data object ProductUnavailable : RechargeError
    data object InvalidAmount : RechargeError
    data object DuplicateRequest : RechargeError
    data object SessionExpired : RechargeError
    data object WalletNotFound : RechargeError
    data class Network(val error: NetworkError) : RechargeError
    data object Unknown : RechargeError
}
