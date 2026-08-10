@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package com.hima.ai.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.hima.ai.R

/**
 * IBM Plex Sans Arabic carries Arabic text and headings; Inter is reserved for
 * Latin labels and tabular numerals — both bundled locally (OFL-licensed) for
 * offline-first field use. See app/src/main/res/font/OFL.txt.
 */
val IBMPlexSansArabic = FontFamily(
    Font(R.font.ibm_plex_sans_arabic_regular, FontWeight.Normal),
    Font(R.font.ibm_plex_sans_arabic_medium, FontWeight.Medium),
    Font(R.font.ibm_plex_sans_arabic_semibold, FontWeight.SemiBold),
    Font(R.font.ibm_plex_sans_arabic_bold, FontWeight.Bold),
)

val Inter = FontFamily(
    Font(
        R.font.inter_variable,
        FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400)),
    ),
    Font(
        R.font.inter_variable,
        FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500)),
    ),
    Font(
        R.font.inter_variable,
        FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600)),
    ),
    Font(
        R.font.inter_variable,
        FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700)),
    ),
)

/**
 * The named type styles from the design (h1 / h2 / t / b / m), plus [num] for
 * tabular-figure numerals set in Inter.
 */
object HimaTextStyles {
    val h1 = TextStyle(
        fontFamily = IBMPlexSansArabic, fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp, lineHeight = 32.sp, letterSpacing = (-0.3).sp,
    )
    val h2 = TextStyle(
        fontFamily = IBMPlexSansArabic, fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp, lineHeight = 26.sp, letterSpacing = (-0.15).sp,
    )
    val t = TextStyle(
        fontFamily = IBMPlexSansArabic, fontWeight = FontWeight.Medium,
        fontSize = 16.sp, lineHeight = 22.sp,
    )
    val b = TextStyle(
        fontFamily = IBMPlexSansArabic, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 22.sp,
    )
    val m = TextStyle(
        fontFamily = IBMPlexSansArabic, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 18.sp,
    )
    val num = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Medium,
        fontSize = 22.sp, lineHeight = 28.sp,
    )
    val button = TextStyle(
        fontFamily = IBMPlexSansArabic, fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp, lineHeight = 22.sp,
    )
}

/** Material 3 typography, mapped to the Hima scale for built-in M3 components. */
val HimaTypography = Typography(
    displaySmall = HimaTextStyles.h1,
    headlineSmall = HimaTextStyles.h2,
    titleLarge = HimaTextStyles.t,
    labelLarge = HimaTextStyles.t,
    bodyLarge = HimaTextStyles.b,
    bodyMedium = HimaTextStyles.b,
    bodySmall = HimaTextStyles.m,
)
