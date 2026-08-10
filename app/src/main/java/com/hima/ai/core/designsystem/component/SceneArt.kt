package com.hima.ai.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.hima.ai.domain.model.SceneKind

/**
 * Generated scene illustrations standing in for report photography. The
 * prototype ships no photo assets, so each [SceneKind] is drawn from a
 * four-step palette — cheap, resolution-independent, and consistent between a
 * 58dp row thumbnail and a full-width hero.
 */
private data class ScenePalette(val sky1: Color, val sky2: Color, val mid: Color, val fore: Color)

private fun paletteFor(kind: SceneKind): ScenePalette = when (kind) {
    SceneKind.FOREST -> ScenePalette(Color(0xFFB9C2B4), Color(0xFF8D9A8A), Color(0xFF5B6359), Color(0xFF3E4740))
    SceneKind.STUMP -> ScenePalette(Color(0xFFD7CEBE), Color(0xFFB6A98F), Color(0xFF8A7B60), Color(0xFF5C513E))
    SceneKind.FIRE -> ScenePalette(Color(0xFFE7D9C4), Color(0xFFD9A06A), Color(0xFFC0603A), Color(0xFF8A3A24))
    SceneKind.WATER -> ScenePalette(Color(0xFFCFD8D6), Color(0xFF9FB3B2), Color(0xFF6E8586), Color(0xFF4A5C5E))
    SceneKind.WASTE -> ScenePalette(Color(0xFFE8ECE5), Color(0xFFD4DDD1), Color(0xFF91A38D), Color(0xFF315B45))
    SceneKind.VALLEY -> ScenePalette(Color(0xFFC6CFC4), Color(0xFF96A594), Color(0xFF63715F), Color(0xFF3C463A))
}

@Composable
fun SceneArt(kind: SceneKind, modifier: Modifier = Modifier) {
    val p = paletteFor(kind)
    Canvas(modifier) {
        drawRect(Brush.verticalGradient(listOf(p.sky1, p.sky2)))
        when (kind) {
            SceneKind.FIRE -> drawFire(p)
            SceneKind.WATER -> drawWater(p)
            SceneKind.WASTE -> drawWaste(p)
            SceneKind.STUMP -> drawStump(p)
            SceneKind.FOREST, SceneKind.VALLEY -> drawTrees(p)
        }
    }
}

private fun DrawScope.drawWaste(p: ScenePalette) {
    val w = size.width
    val h = size.height
    drawCircle(p.mid, radius = size.minDimension * 0.28f, center = Offset(w * 0.50f, h * 0.53f), alpha = 0.16f)
    val strokeWidth = size.minDimension * 0.055f
    drawArc(
        color = p.fore,
        startAngle = 205f,
        sweepAngle = 125f,
        useCenter = false,
        topLeft = Offset(w * 0.30f, h * 0.31f),
        size = Size(w * 0.40f, h * 0.40f),
        style = Stroke(width = strokeWidth),
    )
    drawArc(
        color = p.fore,
        startAngle = 25f,
        sweepAngle = 125f,
        useCenter = false,
        topLeft = Offset(w * 0.30f, h * 0.31f),
        size = Size(w * 0.40f, h * 0.40f),
        style = Stroke(width = strokeWidth),
    )
    val topArrow = Path().apply {
        moveTo(w * 0.69f, h * 0.42f)
        lineTo(w * 0.60f, h * 0.39f)
        lineTo(w * 0.66f, h * 0.49f)
        close()
    }
    val bottomArrow = Path().apply {
        moveTo(w * 0.31f, h * 0.64f)
        lineTo(w * 0.40f, h * 0.67f)
        lineTo(w * 0.34f, h * 0.57f)
        close()
    }
    drawPath(topArrow, p.fore)
    drawPath(bottomArrow, p.fore)
}

private fun DrawScope.ridge(from: Float, color: Color, alpha: Float = 1f) {
    val w = size.width
    val h = size.height
    val path = Path().apply {
        moveTo(0f, h * from)
        cubicTo(w * 0.22f, h * (from - 0.10f), w * 0.42f, h * (from + 0.06f), w * 0.62f, h * (from - 0.03f))
        cubicTo(w * 0.80f, h * (from - 0.10f), w * 0.92f, h * (from + 0.04f), w, h * from)
        lineTo(w, h); lineTo(0f, h); close()
    }
    drawPath(path, color, alpha = alpha)
}

private fun DrawScope.tree(cx: Float, baseY: Float, scale: Float, color: Color) {
    val w = size.width
    val s = w * 0.10f * scale
    val top = Path().apply {
        moveTo(cx, baseY - s * 2.6f)
        lineTo(cx - s * 0.78f, baseY - s * 1.15f)
        lineTo(cx + s * 0.78f, baseY - s * 1.15f)
        close()
    }
    val bottom = Path().apply {
        moveTo(cx, baseY - s * 1.9f)
        lineTo(cx - s * 1.05f, baseY)
        lineTo(cx + s * 1.05f, baseY)
        close()
    }
    drawPath(top, color)
    drawPath(bottom, color)
}

