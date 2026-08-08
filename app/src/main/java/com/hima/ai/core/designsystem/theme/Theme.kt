package com.hima.ai.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

/*
 * Material 3 colour scheme built from the Hima tokens. The design is a single
 * (light, white-first) theme; brand green is the primary action colour.
 * Severity colours are intentionally NOT part of the M3 scheme — they live in
 * HimaColors and are applied only via SeverityBadge.
 */
private val HimaColorScheme = lightColorScheme(
    primary = Green,
    onPrimary = Color.White,
    background = Bg,
    onBackground = Ink,
    surface = Bg,
    onSurface = Ink,
    surfaceVariant = Bg2,
    onSurfaceVariant = Ink2,
    outline = Sage,
    error = SeverityCritical,
    onError = Color.White,
    tertiary = Beige,
)

/**
 * Root theme for the app. Provides the Material 3 theme plus the extended
 * [HimaColors] and [Spacing] tokens via composition locals.
 */
@Composable
fun HimaTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalHimaColors provides LightHimaColors,
        LocalSpacing provides Spacing(),
    ) {
        MaterialTheme(
            colorScheme = HimaColorScheme,
            typography = HimaTypography,
            shapes = HimaShapes,
            content = content,
        )
    }
}
