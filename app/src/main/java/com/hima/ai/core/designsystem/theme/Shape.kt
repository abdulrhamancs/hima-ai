package com.hima.ai.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Named corner radii from the "White-first system" prototype. */
object HimaRadius {
    val chip = 9.dp
    val icon = 12.dp
    val field = 16.dp
    val button = 18.dp
    val thumb = 14.dp
    val hero = 22.dp
    val card = 20.dp
    val sheet = 24.dp
}

/** Material 3 shape scale, mapped to the Hima radii. */
val HimaShapes = Shapes(
    extraSmall = RoundedCornerShape(HimaRadius.chip),
    small = RoundedCornerShape(HimaRadius.field),
    medium = RoundedCornerShape(HimaRadius.button),
    large = RoundedCornerShape(HimaRadius.card),
    extraLarge = RoundedCornerShape(HimaRadius.sheet),
)
