package com.hima.ai.core.designsystem.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hima.ai.core.designsystem.theme.LocalHimaColors
import com.hima.ai.domain.model.BadgeTier
import kotlin.math.cos
import kotlin.math.sin

/**
 * One tier's metal. [discLight] → [discDark] shade the face, [accent] colours
 * the laurel the upper tiers carry, and [engraving] is the colour the sprout
 * is struck in — a darker shade of the same metal, the way a real medal's
 * relief is the same material as its face rather than a separate colour.
 */
data class MedalPalette(
    val discLight: Color,
    val discDark: Color,
    val accent: Color,
    val engraving: Color,
)

/**
 * Per-tier medal metals. Pioneer reuses the app's own primary-green tokens
 * (it reads as the *start* of the environmental identity, not a metal), and
 * Guardian gets a distinct green-gray so the two never look interchangeable.
 * Every other tier is a hand-picked metal/gem ramp — the one deliberate
 * exception to "no new hardcoded hex values" the task calls for. Shared
 * across light/dark (like the severity ramp in Color.kt) so a badge reads the
 * same regardless of theme.
 */
@Composable
fun BadgeTier.medalPalette(): MedalPalette {
    val colors = LocalHimaColors.current
    return when (this) {
        BadgeTier.PIONEER -> MedalPalette(
            discLight = Color(0xFF6F9070),
            discDark = Color(0xFF3B593F),
            accent = colors.greenDeep,
            engraving = Color(0xFF20301F),
        )
        BadgeTier.BRONZE -> MedalPalette(
            discLight = Color(0xFFE3A972),
            discDark = Color(0xFFA2632F),
            accent = Color(0xFF6E4527),
            engraving = Color(0xFF6B3F1C),
        )
        BadgeTier.SILVER -> MedalPalette(
            discLight = Color(0xFFEDF1F6),
            discDark = Color(0xFF9AA5B3),
            accent = Color(0xFF5F6874),
            engraving = Color(0xFF5C6672),
        )
        BadgeTier.GOLD -> MedalPalette(
            discLight = Color(0xFFFFE070),
            discDark = Color(0xFFD9A213),
            accent = Color(0xFF8C6508),
            engraving = Color(0xFF7A5606),
        )
        BadgeTier.PLATINUM -> MedalPalette(
            discLight = Color(0xFFEFF8F7),
            discDark = Color(0xFFAECBC8),
            accent = Color(0xFF6C8B8A),
            engraving = Color(0xFF547070),
        )
        BadgeTier.DIAMOND -> MedalPalette(
            discLight = Color(0xFFD8F2FA),
            discDark = Color(0xFF74C2DC),
            accent = Color(0xFF3C7F9B),
            engraving = Color(0xFF2E6A83),
        )
        BadgeTier.GUARDIAN -> MedalPalette(
            discLight = Color(0xFFA8C4B2),
            discDark = Color(0xFF54705F),
            accent = Color(0xFF2C3A34),
            engraving = Color(0xFF2C3E33),
        )
        BadgeTier.AMBASSADOR -> MedalPalette(
            discLight = Color(0xFFF7E08F),
            discDark = Color(0xFFC08F1B),
            accent = Color(0xFF0A2A20),
            engraving = Color(0xFF14442F),
        )
    }
}

/** How much regalia a tier's medal carries, escalating with rank. */
private enum class BadgeRegalia { PLAIN, LAUREL }

private val BadgeTier.regalia: BadgeRegalia
    get() = when (this) {
        BadgeTier.PIONEER, BadgeTier.BRONZE, BadgeTier.SILVER, BadgeTier.GOLD -> BadgeRegalia.PLAIN
        BadgeTier.PLATINUM, BadgeTier.DIAMOND, BadgeTier.GUARDIAN, BadgeTier.AMBASSADOR -> BadgeRegalia.LAUREL
    }

