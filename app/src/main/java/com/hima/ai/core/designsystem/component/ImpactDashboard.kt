package com.hima.ai.core.designsystem.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hima.ai.core.designsystem.theme.HimaTextStyles
import com.hima.ai.core.designsystem.theme.Inter
import com.hima.ai.core.designsystem.theme.LocalHimaColors
import java.text.NumberFormat
import java.util.Locale

/**
 * A number that counts up to [value] the first time it appears, then tracks any
 * later changes directly.
 *
 * Digits are grouped with [Locale.US] on purpose rather than the device locale:
 * every other figure in the app (coordinates, percentages, stat tiles) already
 * renders in Latin digits, and ICU would switch Arabic to Arabic-Indic numerals,
 * leaving the same screen showing two different numeral systems at once. Swap
 * the locale here if full numeral localisation is ever wanted app-wide.
 */
@Composable
fun AnimatedCount(
    value: Int,
    modifier: Modifier = Modifier,
    durationMs: Int = 900,
    style: androidx.compose.ui.text.TextStyle = HimaTextStyles.num.copy(
        fontFamily = Inter,
        fontSize = 21.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    color: Color = LocalHimaColors.current.ink,
) {
    // Start from zero so the first paint animates; without this the initial
    // composition would already hold the target and never move.
    var target by remember { mutableStateOf(0) }
    LaunchedEffect(value) { target = value }
    val animated by animateIntAsState(
        targetValue = target,
        animationSpec = tween(durationMs, easing = FastOutSlowInEasing),
        label = "animatedCount",
    )
    val formatter = remember { NumberFormat.getIntegerInstance(Locale.US) }
    Text(text = formatter.format(animated), style = style, color = color, modifier = modifier)
}

/** One wedge of [ImpactDonut], with the label shown in its legend. */
data class ImpactSegment(val label: String, val value: Int, val color: Color)

/**
 * A donut breakdown that grows its arcs from zero on appearance rather than
 * snapping to full size.
 *
 * Layout direction is handled structurally: the chart and its legend sit in a
 * [Row], and each legend entry is itself a [Row], so Compose mirrors both under
 * RTL — the legend lands on the correct side in Arabic and each swatch stays on
 * the reading-start edge of its label without any manual flipping.
 */
@Composable
fun ImpactDonut(
    segments: List<ImpactSegment>,
    modifier: Modifier = Modifier,
    diameter: Int = 116,
) {
    val colors = LocalHimaColors.current
    val total = segments.sumOf { it.value }.coerceAtLeast(1)

    var play by remember { mutableStateOf(false) }
    LaunchedEffect(segments) { play = true }
    val progress by animateFloatAsState(
        targetValue = if (play) 1f else 0f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "donutSweep",
    )

    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(diameter.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(diameter.dp)) {
                val thickness = 14.dp.toPx()
                val inset = thickness / 2f
                val arcSize = Size(size.width - thickness, size.height - thickness)
                val topLeft = Offset(inset, inset)

                // Track, so an empty or partial ring still reads as a ring.
                drawArc(
                    color = colors.bg2,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = thickness),
                )

                var start = -90f
                segments.forEach { segment ->
                    val sweep = 360f * (segment.value.toFloat() / total) * progress
                    if (sweep > 0f) {
                        drawArc(
                            color = segment.color,
                            startAngle = start,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = thickness, cap = StrokeCap.Butt),
                        )
                    }
                    start += 360f * (segment.value.toFloat() / total) * progress
                }
            }
            AnimatedCount(
                value = segments.sumOf { it.value },
                style = HimaTextStyles.num.copy(
                    fontFamily = Inter,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
        }

        Spacer(Modifier.width(18.dp))

        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            segments.forEach { segment ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(segment.color),
                    )
                    Text(
                        text = segment.label,
                        style = HimaTextStyles.b.copy(fontSize = 13.sp),
                        color = colors.ink2,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                    Text(
                        text = segment.value.toString(),
                        style = HimaTextStyles.num.copy(fontFamily = Inter, fontSize = 13.sp),
                        color = colors.sage,
                        modifier = Modifier.padding(start = 6.dp),
                    )
                }
            }
        }
    }
}
