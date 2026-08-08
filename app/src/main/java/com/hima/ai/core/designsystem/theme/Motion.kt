package com.hima.ai.core.designsystem.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue

/**
 * A decelerating cubic-bezier close to iOS's native transition curve — a
 * smooth, weighted settle with no overshoot. Used for screen transitions and
 * press feedback alike so motion feels the same everywhere in the app.
 */
val HimaEasing: Easing = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)

/** Shared motion durations, all inside the 200–350ms "fast and responsive" band. */
object HimaMotionDuration {
    const val Press = 120
    const val TabSwitch = 220
    const val ScreenPush = 280
}

/**
 * A brief, un-bouncy press-down for buttons and other tappable controls — a
 * small tactile settle, not a spring overshoot.
 */
@Composable
fun rememberPressScale(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.97f,
): Float {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = tween(HimaMotionDuration.Press, easing = HimaEasing),
        label = "pressScale",
    )
    return scale
}
