package com.hima.ai.presentation.map

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import com.hima.ai.core.designsystem.theme.LocalHimaColors

/**
 * A generated top-down reserve map — boundary, elevation contours, and a wadi
 * — drawn the same way [com.hima.ai.core.designsystem.component.SceneArt]
 * draws report imagery: cheap, resolution-independent, and in the app's own
 * muted palette rather than photographic map tiles.
 */
@Composable
fun ReserveTerrain(modifier: Modifier = Modifier) {
    val colors = LocalHimaColors.current
    Canvas(modifier) {
        val w = size.width
        val h = size.height

        drawRect(colors.bg2)

        // Soft high-risk zone glow, away from any marker so it reads as terrain, not chrome.
        drawCircle(
            color = colors.severityCritical.copy(alpha = 0.07f),
            radius = w * 0.30f,
            center = Offset(w * 0.60f, h * 0.32f),
        )

        // Elevation contours — concentric, slightly irregular loops.
        val contourColor = colors.beige.copy(alpha = 0.55f)
        listOf(0.9f, 0.72f, 0.54f).forEachIndexed { i, scale ->
            val cx = w * 0.40f
            val cy = h * 0.38f
            val rx = w * 0.34f * scale
            val ry = h * 0.24f * scale
            val path = Path().apply {
                moveTo(cx, cy - ry)
                cubicTo(cx + rx * 1.05f, cy - ry * 0.9f, cx + rx * 1.1f, cy + ry * 0.5f, cx + rx * 0.3f, cy + ry)
                cubicTo(cx - rx * 0.6f, cy + ry * 1.1f, cx - rx * 1.1f, cy + ry * 0.2f, cx - rx * 0.9f, cy - ry * 0.5f)
                cubicTo(cx - rx * 0.7f, cy - ry * 1.05f, cx - rx * 0.2f, cy - ry * 1.1f, cx, cy - ry)
                close()
            }
            drawPath(path, contourColor, style = Stroke(width = (1.2f + i * 0.2f)))
        }

        // The wadi — a winding stream crossing the reserve.
        val wadi = Path().apply {
            moveTo(w * 0.08f, h * 0.18f)
            cubicTo(w * 0.20f, h * 0.30f, w * 0.16f, h * 0.44f, w * 0.30f, h * 0.52f)
            cubicTo(w * 0.44f, h * 0.60f, w * 0.42f, h * 0.72f, w * 0.55f, h * 0.80f)
            cubicTo(w * 0.64f, h * 0.86f, w * 0.72f, h * 0.90f, w * 0.86f, h * 0.94f)
        }
        drawPath(wadi, colors.sage.copy(alpha = 0.5f), style = Stroke(width = size.minDimension * 0.012f))

        // Reserve boundary — a large dashed outline, the protected-area convention.
        val boundary = Path().apply {
            moveTo(w * 0.10f, h * 0.14f)
            cubicTo(w * 0.30f, h * 0.02f, w * 0.68f, h * 0.02f, w * 0.90f, h * 0.16f)
            cubicTo(w * 0.98f, h * 0.34f, w * 0.94f, h * 0.60f, w * 0.90f, h * 0.82f)
            cubicTo(w * 0.78f, h * 0.98f, w * 0.30f, h * 0.98f, w * 0.12f, h * 0.86f)
            cubicTo(w * 0.02f, h * 0.66f, w * 0.02f, h * 0.32f, w * 0.10f, h * 0.14f)
            close()
        }
        drawPath(
            boundary,
            color = colors.green.copy(alpha = 0.45f),
            style = Stroke(
                width = 1.6f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)),
            ),
        )

        // A scattering of low, muted vegetation patches for texture.
        val patch = colors.warm.copy(alpha = 0.7f)
        listOf(
            Offset(w * 0.18f, h * 0.60f) to w * 0.05f,
            Offset(w * 0.62f, h * 0.72f) to w * 0.06f,
            Offset(w * 0.78f, h * 0.44f) to w * 0.04f,
            Offset(w * 0.30f, h * 0.22f) to w * 0.045f,
        ).forEach { (center, radius) ->
            drawCircle(patch, radius = radius, center = center)
        }
    }
}
