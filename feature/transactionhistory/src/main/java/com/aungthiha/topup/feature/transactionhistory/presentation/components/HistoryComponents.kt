package com.aungthiha.topup.feature.transactionhistory.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.aungthiha.topup.core.common.format.HIGHLIGHT_SEPARATOR
import com.aungthiha.topup.core.common.format.formatDate
import com.aungthiha.topup.core.common.format.formatDateTime
import com.aungthiha.topup.core.common.format.formatKyat
import com.aungthiha.topup.core.common.format.packageHighlights
import com.aungthiha.topup.core.designsystem.components.AppIconButton
import com.aungthiha.topup.core.designsystem.components.AppOperatorLogo
import com.aungthiha.topup.core.designsystem.components.AppSuccessChip
import com.aungthiha.topup.core.designsystem.components.AppTextField
import com.aungthiha.topup.core.designsystem.icons.AppIcons
import com.aungthiha.topup.core.designsystem.theme.appColorScheme
import com.aungthiha.topup.core.designsystem.theme.appShapes
import com.aungthiha.topup.core.designsystem.theme.appSpacing
import com.aungthiha.topup.core.designsystem.theme.appTypography
import com.aungthiha.topup.feature.transactionhistory.R
import com.aungthiha.topup.feature.transactionhistory.presentation.model.HistoryFilterState
import com.aungthiha.topup.feature.transactionhistory.presentation.model.HistoryOperatorFilter
import com.aungthiha.topup.shared.transaction.domain.model.RechargeProductType
import com.aungthiha.topup.shared.transaction.domain.model.Transaction
import com.aungthiha.topup.shared.transaction.domain.model.TransactionSortOrder
import java.util.TimeZone

@Composable
internal fun HistorySearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppTextField(
        value = value,
        onValueChange = onValueChange,
        label = stringResource(R.string.history_search_label),
        placeholder = stringResource(R.string.history_search_placeholder),
        leadingIcon = AppIcons.Search,
        trailingContent = if (value.isNotEmpty()) {
            {
                AppIconButton(
                    icon = AppIcons.Close,
                    contentDescription = stringResource(R.string.history_clear_search),
                    onClick = onClear,
                )
            }
        } else {
            null
        },
        modifier = modifier,
    )
}

