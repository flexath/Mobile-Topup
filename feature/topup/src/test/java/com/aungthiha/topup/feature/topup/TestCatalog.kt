package com.aungthiha.topup.feature.topup

import com.aungthiha.topup.feature.topup.domain.model.CatalogProduct
import com.aungthiha.topup.feature.topup.domain.model.CatalogProductType
import com.aungthiha.topup.feature.topup.domain.model.CustomAmountRule
import com.aungthiha.topup.feature.topup.domain.model.OperatorCatalog
import com.aungthiha.topup.feature.topup.domain.model.OperatorCode
import com.aungthiha.topup.feature.topup.domain.model.TelecomOperator

internal object TestCatalog {
    val rule = CustomAmountRule(minMmk = 1_000, maxMmk = 50_000, stepMmk = 100)

    fun denomination(operator: OperatorCode, amount: Long) = CatalogProduct(
        id = "${operator.code}-den-$amount",
        sku = "${operator.code}-AIRTIME-$amount",
        operatorCode = operator,
        type = CatalogProductType.DENOMINATION,
        name = "Airtime top-up",
        priceMmk = amount,
        dataAllowanceMb = null,
        callMinutes = null,
        services = emptyList(),
        validityDays = null,
        isDemo = true,
    )

    fun dataPackage(operator: OperatorCode, price: Long) = CatalogProduct(
        id = "${operator.code}-pkg-$price",
        sku = "${operator.code}-PKG-$price",
        operatorCode = operator,
        type = CatalogProductType.DATA_PACKAGE,
        name = "Demo pack",
        priceMmk = price,
        dataAllowanceMb = 1024,
        callMinutes = 50,
        services = listOf("YouTube"),
        validityDays = 7,
        isDemo = true,
    )

    fun operatorCatalog(operator: OperatorCode) = OperatorCatalog(
        operator = TelecomOperator(
            code = operator,
            displayName = operator.fallbackDisplayName,
            fullName = operator.fallbackDisplayName,
            logoUrl = "https://example.test/${operator.code}.jpeg",
            customAmountRule = rule,
        ),
        denominations = listOf(1_000L, 2_000L, 20_000L).map { denomination(operator, it) },
        packages = listOf(1_500L, 20_000L).map { dataPackage(operator, it) },
    )

    val all = OperatorCode.entries.map(::operatorCatalog)
}
