package com.aungthiha.topup.feature.topup.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import com.aungthiha.topup.core.common.format.HIGHLIGHT_SEPARATOR
import com.aungthiha.topup.core.common.format.formatGrouped
import com.aungthiha.topup.core.common.format.formatKyat
import com.aungthiha.topup.core.common.format.packageHighlights
import com.aungthiha.topup.core.designsystem.components.AppBanner
import com.aungthiha.topup.core.designsystem.components.AppBannerTone
import com.aungthiha.topup.core.designsystem.components.AppSelectableCard
import com.aungthiha.topup.core.designsystem.components.AppTextButton
import com.aungthiha.topup.core.designsystem.components.AppTextField
import com.aungthiha.topup.core.designsystem.icons.AppIcons
import com.aungthiha.topup.core.designsystem.theme.appColorScheme
import com.aungthiha.topup.core.designsystem.theme.appSpacing
import com.aungthiha.topup.core.designsystem.theme.appTypography
import com.aungthiha.topup.feature.topup.R
import com.aungthiha.topup.feature.topup.domain.amount.CustomAmountValidation
import com.aungthiha.topup.feature.topup.domain.model.CatalogProduct
import com.aungthiha.topup.feature.topup.domain.model.CustomAmountRule
import com.aungthiha.topup.feature.topup.domain.model.RechargeTab

private const val DENOMINATION_COLUMNS = 3
private const val PACKAGE_COLUMNS = 2
private const val TAB_ANIMATION_MILLIS = 200

