package com.hima.ai.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hima.ai.core.designsystem.theme.LocalHimaColors

/**
 * The app's own progress ring — a sweeping arc on a warm track, rather than a
 * stock Material spinner, so the AI step reads as part of the product.
 */
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    diameter: Dp = 54.dp,
    strokeWidth: Dp = 5.dp,
) {
    val colors = LocalHimaColors.current
    val transition = rememberInfiniteTransition(label = "loadingRing")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(1400, easing = LinearEasing)),
        label = "ringSweep",
    )

    Canvas(modifier = modifier.size(diameter).rotate(angle)) {
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        val inset = strokeWidth.toPx() / 2f
        val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
        drawArc(
            color = colors.warm,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = stroke,
        )
        drawArc(
            color = colors.green,
            startAngle = -90f,
            sweepAngle = 96f,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = stroke,
        )
    }
}

/** A thin determinate bar, used for the AI confidence readout. */
@Composable
fun ConfidenceBar(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val colors = LocalHimaColors.current
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(900),
        label = "confidence",
    )
    Box(
        modifier
            .fillMaxWidth()
            .height(7.dp)
            .clip(RoundedCornerShape(50))
            .background(colors.warm),
    ) {
        Box(
            Modifier
                .fillMaxWidth(animated)
                .height(7.dp)
                .clip(RoundedCornerShape(50))
                .background(colors.green),
        )
    }
}
