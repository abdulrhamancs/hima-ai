package com.hima.ai.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Named radii on the shared 4dp-oriented Hima shape scale. */
object HimaRadius {
    val chip = 12.dp
    val icon = 12.dp
    val field = 14.dp
    val button = 14.dp
    val thumb = 12.dp
    val hero = 20.dp
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
