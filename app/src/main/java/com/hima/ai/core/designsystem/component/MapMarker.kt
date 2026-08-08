package com.hima.ai.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hima.ai.core.designsystem.theme.HimaTextStyles
import com.hima.ai.core.designsystem.theme.Inter
import com.hima.ai.core.designsystem.theme.LocalHimaColors
import com.hima.ai.domain.model.IncidentCategory
import com.hima.ai.domain.model.Severity

/**
 * An incident marker: the category glyph on a solid severity-coloured badge —
 * the same severity palette as [SeverityBadge], just at full saturation so it
 * reads against the map's muted terrain. Critical incidents pulse gently to
 * draw the eye without needing extra colour.
 */
@Composable
fun MapMarkerPin(
    category: IncidentCategory,
    severity: Severity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
) {
    val markerColor = severityMarkerColor(severity)
    val size = if (selected) 40.dp else 34.dp

    Box(modifier = modifier.size(48.dp), contentAlignment = Alignment.Center) {
        if (severity == Severity.CRITICAL) {
            PulseRing(color = markerColor, diameter = size)
        }
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(Color.White)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(size - 4.dp)
                    .clip(CircleShape)
                    .background(markerColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(category.iconRes),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(if (selected) 19.dp else 16.dp),
                )
            }
        }
    }
}

/** The full-saturation severity colour used on the map (vs. [SeverityBadge]'s pale tint). */
@Composable
fun severityMarkerColor(severity: Severity): Color {
    val colors = LocalHimaColors.current
    return when (severity) {
        Severity.LOW -> colors.severityLow
        Severity.MEDIUM -> colors.severityMid
        Severity.HIGH -> colors.severityHigh
        Severity.CRITICAL -> colors.severityCritical
    }
}

/**
 * A merged group of nearby incidents, shown instead of overlapping pins when
 * zoomed out. Tinted by the most severe incident in the group, so a cluster
 * hiding a critical report still reads as urgent. Tapping is expected to zoom
 * in on the group rather than open a report directly.
 */
@Composable
fun MapClusterMarker(
    count: Int,
    severity: Severity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val markerColor = severityMarkerColor(severity)
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(markerColor)
            .border(2.dp, Color.White, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = count.toString(),
            style = HimaTextStyles.num.copy(fontFamily = Inter, fontSize = 15.sp, fontWeight = FontWeight.Bold),
            color = Color.White,
        )
    }
}

@Composable
private fun PulseRing(color: Color, diameter: Dp) {
    val transition = rememberInfiniteTransition(label = "markerPulse")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "pulseProgress",
    )
    Box(
        modifier = Modifier
            .size(diameter)
            .scale(1f + progress * 1.1f)
            .alpha((1f - progress) * 0.5f)
            .clip(CircleShape)
            .background(color),
    )
}

/**
 * The ranger's own position — a green dot with a soft breathing halo, distinct
 * from incident markers so it's never mistaken for a report.
 */
@Composable
fun CurrentLocationMarker(modifier: Modifier = Modifier) {
    val colors = LocalHimaColors.current
    val transition = rememberInfiniteTransition(label = "locationPulse")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "locationPulseProgress",
    )
    Box(modifier = modifier.size(40.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(40.dp)
                .scale(0.5f + progress * 0.9f)
                .alpha((1f - progress) * 0.4f)
                .clip(CircleShape)
                .background(colors.green),
        )
        Box(
            Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(colors.green),
            )
        }
    }
}