/**
 * The reward-ladder icon: a struck medal — a shaded disc of metal with the
 * app's seedling embossed into it — coloured per [tier] via [medalPalette].
 * One parameterised composable rather than eight hand-drawn assets, so adding
 * a ninth tier is a palette entry, not a new icon.
 *
 * Every shape is built symmetric about the medal's own centre line, so nothing
 * needs RTL handling: a shape that mirrors to itself has no reading direction
 * to get wrong.
 *
 * [glowing] adds the "this is your current badge" treatment: a soft pulsing
 * ring behind the medal, scale + alpha only (no blur), on an infinite loop —
 * reserved for the single medal shown in the hero card.
 */
@Composable
fun MedalBadge(
    tier: BadgeTier,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    glowing: Boolean = false,
) {
    val palette = tier.medalPalette()
    val regalia = tier.regalia
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        if (glowing) {
            val infinite = rememberInfiniteTransition(label = "medalGlow")
            val glowScale by infinite.animateFloat(
                initialValue = 1f,
                targetValue = 1.16f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1900, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "medalGlowScale",
            )
            val glowAlpha by infinite.animateFloat(
                initialValue = 0.36f,
                targetValue = 0.07f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1900, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "medalGlowAlpha",
            )
            Box(
                Modifier
                    .matchParentSize()
                    .scale(glowScale)
                    .background(
                        Brush.radialGradient(listOf(palette.discLight.copy(alpha = glowAlpha), Color.Transparent)),
                        CircleShape,
                    ),
            )
        }
        Canvas(Modifier.size(size)) { drawMedal(palette, regalia) }
    }
}

private fun DrawScope.drawMedal(palette: MedalPalette, regalia: BadgeRegalia) {
    val s = size.minDimension
    val cx = size.width / 2f
    val cy = s * 0.50f
    // Sized so the laurel on the upper tiers still lands inside the canvas.
    val r = s * 0.38f

    if (regalia != BadgeRegalia.PLAIN) drawLaurelWreath(cx, cy, r, palette.accent)

    // One clean circle of metal — no contrasting rim ring around it, which
    // read as a hard outline drawn on top rather than as part of the medal.
    drawCircle(
        brush = Brush.linearGradient(
            colors = listOf(palette.discLight, palette.discDark),
            start = Offset(cx - r, cy - r),
            end = Offset(cx + r, cy + r),
        ),
        radius = r,
        center = Offset(cx, cy),
    )
    // Engraved guilloche line just inside the face, the way a struck coin has
    // a border ring around its device.
    drawCircle(
        color = palette.engraving.copy(alpha = 0.24f),
        radius = r * 0.80f,
        center = Offset(cx, cy),
        style = Stroke(width = r * 0.035f),
    )

    drawSeedling(cx, cy, r * 0.64f, palette)

    // Flat sheen — plain translucent shapes at reduced opacity, not blur or
    // glow effects, for a metallic feel per the design brief.
    drawOval(
        color = Color.White.copy(alpha = 0.26f),
        topLeft = Offset(cx - r * 0.62f, cy - r * 0.72f),
        size = Size(r * 0.72f, r * 0.34f),
    )
    drawArc(
        color = Color.White.copy(alpha = 0.30f),
        startAngle = 172f,
        sweepAngle = 74f,
        useCenter = false,
        topLeft = Offset(cx - r * 0.91f, cy - r * 0.91f),
        size = Size(r * 1.82f, r * 1.82f),
        style = Stroke(width = r * 0.09f, cap = StrokeCap.Round),
    )
}

/**
 * The seedling the medal is struck with: a curved stem rising from a soil
 * mound, carrying three leaves. Drawn twice — a light copy offset down-right,
 * then the dark copy — which is what gives the relief its raised edge.
 */
private fun DrawScope.drawSeedling(cx: Float, cy: Float, h: Float, palette: MedalPalette) {
    val bevel = h * 0.055f
    translate(bevel, bevel) {
        drawSeedlingShapes(cx, cy, h, Color.White.copy(alpha = 0.34f))
    }
    drawSeedlingShapes(cx, cy, h, palette.engraving)
}

