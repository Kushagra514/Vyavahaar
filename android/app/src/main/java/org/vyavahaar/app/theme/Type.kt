package org.vyavahaar.app.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Senior-friendly typography scale:
// - Generous line heights designed specifically for Devanagari (Hindi) matras (मात्राएं)
// - Letter spacing is zero to prevent detaching conjuncts (संयुक्त अक्षर) in Hindi
// - Clean semi-bold/medium weights for high legibility on aging eyes
fun createSeniorTypography(fontScaleMultiplier: Float = 1.0f): Typography {
    val m = fontScaleMultiplier.coerceIn(1.0f, 1.6f)
    return Typography(
        headlineLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = (26 * m).sp,
            lineHeight = (36 * m).sp,
            letterSpacing = 0.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = (22 * m).sp,
            lineHeight = (32 * m).sp,
            letterSpacing = 0.sp
        ),
        titleLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = (20 * m).sp,
            lineHeight = (30 * m).sp,
            letterSpacing = 0.sp
        ),
        titleMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = (18 * m).sp,
            lineHeight = (26 * m).sp,
            letterSpacing = 0.sp
        ),
        titleSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = (16 * m).sp,
            lineHeight = (24 * m).sp,
            letterSpacing = 0.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (17 * m).sp,
            lineHeight = (26 * m).sp,
            letterSpacing = 0.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (15 * m).sp,
            lineHeight = (23 * m).sp,
            letterSpacing = 0.sp
        ),
        bodySmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (13 * m).sp,
            lineHeight = (19 * m).sp,
            letterSpacing = 0.sp
        ),
        labelLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = (15 * m).sp,
            lineHeight = (22 * m).sp,
            letterSpacing = 0.sp
        ),
        labelMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = (13 * m).sp,
            lineHeight = (18 * m).sp,
            letterSpacing = 0.sp
        ),
        labelSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = (12 * m).sp,
            lineHeight = (16 * m).sp,
            letterSpacing = 0.sp
        )
    )
}
