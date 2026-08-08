package com.hima.ai.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/*
 * Raw design tokens — taken verbatim from the approved "White-first system"
 * prototype's :root palette (design/prototype/hima-white-system.html).
 */
val Ink = Color(0xFF2C2F2B)      // primary text
val Ink2 = Color(0xFF5B6359)     // secondary text / body
val Sage = Color(0xFF8D9A8A)     // tertiary text / placeholders / icons
val Beige = Color(0xFFC8C2B5)    // muted accents
val Warm = Color(0xFFF1EEE6)     // warm surface (hero overlays, secondary buttons)
val Bg = Color(0xFFFFFFFF)       // app background
val Bg2 = Color(0xFFFAFAF8)      // sunk surface (fields, stat tiles, chips bg)
val Green = Color(0xFF2E4A38)    // THE action colour — one primary action per screen
val GreenDeep = Color(0xFF22392A)

// Severity ramp — deliberately off the green action colour (4 levels: low/mid/high/critical).
val SeverityLow = Color(0xFF8D9A8A)
val SeverityMid = Color(0xFFB08A3E)
val SeverityHigh = Color(0xFFD97706)
val SeverityCritical = Color(0xFFC0392B)

// Severity badge background + foreground pairs (SeverityBadge component).
val SeverityLowBg = Color(0xFFEFF1EE)
val SeverityLowFg = Color(0xFF5F6E5D)
val SeverityMidBg = Color(0xFFF6EFDF)
val SeverityMidFg = Color(0xFF8A6A24)
val SeverityHighBg = Color(0xFFFBEDE0)
val SeverityHighFg = Color(0xFFB4610A)
val SeverityCriticalBg = Color(0xFFFAEAE8)
val SeverityCriticalFg = Color(0xFFA32E22)

/**
 * Extended, brand-specific colours that don't map onto Material 3's slots.
 * Exposed via [LocalHimaColors] so components read them like `LocalHimaColors.current.sage`.
 */
@Immutable
data class HimaColors(
    val ink: Color,
    val ink2: Color,
    val sage: Color,
    val beige: Color,
    val warm: Color,
    val bg: Color,
    val bg2: Color,
    val green: Color,
    val greenDeep: Color,
    val severityLow: Color,
    val severityMid: Color,
    val severityHigh: Color,
    val severityCritical: Color,
)

val LightHimaColors = HimaColors(
    ink = Ink,
    ink2 = Ink2,
    sage = Sage,
    beige = Beige,
    warm = Warm,
    bg = Bg,
    bg2 = Bg2,
    green = Green,
    greenDeep = GreenDeep,
    severityLow = SeverityLow,
    severityMid = SeverityMid,
    severityHigh = SeverityHigh,
    severityCritical = SeverityCritical,
)

val LocalHimaColors = staticCompositionLocalOf { LightHimaColors }
