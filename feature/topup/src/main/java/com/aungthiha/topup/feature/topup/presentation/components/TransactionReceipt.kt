package com.aungthiha.topup.feature.topup.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.aungthiha.topup.core.common.format.HIGHLIGHT_SEPARATOR
import com.aungthiha.topup.core.common.format.formatDateTime
import com.aungthiha.topup.core.common.format.formatKyat
import com.aungthiha.topup.core.common.format.formatValidity
import com.aungthiha.topup.core.common.format.packageHighlights
import com.aungthiha.topup.core.designsystem.components.AppTransactionReceipt
import com.aungthiha.topup.core.designsystem.components.AppTransactionReceiptContent
import com.aungthiha.topup.core.designsystem.utils.CaptureController
import com.aungthiha.topup.feature.topup.R
import com.aungthiha.topup.shared.transaction.domain.model.RechargeProductType
import com.aungthiha.topup.shared.transaction.domain.model.Transaction

@Composable
internal fun TransactionReceipt(
    transaction: Transaction,
    captureController: CaptureController,
    modifier: Modifier = Modifier,
) {
    val packageName = when (transaction.product.type) {
        RechargeProductType.DENOMINATION -> stringResource(R.string.topup_item_denomination, formatKyat(transaction.amountMmk))
        RechargeProductType.CUSTOM_AMOUNT -> stringResource(R.string.topup_item_custom)
        RechargeProductType.DATA_PACKAGE -> transaction.product.name
    }
    AppTransactionReceipt(
        content = AppTransactionReceiptContent(
            transactionId = transaction.id,
            dateTime = formatDateTime(transaction.createdAtMillis),
            phoneNumber = transaction.phoneNumber,
            operatorName = transaction.operator.name,
            operatorLogoUrl = transaction.operator.logoUrl,
            packageName = packageName,
            packageDetails = packageHighlights(
                services = transaction.product.services,
                dataAllowanceMb = transaction.product.dataAllowanceMb,
                callMinutes = transaction.product.callMinutes,
                validityDays = null,
            ).joinToString(HIGHLIGHT_SEPARATOR).ifEmpty { null },
            validity = transaction.product.validityDays?.let(::formatValidity),
            amount = formatKyat(transaction.amountMmk),
        ),
        captureController = captureController,
        modifier = modifier,
    )
}