@Composable
internal fun HistoryFilterRow(
    filters: HistoryFilterState,
    onDateClick: () -> Unit,
    onOperatorToggle: (String) -> Unit,
    onSortToggle: () -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.appSpacing
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val dateRange = filters.toQuery().dateRange
        HistoryChip(
            selected = dateRange != null,
            label = dateRange?.let {
                stringResource(
                    R.string.history_filter_date_range,
                    formatDate(it.startMillis),
                    formatDate(it.endMillisExclusive - 1),
                )
            } ?: stringResource(R.string.history_filter_date),
            icon = AppIcons.Calendar,
            onClick = onDateClick,
        )
        HistoryOperatorFilter.entries.forEach { operator ->
            HistoryChip(
                selected = operator.code in filters.operatorCodes,
                label = operator.label,
                onClick = { onOperatorToggle(operator.code) },
            )
        }
        HistoryChip(
            selected = filters.sortOrder == TransactionSortOrder.OLDEST_FIRST,
            label = stringResource(
                when (filters.sortOrder) {
                    TransactionSortOrder.NEWEST_FIRST -> R.string.history_sort_newest
                    TransactionSortOrder.OLDEST_FIRST -> R.string.history_sort_oldest
                },
            ),
            icon = AppIcons.Sort,
            onClick = onSortToggle,
        )
        if (filters.hasFilters) {
            TextButton(onClick = onClearFilters) {
                Text(
                    text = stringResource(R.string.history_clear_filters),
                    style = MaterialTheme.appTypography.labelLarge,
                    color = MaterialTheme.appColorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun HistoryChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    icon: Int? = null,
) {
    val colors = MaterialTheme.appColorScheme
    val spacing = MaterialTheme.appSpacing
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = label, style = MaterialTheme.appTypography.labelLarge) },
        leadingIcon = when {
            selected -> {
                { Icon(painterResource(AppIcons.Check), contentDescription = null, modifier = Modifier.size(spacing.large)) }
            }
            icon != null -> {
                { Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(spacing.large)) }
            }
            else -> null
        },
        shape = MaterialTheme.appShapes.pill,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = colors.surface,
            labelColor = colors.contentPrimary,
            iconColor = colors.contentSecondary,
            selectedContainerColor = colors.primary.copy(alpha = SELECTED_CHIP_ALPHA),
            selectedLabelColor = colors.primary,
            selectedLeadingIconColor = colors.primary,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = colors.outline,
            selectedBorderColor = colors.primary,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HistoryDateRangeDialog(
    initialStartUtcMillis: Long?,
    initialEndUtcMillis: Long?,
    onApply: (Long?, Long?) -> Unit,
    onDismiss: () -> Unit,
) {
    val todayUtc = System.currentTimeMillis().let { it + TimeZone.getDefault().getOffset(it) }
    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialStartUtcMillis,
        initialSelectedEndDateMillis = initialEndUtcMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= todayUtc
        },
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { onApply(state.selectedStartDateMillis, state.selectedEndDateMillis) },
                enabled = state.selectedStartDateMillis != null,
            ) { Text(stringResource(R.string.history_date_picker_apply)) }
        },
        dismissButton = {
            Row {
                TextButton(onClick = { onApply(null, null) }) { Text(stringResource(R.string.history_date_picker_clear)) }
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.history_date_picker_cancel)) }
            }
        },
    ) {
        DateRangePicker(
            state = state,
            title = {
                Text(
                    text = stringResource(R.string.history_date_picker_title),
                    style = MaterialTheme.appTypography.titleMedium,
                    modifier = Modifier.padding(
                        start = MaterialTheme.appSpacing.extraLarge,
                        top = MaterialTheme.appSpacing.large,
                    ),
                )
            },
            showModeToggle = false,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
internal fun TransactionRow(
    transaction: Transaction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.appColorScheme
    val spacing = MaterialTheme.appSpacing
    val shape = MaterialTheme.appShapes.large
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surface)
            .border(spacing.hairline, colors.outlineVariant, shape)
            .clickable(onClick = onClick)
            .padding(spacing.medium),
        horizontalArrangement = Arrangement.spacedBy(spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppOperatorLogo(url = transaction.operator.logoUrl, operatorName = transaction.operator.name)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing.extraExtraSmall)) {
            Text(
                text = transaction.phoneNumber,
                style = MaterialTheme.appTypography.titleSmall,
                color = colors.contentPrimary,
            )
            Text(
                text = transaction.summary(),
                style = MaterialTheme.appTypography.bodySmall,
                color = colors.contentSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = formatDateTime(transaction.createdAtMillis),
                style = MaterialTheme.appTypography.labelSmall,
                color = colors.contentSecondary,
            )
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(spacing.extraSmall)) {
            Text(
                text = formatKyat(transaction.amountMmk),
                style = MaterialTheme.appTypography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = colors.contentPrimary,
            )
            AppSuccessChip(text = stringResource(R.string.history_status_success))
        }
    }
}

@Composable
internal fun Transaction.packageTitle(): String = when (product.type) {
    RechargeProductType.DENOMINATION -> stringResource(R.string.history_item_denomination, formatKyat(amountMmk))
    RechargeProductType.CUSTOM_AMOUNT -> stringResource(R.string.history_item_custom)
    RechargeProductType.DATA_PACKAGE -> product.name
}

@Composable
private fun Transaction.summary(): String =
    (listOf(packageTitle()) + packageHighlights(
        services = product.services,
        dataAllowanceMb = product.dataAllowanceMb,
        callMinutes = product.callMinutes,
        validityDays = product.validityDays,
    )).joinToString(HIGHLIGHT_SEPARATOR)

private const val SELECTED_CHIP_ALPHA = 0.08f
