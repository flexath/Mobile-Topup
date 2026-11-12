package com.aungthiha.topup.feature.topup.domain.amount

import com.aungthiha.topup.feature.topup.domain.model.CustomAmountRule

sealed interface CustomAmountValidation {
    data object Empty : CustomAmountValidation
    data object NotANumber : CustomAmountValidation
    data class BelowMinimum(val minMmk: Long) : CustomAmountValidation
    data class AboveMaximum(val maxMmk: Long) : CustomAmountValidation
    data class InvalidStep(val stepMmk: Long) : CustomAmountValidation
    data class Valid(val amountMmk: Long) : CustomAmountValidation
}

object CustomAmountValidator {

    private const val MAX_INPUT_DIGITS = 9

    /** Keeps digits only (grouping commas / spaces are dropped) and caps the length. */
    fun sanitize(input: String): String = input.filter(Char::isDigit).take(MAX_INPUT_DIGITS)

    fun validate(input: String, rule: CustomAmountRule): CustomAmountValidation {
        val trimmed = input.trim().replace(",", "")
        if (trimmed.isEmpty()) return CustomAmountValidation.Empty
        val amount = trimmed.toLongOrNull()?.takeIf { trimmed.all(Char::isDigit) }
            ?: return CustomAmountValidation.NotANumber
        return when {
            amount < rule.minMmk -> CustomAmountValidation.BelowMinimum(rule.minMmk)
            amount > rule.maxMmk -> CustomAmountValidation.AboveMaximum(rule.maxMmk)
            amount % rule.stepMmk != 0L -> CustomAmountValidation.InvalidStep(rule.stepMmk)
            else -> CustomAmountValidation.Valid(amount)
        }
    }
}
