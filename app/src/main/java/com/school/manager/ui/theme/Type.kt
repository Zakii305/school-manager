package com.school.manager.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val AmiriFamily = FontFamily.Serif
val LatinFamily = FontFamily.SansSerif

val AppTypography = Typography(
    displayLarge = TextStyle(fontFamily = LatinFamily, fontWeight = FontWeight.Bold, fontSize = 57.sp),
    displayMedium = TextStyle(fontFamily = LatinFamily, fontWeight = FontWeight.Bold, fontSize = 45.sp),
    headlineLarge = TextStyle(fontFamily = LatinFamily, fontWeight = FontWeight.SemiBold, fontSize = 32.sp),
    headlineMedium = TextStyle(fontFamily = LatinFamily, fontWeight = FontWeight.SemiBold, fontSize = 28.sp),
    titleLarge = TextStyle(fontFamily = LatinFamily, fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
    bodyLarge = TextStyle(fontFamily = LatinFamily, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = LatinFamily, fontSize = 14.sp),
    labelLarge = TextStyle(fontFamily = LatinFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp)
)

object ArabicStyles {
    val ayat = TextStyle(
        fontFamily = AmiriFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 26.sp,
        lineHeight = 44.sp
    )
    val translation = TextStyle(
        fontFamily = LatinFamily,
        fontStyle = FontStyle.Italic,
        fontSize = 15.sp,
        lineHeight = 24.sp
    )
}
