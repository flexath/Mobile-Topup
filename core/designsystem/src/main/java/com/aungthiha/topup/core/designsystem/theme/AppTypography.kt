package com.aungthiha.topup.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.aungthiha.topup.core.designsystem.R

val Manrope: FontFamily = FontFamily(
    Font(resId = R.font.manrope_bold, weight = FontWeight.Bold),
    Font(resId = R.font.manrope_semibold, weight = FontWeight.SemiBold),
    Font(resId = R.font.manrope_medium, weight = FontWeight.Medium),
    Font(resId = R.font.manrope_regular, weight = FontWeight.Normal),
    Font(resId = R.font.manrope_light, weight = FontWeight.Light),
)

@Immutable
data class AppTypography(
    val displayLarge: TextStyle = TextStyle.Default,
    val displayMedium: TextStyle = TextStyle.Default,
    val displaySmall: TextStyle = TextStyle.Default,
    val headlineLarge: TextStyle = TextStyle.Default,
    val headlineMedium: TextStyle = TextStyle.Default,
    val headlineSmall: TextStyle = TextStyle.Default,
    val titleLarge: TextStyle = TextStyle.Default,
    val titleMedium: TextStyle = TextStyle.Default,
    val titleSmall: TextStyle = TextStyle.Default,
    val bodyLarge: TextStyle = TextStyle.Default,
    val bodyMedium: TextStyle = TextStyle.Default,
    val bodySmall: TextStyle = TextStyle.Default,
    val labelLarge: TextStyle = TextStyle.Default,
    val labelMedium: TextStyle = TextStyle.Default,
    val labelSmall: TextStyle = TextStyle.Default,
)

/**
 * Creates TopUp's shared semantic type scale using Manrope.
 *
 * Direct [sp] values intentionally preserve Android font scaling; typography must not be resized
 * from the window width.
 */
@Composable
fun getTypography(): AppTypography {
    return remember {
        AppTypography(
            displayLarge = textStyle(
                fontFamily = Manrope,
                fontWeight = FontWeight.SemiBold,
                fontSize = 48,
                lineHeight = 56,
                letterSpacing = -0.5,
            ),
            displayMedium = textStyle(
                fontFamily = Manrope,
                fontWeight = FontWeight.SemiBold,
                fontSize = 40,
                lineHeight = 48,
                letterSpacing = -0.4,
            ),
            displaySmall = textStyle(
                fontFamily = Manrope,
                fontWeight = FontWeight.SemiBold,
                fontSize = 34,
                lineHeight = 42,
                letterSpacing = -0.2,
            ),
            headlineLarge = textStyle(
                fontFamily = Manrope,
                fontWeight = FontWeight.SemiBold,
                fontSize = 32,
                lineHeight = 40,
                letterSpacing = -0.2,
            ),
            headlineMedium = textStyle(
                fontFamily = Manrope,
                fontWeight = FontWeight.SemiBold,
                fontSize = 28,
                lineHeight = 36,
                letterSpacing = -0.1,
            ),
            headlineSmall = textStyle(
                fontFamily = Manrope,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24,
                lineHeight = 32,
            ),
            titleLarge = textStyle(
                fontFamily = Manrope,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22,
                lineHeight = 28,
            ),
            titleMedium = textStyle(
                fontFamily = Manrope,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17,
                lineHeight = 24,
            ),
            titleSmall = textStyle(
                fontFamily = Manrope,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15,
                lineHeight = 20,
                letterSpacing = 0.1,
            ),
            bodyLarge = textStyle(
                fontFamily = Manrope,
                fontWeight = FontWeight.Normal,
                fontSize = 17,
                lineHeight = 26,
            ),
            bodyMedium = textStyle(
                fontFamily = Manrope,
                fontWeight = FontWeight.Normal,
                fontSize = 15,
                lineHeight = 22,
            ),
            bodySmall = textStyle(
                fontFamily = Manrope,
                fontWeight = FontWeight.Normal,
                fontSize = 13,
                lineHeight = 18,
                letterSpacing = 0.1,
            ),
            labelLarge = textStyle(
                fontFamily = Manrope,
                fontWeight = FontWeight.Medium,
                fontSize = 15,
                lineHeight = 20,
                letterSpacing = 0.1,
            ),
            labelMedium = textStyle(
                fontFamily = Manrope,
                fontWeight = FontWeight.Medium,
                fontSize = 13,
                lineHeight = 18,
                letterSpacing = 0.2,
            ),
            labelSmall = textStyle(
                fontFamily = Manrope,
                fontWeight = FontWeight.Medium,
                fontSize = 11,
                lineHeight = 16,
                letterSpacing = 0.3,
            ),
        )
    }
}

private fun textStyle(
    fontFamily: FontFamily,
    fontWeight: FontWeight,
    fontSize: Int,
    lineHeight: Int,
    letterSpacing: Double = 0.0,
) = TextStyle(
    fontFamily = fontFamily,
    fontWeight = fontWeight,
    fontSize = fontSize.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp,
)

internal fun AppTypography.toMaterialTypography() = Typography(
    displayLarge = displayLarge,
    displayMedium = displayMedium,
    displaySmall = displaySmall,
    headlineLarge = headlineLarge,
    headlineMedium = headlineMedium,
    headlineSmall = headlineSmall,
    titleLarge = titleLarge,
    titleMedium = titleMedium,
    titleSmall = titleSmall,
    bodyLarge = bodyLarge,
    bodyMedium = bodyMedium,
    bodySmall = bodySmall,
    labelLarge = labelLarge,
    labelMedium = labelMedium,
    labelSmall = labelSmall,
)
