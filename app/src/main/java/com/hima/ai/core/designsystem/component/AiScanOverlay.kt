package com.hima.ai.core.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hima.ai.core.designsystem.theme.HimaTextStyles
import com.hima.ai.core.designsystem.theme.LocalHimaColors
import kotlinx.coroutines.delay

/**
 * The "AI is inspecting this" treatment drawn over the evidence photo while the
 * model runs: a soft green line sweeping the frame plus four pulsing reticle
 * brackets on the corners.
 *
 * Deliberately direction-neutral — the sweep is vertical and the brackets are
 * symmetric — so it reads identically under Arabic (RTL) and English (LTR)
 * without any mirroring logic. Compose only, no bitmap effects, so it stays
 * cheap enough to run repeatedly during a live demo.
 */
@Composable
fun AiScanOverlay(modifier: Modifier = Modifier) {
    val colors = LocalHimaColors.current
    val transition = rememberInfiniteTransition(label = "aiScan")
    val sweep by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "aiScanSweep",
    )
    val pulse by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1150, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "aiScanPulse",
    )

    Canvas(modifier) {
        drawScanLine(progress = sweep, accent = colors.green)
        drawReticleBrackets(alpha = pulse, accent = colors.green)
    }
}

/** The sweeping line: a wide soft glow band, a solid accent core, and a thin
 *  white filament that gives it the "lit" look over a photograph. */
private fun DrawScope.drawScanLine(progress: Float, accent: Color) {
    val y = size.height * progress
    val glowHeight = 72.dp.toPx()
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.Transparent, accent.copy(alpha = 0.30f), Color.Transparent),
            startY = y - glowHeight / 2f,
            endY = y + glowHeight / 2f,
        ),
        topLeft = Offset(0f, y - glowHeight / 2f),
        size = Size(size.width, glowHeight),
    )
    drawLine(
        color = accent.copy(alpha = 0.95f),
        start = Offset(0f, y),
        end = Offset(size.width, y),
        strokeWidth = 3.dp.toPx(),
        cap = StrokeCap.Round,
    )
    drawLine(
        color = Color.White.copy(alpha = 0.55f),
        start = Offset(0f, y),
        end = Offset(size.width, y),
        strokeWidth = 1.dp.toPx(),
        cap = StrokeCap.Round,
    )
}

/** Four symmetric corner brackets, breathing together with [alpha]. */
private fun DrawScope.drawReticleBrackets(alpha: Float, accent: Color) {
    val arm = 22.dp.toPx()
    val inset = 12.dp.toPx()
    val width = 2.5.dp.toPx()
    val color = accent.copy(alpha = alpha)
    val left = inset
    val top = inset
    val right = size.width - inset
    val bottom = size.height - inset

    fun bracket(x: Float, y: Float, dx: Float, dy: Float) {
        drawLine(color, Offset(x, y), Offset(x + dx, y), width, StrokeCap.Round)
        drawLine(color, Offset(x, y), Offset(x, y + dy), width, StrokeCap.Round)
    }
    bracket(left, top, arm, arm)
    bracket(right, top, -arm, arm)
    bracket(left, bottom, arm, -arm)
    bracket(right, bottom, -arm, -arm)
}

/**
 * The rotating "what the model is doing right now" line under the photo.
 *
 * Cycles [phases] on a loop for as long as it stays composed, so a slow call
 * simply keeps cycling rather than stalling on the last phrase. The transition
 * is vertical on purpose: it has no start/end edge, so nothing has to mirror
 * between Arabic and English. Callers remove this once the real result lands;
 * fading the whole block out (rather than swapping its text) is what stops it
 * cleanly instead of cutting a phrase off mid-transition.
 */
@Composable
fun AnalysisPhaseText(
    phases: List<String>,
    modifier: Modifier = Modifier,
    intervalMs: Long = 1500L,
) {
    val colors = LocalHimaColors.current
    var index by remember { mutableIntStateOf(0) }

    LaunchedEffect(phases, intervalMs) {
        if (phases.isEmpty()) return@LaunchedEffect
        while (true) {
            delay(intervalMs)
            index = (index + 1) % phases.size
        }
    }

    AnimatedContent(
        targetState = phases.getOrElse(index) { "" },
        transitionSpec = {
            (slideInVertically(tween(260, easing = FastOutSlowInEasing)) { it / 3 } + fadeIn(tween(260)))
                .togetherWith(
                    slideOutVertically(tween(200, easing = FastOutSlowInEasing)) { -it / 3 } +
                        fadeOut(tween(160)),
                )
        },
        modifier = modifier,
        label = "analysisPhase",
    ) { phase ->
        Text(
            text = phase,
            style = HimaTextStyles.b.copy(fontSize = 14.5.sp),
            color = colors.sage,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
