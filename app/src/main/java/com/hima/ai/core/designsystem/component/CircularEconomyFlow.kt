package com.hima.ai.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hima.ai.core.designsystem.theme.HimaTextStyles
import com.hima.ai.core.designsystem.theme.LocalHimaColors
import kotlinx.coroutines.delay

/** The four beats of Hima's value loop, in order. */
enum class EconomyStage { DETECTED, ANALYSED, ACTION, IMPACT }

/**
 * The app's circular-economy story as a diagram rather than a paragraph:
 * incident detected → AI analysis → recommended action → positive impact.
 *
 * [litCount] is how many stages have actually happened; the nodes light up one
 * after another (staggered by [stepDelayMs]) instead of appearing pre-filled,
 * so the loop reads as something the app *did*, not a static legend.
 *
 * Direction handling: the stages sit in a [Row], which Compose already mirrors
 * under RTL, so in Arabic the first stage correctly starts on the right. The
 * connector chevrons are drawn in raw canvas pixels — which do *not* mirror —
 * so they flip explicitly against [LocalLayoutDirection] to keep pointing
 * along the reading direction.
 */
@Composable
fun CircularEconomyFlow(
    labels: List<String>,
    litCount: Int,
    modifier: Modifier = Modifier,
    stepDelayMs: Long = 220L,
) {
    val stages = EconomyStage.entries
    var revealed by remember { mutableIntStateOf(0) }

    LaunchedEffect(litCount) {
        if (litCount <= revealed) {
            revealed = litCount
            return@LaunchedEffect
        }
        while (revealed < litCount) {
            delay(stepDelayMs)
            revealed++
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        stages.forEachIndexed { index, stage ->
            if (index > 0) {
                FlowConnector(
                    lit = revealed > index,
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = NODE_SIZE_DP.dp / 2 - 1.dp),
                )
            }
            FlowNode(
                stage = stage,
                label = labels.getOrElse(index) { "" },
                lit = revealed > index,
            )
        }
    }
}

private const val NODE_SIZE_DP = 40

@Composable
private fun FlowNode(stage: EconomyStage, label: String, lit: Boolean) {
    val colors = LocalHimaColors.current
    val fill by animateColorAsState(
        targetValue = if (lit) colors.green else colors.bg2,
        animationSpec = tween(320),
        label = "economyNodeFill",
    )
    val glyphTint by animateColorAsState(
        targetValue = if (lit) colors.onGreen else colors.sage,
        animationSpec = tween(320),
        label = "economyNodeGlyph",
    )
    // A small spring settle as each node lands — the "this stage just
    // completed" beat, kept subtle so four in a row don't feel bouncy.
    val scale by animateFloatAsState(
        targetValue = if (lit) 1f else 0.92f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "economyNodeScale",
    )

    Column(
        modifier = Modifier.width(72.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(NODE_SIZE_DP.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(fill),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.size(20.dp)) { drawStageGlyph(stage, glyphTint) }
        }
        Text(
            text = label,
            style = HimaTextStyles.b.copy(fontSize = 11.sp, lineHeight = 14.sp),
            color = if (lit) colors.ink2 else colors.sage,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 7.dp),
        )
    }
}

/** The line between two nodes, with a chevron pointing along the reading direction. */
@Composable
private fun FlowConnector(lit: Boolean, modifier: Modifier = Modifier) {
    val colors = LocalHimaColors.current
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val tint by animateColorAsState(
        targetValue = if (lit) colors.green else colors.divider,
        animationSpec = tween(320),
        label = "economyConnector",
    )

    Canvas(modifier.height(NODE_SIZE_DP.dp)) {
        val y = 1.dp.toPx()
        val stroke = 2.dp.toPx()
        drawLine(
            color = tint,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        // Raw canvas pixels don't mirror with the layout, so point the chevron
        // toward the end edge explicitly: right in LTR, left in RTL.
        val dir = if (isRtl) -1f else 1f
        val tipX = if (isRtl) 0f else size.width
        val arm = 4.dp.toPx()
        drawLine(tint, Offset(tipX, y), Offset(tipX - dir * arm, y - arm), stroke, StrokeCap.Round)
        drawLine(tint, Offset(tipX, y), Offset(tipX - dir * arm, y + arm), stroke, StrokeCap.Round)
    }
}

/** Small line-art glyphs, drawn rather than shipped as assets so they inherit
 *  the theme's colours and scale cleanly at any node size. */
private fun DrawScope.drawStageGlyph(stage: EconomyStage, tint: Color) {
    val w = size.width
    val h = size.height
    val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
    when (stage) {
        // A viewfinder: something has been spotted.
        EconomyStage.DETECTED -> {
            drawCircle(tint, radius = w * 0.38f, style = stroke)
            drawCircle(tint, radius = w * 0.10f)
        }
        // Rising bars: the model weighing it up.
        EconomyStage.ANALYSED -> {
            val base = h * 0.78f
            listOf(0.26f to 0.45f, 0.5f to 0.28f, 0.74f to 0.55f).forEach { (x, top) ->
                drawLine(tint, Offset(w * x, base), Offset(w * x, h * top), stroke.width, StrokeCap.Round)
            }
        }
        // A closed loop: the recycle / reuse / reduce decision.
        EconomyStage.ACTION -> {
            drawCircle(tint, radius = w * 0.34f, style = stroke)
            val tip = Offset(w * 0.5f + w * 0.34f, h * 0.5f)
            drawLine(tint, tip, Offset(tip.x - w * 0.14f, tip.y - h * 0.12f), stroke.width, StrokeCap.Round)
            drawLine(tint, tip, Offset(tip.x - w * 0.14f, tip.y + h * 0.12f), stroke.width, StrokeCap.Round)
        }
        // A leaf: the outcome the loop exists for.
        EconomyStage.IMPACT -> {
            val leaf = Path().apply {
                moveTo(w * 0.5f, h * 0.16f)
                cubicTo(w * 0.94f, h * 0.34f, w * 0.86f, h * 0.82f, w * 0.5f, h * 0.86f)
                cubicTo(w * 0.14f, h * 0.82f, w * 0.06f, h * 0.34f, w * 0.5f, h * 0.16f)
                close()
            }
            drawPath(leaf, tint, style = stroke)
            drawLine(
                tint,
                Offset(w * 0.5f, h * 0.24f),
                Offset(w * 0.5f, h * 0.82f),
                stroke.width * 0.8f,
                StrokeCap.Round,
            )
        }
    }
}
