package com.hima.ai.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/* Exact Hima light palette. Components consume these semantically, never by hex. */
val Ink = Color(0xFF172621)      // primary text
val Ink2 = Color(0xFF5C6F5A)    // secondary text / body
val Sage = Color(0xFF5C6F5A)    // placeholders / inactive icons
val Beige = Color(0xFFC3D3C1)   // muted accents / outlines
val Warm = Color(0xFFE8EFE7)    // secondary surface
val Bg = Color(0xFFF7FAF7)      // app background
val Bg2 = Color(0xFFE8EFE7)     // sunk surface (fields, stat tiles, chips)
val Surface = Color(0xFFFFFFFF) // raised cards and navigation
val Divider = Color(0xFFC3D3C1) // subtle structural separation
val Green = Color(0xFF3B593F)   // primary environmental action
val GreenDeep = Color(0xFF172621)
val OnHero = Color(0xFFF2F5F1)

// Intentionally designed dark palette — near-black and layered forest greens.
val DarkInk = Color(0xFFF2F5F1)
val DarkInk2 = Color(0xFFA8B8A5)
val DarkSage = Color(0xFFA8B8A5)
val DarkBeige = Color(0xFF3B593F)
val DarkWarm = Color(0xFF2A4038)
val DarkBg = Color(0xFF0D0D0D)
val DarkBg2 = Color(0xFF2A4038)
val DarkSurface = Color(0xFF172621)
val DarkDivider = Color(0xFF3B593F)
val DarkGreen = Color(0xFF4D734C)
val DarkGreenDeep = Color(0xFF172621)

// Status ramp shared by both themes.
val SeverityLow = Color(0xFF4D734C)
val SeverityMid = Color(0xFFD9B23D)
val SeverityHigh = Color(0xFFD98C3D)
val SeverityCritical = Color(0xFFD64541)
val DarkSeverityCritical = Color(0xFFC4453C)

// Severity badge background + foreground pairs (SeverityBadge component).
val SeverityLowBg = Color(0xFFEFF1EE)
val SeverityLowFg = Color(0xFF5F6E5D)
val SeverityMidBg = Color(0xFFF6EFDF)
val SeverityMidFg = Color(0xFF8A6A24)
val SeverityHighBg = Color(0xFFFBEDE0)
val SeverityHighFg = Color(0xFFB4610A)
val SeverityCriticalBg = Color(0xFFFAEAE8)
val SeverityCriticalFg = Color(0xFFA32E22)
val DarkSeverityLowBg = Color(0xFF25342A)
val DarkSeverityLowFg = Color(0xFFB9C9BB)
val DarkSeverityMidBg = Color(0xFF3A321F)
val DarkSeverityMidFg = Color(0xFFE6C778)
val DarkSeverityHighBg = Color(0xFF402818)
val DarkSeverityHighFg = Color(0xFFF4B069)
val DarkSeverityCriticalBg = Color(0xFF40211E)
val DarkSeverityCriticalFg = Color(0xFFF39A90)

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
    val surface: Color,
    val divider: Color,
    val green: Color,
    val greenDeep: Color,
    val onGreen: Color,
    val severityLow: Color,
    val severityMid: Color,
    val severityHigh: Color,
    val severityCritical: Color,
    val isDark: Boolean,
)

val LightHimaColors = HimaColors(
    ink = Ink,
    ink2 = Ink2,
    sage = Sage,
    beige = Beige,
    warm = Warm,
    bg = Bg,
    bg2 = Bg2,
    surface = Surface,
    divider = Divider,
    green = Green,
    greenDeep = GreenDeep,
    onGreen = OnHero,
    severityLow = SeverityLow,
    severityMid = SeverityMid,
    severityHigh = SeverityHigh,
    severityCritical = SeverityCritical,
    isDark = false,
)

val DarkHimaColors = HimaColors(
    ink = DarkInk,
    ink2 = DarkInk2,
    sage = DarkSage,
    beige = DarkBeige,
    warm = DarkWarm,
    bg = DarkBg,
    bg2 = DarkBg2,
    surface = DarkSurface,
    divider = DarkDivider,
    green = DarkGreen,
    greenDeep = DarkGreenDeep,
    onGreen = DarkBg,
    severityLow = SeverityLow,
    severityMid = SeverityMid,
    severityHigh = SeverityHigh,
    severityCritical = DarkSeverityCritical,
    isDark = true,
)

val LocalHimaColors = staticCompositionLocalOf { LightHimaColors }
