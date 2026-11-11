package com.aungthiha.topup.core.common.format

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val MEGABYTES_PER_GIGABYTE = 1024

/** Formats whole Myanmar kyat, e.g. `500000` → `500,000 Ks`. */
fun formatKyat(amountMmk: Long): String = "${formatGrouped(amountMmk)} Ks"

/** Digit grouping only, e.g. `20000` → `20,000`. */
fun formatGrouped(value: Long): String = NumberFormat.getIntegerInstance(Locale.US).format(value)

/** `512` → `512 MB`, `5120` → `5 GB`, `1536` → `1.5 GB`. */
fun formatDataAllowance(megabytes: Int): String {
    if (megabytes < MEGABYTES_PER_GIGABYTE) return "$megabytes MB"
    val gigabytes = megabytes.toDouble() / MEGABYTES_PER_GIGABYTE
    val rounded = Math.round(gigabytes * 10) / 10.0
    return if (rounded % 1.0 == 0.0) "${rounded.toLong()} GB" else "$rounded GB"
}

fun formatMinutes(minutes: Int): String = if (minutes == 1) "1 Minute" else "$minutes Minutes"

fun formatValidity(days: Int): String = if (days == 1) "1 Day" else "$days Days"

const val HIGHLIGHT_SEPARATOR = " · "

/**
 * Package information in display order, using only the fields the catalog actually provides:
 * `YouTube + TikTok`, `5 GB`, `50 Minutes`, `30 Days`. [fallbackName] leads when there are no
 * services so the package is still identifiable.
 */
fun packageHighlights(
    services: List<String>,
    dataAllowanceMb: Int?,
    callMinutes: Int?,
    validityDays: Int?,
    fallbackName: String? = null,
): List<String> = buildList {
    when {
        services.isNotEmpty() -> add(services.joinToString(" + "))
        !fallbackName.isNullOrBlank() -> add(fallbackName)
    }
    dataAllowanceMb?.let { add(formatDataAllowance(it)) }
    callMinutes?.let { add(formatMinutes(it)) }
    validityDays?.let { add(formatValidity(it)) }
}

/** `11 Nov 2026, 2:24 PM` in the device time zone. */
fun formatDateTime(epochMillis: Long, locale: Locale = Locale.getDefault()): String =
    SimpleDateFormat("d MMM yyyy, h:mm a", locale).format(Date(epochMillis))

/** `11 Nov 2026` in the device time zone. */
fun formatDate(epochMillis: Long, locale: Locale = Locale.getDefault()): String =
    SimpleDateFormat("d MMM yyyy", locale).format(Date(epochMillis))