@Composable
internal fun RechargeTabRow(
    activeTab: RechargeTab,
    onTabSelected: (RechargeTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.appColorScheme
    PrimaryTabRow(
        selectedTabIndex = activeTab.ordinal,
        modifier = modifier.fillMaxWidth(),
        containerColor = colors.background,
        contentColor = colors.primary,
        indicator = {
            TabRowDefaults.PrimaryIndicator(
                modifier = Modifier.tabIndicatorOffset(activeTab.ordinal, matchContentSize = false),
                color = colors.primary,
            )
        },
        divider = { HorizontalDivider(color = colors.outlineVariant) },
    ) {
        RechargeTab.entries.forEach { tab ->
            val selected = tab == activeTab
            Tab(
                selected = selected,
                onClick = { onTabSelected(tab) },
                selectedContentColor = colors.primary,
                unselectedContentColor = colors.contentSecondary,
                text = {
                    Text(
                        text = stringResource(
                            when (tab) {
                                RechargeTab.DENOMINATIONS -> R.string.topup_tab_denominations
                                RechargeTab.PACKAGES -> R.string.topup_tab_packages
                            },
                        ),
                        style = MaterialTheme.appTypography.titleSmall.copy(
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        ),
                    )
                },
            )
        }
    }
}

/** Animated container that swaps between the two tabs' content. */
@Composable
internal fun RechargeTabContent(
    activeTab: RechargeTab,
    modifier: Modifier = Modifier,
    content: @Composable (RechargeTab) -> Unit,
) {
    AnimatedContent(
        targetState = activeTab,
        transitionSpec = { fadeIn(tween(TAB_ANIMATION_MILLIS)) togetherWith fadeOut(tween(TAB_ANIMATION_MILLIS)) },
        label = "rechargeTabContent",
        modifier = modifier,
    ) { tab -> content(tab) }
}

@Composable
internal fun DenominationOptions(
    denominations: List<CatalogProduct>,
    selectedProductId: String?,
    isCustomAmountVisible: Boolean,
    customAmountInput: String,
    customAmountValidation: CustomAmountValidation,
    customAmountRule: CustomAmountRule?,
    onSelect: (String) -> Unit,
    onToggleCustomAmount: () -> Unit,
    onCustomAmountChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.appSpacing
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(spacing.medium)) {
        if (denominations.isEmpty()) {
            EmptyOptionsText(text = stringResource(R.string.topup_no_denominations))
        } else {
            SelectionGrid(items = denominations, columns = DENOMINATION_COLUMNS) { product ->
                DenominationCard(
                    product = product,
                    selected = product.id == selectedProductId,
                    onClick = { onSelect(product.id) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        AppTextButton(
            text = stringResource(if (isCustomAmountVisible) R.string.topup_custom_amount_hide else R.string.topup_custom_amount_cta),
            onClick = onToggleCustomAmount,
            leadingIcon = if (isCustomAmountVisible) AppIcons.Close else AppIcons.Add,
            enabled = customAmountRule != null,
        )

        AnimatedVisibility(
            visible = isCustomAmountVisible && customAmountRule != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            if (customAmountRule != null) {
                CustomAmountField(
                    input = customAmountInput,
                    validation = customAmountValidation,
                    rule = customAmountRule,
                    onChange = onCustomAmountChange,
                )
            }
        }
    }
}

@Composable
private fun CustomAmountField(
    input: String,
    validation: CustomAmountValidation,
    rule: CustomAmountRule,
    onChange: (String) -> Unit,
) {
    val ruleText = stringResource(
        R.string.topup_custom_amount_rule,
        formatKyat(rule.minMmk),
        formatKyat(rule.maxMmk),
        formatKyat(rule.stepMmk),
    )
    val error = when (validation) {
        CustomAmountValidation.NotANumber -> stringResource(R.string.topup_custom_amount_not_number)
        is CustomAmountValidation.BelowMinimum -> stringResource(R.string.topup_custom_amount_below, formatKyat(validation.minMmk))
        is CustomAmountValidation.AboveMaximum -> stringResource(R.string.topup_custom_amount_above, formatKyat(validation.maxMmk))
        is CustomAmountValidation.InvalidStep -> stringResource(R.string.topup_custom_amount_step, formatKyat(validation.stepMmk))
        CustomAmountValidation.Empty, is CustomAmountValidation.Valid -> null
    }
    AppTextField(
        value = input,
        onValueChange = onChange,
        label = stringResource(R.string.topup_custom_amount_label),
        placeholder = formatGrouped(rule.minMmk),
        supportingText = error ?: ruleText,
        isError = error != null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
        trailingContent = {
            Text(
                text = stringResource(R.string.topup_kyat_suffix),
                style = MaterialTheme.appTypography.labelLarge,
                color = MaterialTheme.appColorScheme.contentSecondary,
                modifier = Modifier.padding(end = MaterialTheme.appSpacing.medium),
            )
        },
    )
}

@Composable
internal fun PackageOptions(
    packages: List<CatalogProduct>,
    selectedProductId: String?,
    showDemoNote: Boolean,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.appSpacing
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(spacing.medium)) {
        if (packages.isEmpty()) {
            EmptyOptionsText(text = stringResource(R.string.topup_no_packages))
        } else {
            SelectionGrid(items = packages, columns = PACKAGE_COLUMNS) { product ->
                PackageCard(
                    product = product,
                    selected = product.id == selectedProductId,
                    onClick = { onSelect(product.id) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        if (showDemoNote) {
            AppBanner(message = stringResource(R.string.topup_demo_catalog_note), tone = AppBannerTone.Info)
        }
    }
}

@Composable
private fun DenominationCard(
    product: CatalogProduct,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.appColorScheme
    val spacing = MaterialTheme.appSpacing
    AppSelectableCard(
        selected = selected,
        onClick = onClick,
        modifier = modifier.fillMaxHeight(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = spacing.thumbnailShort)
                .padding(vertical = spacing.small),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = formatGrouped(product.priceMmk),
                style = MaterialTheme.appTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = if (selected) colors.primary else colors.contentPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            Text(
                text = stringResource(R.string.topup_kyat_suffix),
                style = MaterialTheme.appTypography.labelMedium,
                color = colors.contentSecondary,
            )
        }
    }
}

@Composable
private fun PackageCard(
    product: CatalogProduct,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.appColorScheme
    val spacing = MaterialTheme.appSpacing
    val highlights = packageHighlights(
        services = product.services,
        dataAllowanceMb = product.dataAllowanceMb,
        callMinutes = product.callMinutes,
        validityDays = product.validityDays,
        fallbackName = product.name,
    )
    AppSelectableCard(
        selected = selected,
        onClick = onClick,
        modifier = modifier.fillMaxHeight(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = spacing.extraLarge),
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            Text(
                text = formatKyat(product.priceMmk),
                style = MaterialTheme.appTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = if (selected) colors.primary else colors.contentPrimary,
            )
            Text(
                text = highlights.joinToString(HIGHLIGHT_SEPARATOR),
                style = MaterialTheme.appTypography.bodySmall,
                color = colors.contentSecondary,
            )
        }
    }
}

/** Fixed column count; cards in a row share the tallest card's height. */
@Composable
private fun <T> SelectionGrid(
    items: List<T>,
    columns: Int,
    itemContent: @Composable RowScope.(T) -> Unit,
) {
    val spacing = MaterialTheme.appSpacing
    Column(verticalArrangement = Arrangement.spacedBy(spacing.medium)) {
        items.chunked(columns).forEach { rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(spacing.medium),
            ) {
                rowItems.forEach { item -> itemContent(item) }
                repeat(columns - rowItems.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun EmptyOptionsText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.appTypography.bodyMedium,
        color = MaterialTheme.appColorScheme.contentSecondary,
        modifier = Modifier.padding(vertical = MaterialTheme.appSpacing.large),
    )
}