private fun DrawScope.drawSeedlingShapes(cx: Float, cy: Float, h: Float, tint: Color) {
    // Soil mound: a shallow dome the stem grows out of.
    val soilY = cy + h * 0.86f
    drawPath(
        Path().apply {
            moveTo(cx - h * 0.62f, soilY)
            quadraticTo(cx, soilY - h * 0.42f, cx + h * 0.62f, soilY)
            close()
        },
        tint,
    )

    // Stem: a tapered S-curve from the soil up to the crown of the sprout.
    drawPath(
        Path().apply {
            moveTo(cx - h * 0.11f, soilY)
            cubicTo(
                cx - h * 0.20f, cy + h * 0.20f,
                cx - h * 0.02f, cy - h * 0.32f,
                cx + h * 0.13f, cy - h * 0.86f,
            )
            lineTo(cx + h * 0.24f, cy - h * 0.82f)
            cubicTo(
                cx + h * 0.12f, cy - h * 0.28f,
                cx + h * 0.02f, cy + h * 0.22f,
                cx + h * 0.09f, soilY,
            )
            close()
        },
        tint,
    )

    // Three leaves, alternating up the stem: a big one crowning it, then one
    // out to each side, matching how a real seedling stacks its first leaves.
    drawLeaf(Offset(cx + h * 0.13f, cy - h * 0.50f), Offset(cx + h * 0.86f, cy - h * 1.02f), h * 0.30f, tint)
    drawLeaf(Offset(cx + h * 0.02f, cy - h * 0.30f), Offset(cx - h * 0.84f, cy - h * 0.60f), h * 0.28f, tint)
    drawLeaf(Offset(cx + h * 0.05f, cy + h * 0.16f), Offset(cx + h * 0.88f, cy - h * 0.12f), h * 0.29f, tint)
}

/** A pointed leaf from [base] to [tip], bulging by [halfWidth], with a centre vein cut out. */
private fun DrawScope.drawLeaf(base: Offset, tip: Offset, halfWidth: Float, tint: Color) {
    val dx = tip.x - base.x
    val dy = tip.y - base.y
    val length = kotlin.math.sqrt(dx * dx + dy * dy).takeIf { it > 0f } ?: 1f
    val nx = -dy / length
    val ny = dx / length
    val mid = Offset((base.x + tip.x) / 2f, (base.y + tip.y) / 2f)
    drawPath(
        Path().apply {
            moveTo(base.x, base.y)
            quadraticTo(mid.x + nx * halfWidth, mid.y + ny * halfWidth, tip.x, tip.y)
            quadraticTo(mid.x - nx * halfWidth, mid.y - ny * halfWidth, base.x, base.y)
            close()
        },
        tint,
    )
}

/**
 * A laurel wreath cradling the rim on the upper tiers. Each leaf is anchored
 * on the rim and sweeps *along* the arc rather than straight out from the
 * centre — radial leaves spaced around the whole circle read as a sunburst,
 * not a wreath — and both branches stay on the lower half so the wreath
 * cradles the medal instead of enclosing it.
 */
private fun DrawScope.drawLaurelWreath(cx: Float, cy: Float, r: Float, color: Color) {
    // Screen angles, measured from the +x axis with y running downward: the
    // lower-right quadrant sweeping up toward the rim's shoulder.
    val angles = listOf(104f, 79f, 54f, 29f)
    listOf(-1f, 1f).forEach { dir ->
        angles.forEach { angle ->
            val anchorAngle = Math.toRadians(angle.toDouble())
            val tipAngle = Math.toRadians((angle - 26f).toDouble())
            val anchor = Offset(
                cx + dir * (cos(anchorAngle) * r * 1.00f).toFloat(),
                cy + (sin(anchorAngle) * r * 1.00f).toFloat(),
            )
            val tip = Offset(
                cx + dir * (cos(tipAngle) * r * 1.30f).toFloat(),
                cy + (sin(tipAngle) * r * 1.30f).toFloat(),
            )
            drawLeaf(anchor, tip, r * 0.13f, color)
        }
    }
}

