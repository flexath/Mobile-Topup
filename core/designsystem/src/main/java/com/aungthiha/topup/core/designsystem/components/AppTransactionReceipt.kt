package com.aungthiha.topup.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.aungthiha.topup.core.designsystem.R
import com.aungthiha.topup.core.designsystem.theme.appColorScheme
import com.aungthiha.topup.core.designsystem.theme.appSpacing
import com.aungthiha.topup.core.designsystem.utils.CaptureController
import com.aungthiha.topup.core.designsystem.utils.capturable

/** Pre-formatted receipt values. Wallet balance is intentionally not part of a receipt. */
@Immutable
data class AppTransactionReceiptContent(
    val transactionId: String,
    val dateTime: String,
    val phoneNumber: String,
    val operatorName: String,
    val operatorLogoUrl: String?,
    val packageName: String,
    val packageDetails: String?,
    val validity: String?,
    val amount: String,
)

/**
 * Shareable transaction receipt. Everything inside is captured by [captureController]; app bars,
 * navigation, wallet balance and branding live outside it.
 */
@Composable
fun AppTransactionReceipt(
    content: AppTransactionReceiptContent,
    captureController: CaptureController,
    modifier: Modifier = Modifier,
) {
    val status = stringResource(R.string.ds_receipt_status_success)
    val lines = buildList {
        add(AppReceiptLine(stringResource(R.string.ds_receipt_transaction_id), content.transactionId))
        add(AppReceiptLine(stringResource(R.string.ds_receipt_date), content.dateTime))
        add(AppReceiptLine(stringResource(R.string.ds_receipt_phone), content.phoneNumber))
        add(AppReceiptLine(stringResource(R.string.ds_receipt_operator), content.operatorName))
        add(AppReceiptLine(stringResource(R.string.ds_receipt_package), content.packageName))
        content.packageDetails?.let { add(AppReceiptLine(stringResource(R.string.ds_receipt_details), it)) }
        content.validity?.let { add(AppReceiptLine(stringResource(R.string.ds_receipt_validity), it)) }
        add(AppReceiptLine(stringResource(R.string.ds_receipt_status), status))
    }
    Box(
        modifier = modifier
            .capturable(captureController)
            .background(MaterialTheme.appColorScheme.background)
            .padding(MaterialTheme.appSpacing.small),
    ) {
        AppReceiptCard(
            operatorName = content.operatorName,
            operatorLogoUrl = content.operatorLogoUrl,
            statusText = status,
            amountLabel = stringResource(R.string.ds_receipt_amount),
            amountText = content.amount,
            lines = lines,
        )
    }
}