private fun DrawScope.drawTrees(p: ScenePalette) {
    ridge(0.55f, p.mid, alpha = 0.85f)
    val h = size.height
    val w = size.width
    tree(w * 0.20f, h * 0.72f, 1.0f, p.fore)
    tree(w * 0.44f, h * 0.66f, 1.25f, p.fore)
    tree(w * 0.68f, h * 0.74f, 0.95f, p.fore)
    tree(w * 0.86f, h * 0.70f, 1.1f, p.fore)
    ridge(0.84f, p.fore)
}

private fun DrawScope.drawStump(p: ScenePalette) {
    val w = size.width
    val h = size.height
    ridge(0.70f, p.mid)
    // Cut stump: elliptical top with growth rings, plus a felled log.
    val cx = w * 0.50f
    val cy = h * 0.60f
    val rx = w * 0.22f
    val ry = h * 0.075f
    drawOval(p.fore, topLeft = Offset(cx - rx, cy - ry + h * 0.03f), size = Size(rx * 2, ry * 2))
    drawOval(p.sky2, topLeft = Offset(cx - rx, cy - ry), size = Size(rx * 2, ry * 2))
    drawOval(
        p.fore,
        topLeft = Offset(cx - rx * 0.6f, cy - ry * 0.6f),
        size = Size(rx * 1.2f, ry * 1.2f),
        style = Stroke(width = size.minDimension * 0.012f),
    )
    drawRect(p.mid, topLeft = Offset(cx - rx, cy), size = Size(rx * 2, h * 0.16f))
    val log = Path().apply {
        moveTo(w * 0.04f, h * 0.86f)
        lineTo(w * 0.40f, h * 0.78f)
        lineTo(w * 0.44f, h * 0.86f)
        lineTo(w * 0.06f, h * 0.95f)
        close()
    }
    drawPath(log, p.fore)
}

private fun DrawScope.drawFire(p: ScenePalette) {
    val w = size.width
    val h = size.height
    ridge(0.66f, p.mid, alpha = 0.65f)
    drawCircle(p.mid, radius = w * 0.30f, center = Offset(w * 0.52f, h * 0.70f), alpha = 0.45f)
    val flame = Path().apply {
        moveTo(w * 0.52f, h * 0.40f)
        cubicTo(w * 0.70f, h * 0.56f, w * 0.76f, h * 0.64f, w * 0.76f, h * 0.74f)
        cubicTo(w * 0.76f, h * 0.90f, w * 0.28f, h * 0.90f, w * 0.28f, h * 0.74f)
        cubicTo(w * 0.28f, h * 0.64f, w * 0.34f, h * 0.56f, w * 0.52f, h * 0.40f)
        close()
    }
    drawPath(flame, p.fore)
    val inner = Path().apply {
        moveTo(w * 0.52f, h * 0.56f)
        cubicTo(w * 0.62f, h * 0.66f, w * 0.65f, h * 0.70f, w * 0.65f, h * 0.77f)
        cubicTo(w * 0.65f, h * 0.86f, w * 0.39f, h * 0.86f, w * 0.39f, h * 0.77f)
        cubicTo(w * 0.39f, h * 0.70f, w * 0.42f, h * 0.66f, w * 0.52f, h * 0.56f)
        close()
    }
    drawPath(inner, p.sky2)
}

private fun DrawScope.drawWater(p: ScenePalette) {
    val w = size.width
    val h = size.height
    ridge(0.58f, p.mid)
    val wave = Path().apply {
        moveTo(0f, h * 0.76f)
        cubicTo(w * 0.25f, h * 0.68f, w * 0.45f, h * 0.84f, w * 0.68f, h * 0.76f)
        cubicTo(w * 0.85f, h * 0.70f, w * 0.94f, h * 0.80f, w, h * 0.76f)
        lineTo(w, h); lineTo(0f, h); close()
    }
    drawPath(wave, p.fore)
    val stroke = Stroke(width = size.minDimension * 0.022f)
    drawLine(p.sky1, Offset(w * 0.15f, h * 0.70f), Offset(w * 0.36f, h * 0.70f), strokeWidth = stroke.width, alpha = 0.55f)
    drawLine(p.sky1, Offset(w * 0.52f, h * 0.82f), Offset(w * 0.76f, h * 0.82f), strokeWidth = stroke.width, alpha = 0.55f)
    drawLine(p.sky1, Offset(w * 0.28f, h * 0.90f), Offset(w * 0.55f, h * 0.90f), strokeWidth = stroke.width, alpha = 0.55f)
}
