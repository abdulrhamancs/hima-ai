package com.hima.ai.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing scale. [gutter] is the standard 20dp horizontal screen padding used
 * throughout the "White-first system" prototype.
 */
@Immutable
data class Spacing(
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 20.dp,
    val xxl: Dp = 32.dp,
    val gutter: Dp = 20.dp,
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }
