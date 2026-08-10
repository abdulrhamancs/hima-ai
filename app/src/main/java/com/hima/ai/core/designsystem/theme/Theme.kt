package com.hima.ai.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

/* Material 3 schemes built from Hima's semantic light and dark tokens. */
private val LightHimaColorScheme = lightColorScheme(
    primary = Green,
    onPrimary = OnHero,
    background = Bg,
    onBackground = Ink,
    surface = Surface,
    onSurface = Ink,
    surfaceVariant = Bg2,
    onSurfaceVariant = Ink2,
    outline = Beige,
    error = SeverityCritical,
    onError = Color.White,
    tertiary = Beige,
)

private val DarkHimaColorScheme = darkColorScheme(
    primary = DarkGreen,
    onPrimary = DarkGreenDeep,
    background = DarkBg,
    onBackground = DarkInk,
    surface = DarkSurface,
    onSurface = DarkInk,
    surfaceVariant = DarkBg2,
    onSurfaceVariant = DarkInk2,
    outline = DarkDivider,
    error = DarkSeverityCritical,
    onError = DarkInk,
    tertiary = DarkBeige,
)

/**
 * Root theme for the app. Provides the Material 3 theme plus the extended
 * [HimaColors] and [Spacing] tokens via composition locals.
 */
@Composable
fun HimaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkHimaColors else LightHimaColors
    CompositionLocalProvider(
        LocalHimaColors provides colors,
        LocalSpacing provides Spacing(),
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkHimaColorScheme else LightHimaColorScheme,
            typography = HimaTypography,
            shapes = HimaShapes,
            content = content,
        )
    }
}
