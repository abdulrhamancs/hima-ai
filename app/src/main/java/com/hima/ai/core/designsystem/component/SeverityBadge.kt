package com.hima.ai.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hima.ai.core.designsystem.theme.HimaTextStyles
import com.hima.ai.core.designsystem.theme.DarkSeverityCriticalBg
import com.hima.ai.core.designsystem.theme.DarkSeverityCriticalFg
import com.hima.ai.core.designsystem.theme.DarkSeverityHighBg
import com.hima.ai.core.designsystem.theme.DarkSeverityHighFg
import com.hima.ai.core.designsystem.theme.DarkSeverityLowBg
import com.hima.ai.core.designsystem.theme.DarkSeverityLowFg
import com.hima.ai.core.designsystem.theme.DarkSeverityMidBg
import com.hima.ai.core.designsystem.theme.DarkSeverityMidFg
import com.hima.ai.core.designsystem.theme.LocalHimaColors
import com.hima.ai.core.designsystem.theme.SeverityCriticalBg
import com.hima.ai.core.designsystem.theme.SeverityCriticalFg
import com.hima.ai.core.designsystem.theme.SeverityHighBg
import com.hima.ai.core.designsystem.theme.SeverityHighFg
import com.hima.ai.core.designsystem.theme.SeverityLowBg
import com.hima.ai.core.designsystem.theme.SeverityLowFg
import com.hima.ai.core.designsystem.theme.SeverityMidBg
import com.hima.ai.core.designsystem.theme.SeverityMidFg
import com.hima.ai.domain.model.Severity

/** Background/foreground tint pair for a severity level. */
data class SeverityColors(val background: Color, val foreground: Color)

fun severityColors(severity: Severity, darkTheme: Boolean = false): SeverityColors = when (severity) {
    Severity.UNKNOWN, Severity.LOW -> if (darkTheme) {
        SeverityColors(DarkSeverityLowBg, DarkSeverityLowFg)
    } else {
        SeverityColors(SeverityLowBg, SeverityLowFg)
    }
    Severity.MEDIUM -> if (darkTheme) {
        SeverityColors(DarkSeverityMidBg, DarkSeverityMidFg)
    } else {
        SeverityColors(SeverityMidBg, SeverityMidFg)
    }
    Severity.HIGH -> if (darkTheme) {
        SeverityColors(DarkSeverityHighBg, DarkSeverityHighFg)
    } else {
        SeverityColors(SeverityHighBg, SeverityHighFg)
    }
    Severity.CRITICAL -> if (darkTheme) {
        SeverityColors(DarkSeverityCriticalBg, DarkSeverityCriticalFg)
    } else {
        SeverityColors(SeverityCriticalBg, SeverityCriticalFg)
    }
}

/**
 * A labelled severity pill. Tinted rather than saturated so it stays readable
 * outdoors without shouting.
 *
 * The leading glyph changes shape as well as colour — circle, square, triangle,
 * diamond as severity climbs — so the level survives being seen by a colourblind
 * ranger, printed in mono, or glanced at in bright sun. Colour alone never
 * carries the meaning.
 */
@Composable
fun SeverityBadge(severity: Severity, modifier: Modifier = Modifier) {
    val themeColors = LocalHimaColors.current
    val colors = severityColors(severity, darkTheme = themeColors.isDark)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(colors.background)
            .padding(horizontal = 11.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Canvas(Modifier.size(7.dp)) { drawSeverityGlyph(severity, colors.foreground) }
        Text(
            text = stringResource(severity.labelRes),
            style = HimaTextStyles.m.copy(fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold),
            color = colors.foreground,
        )
    }
}

/** Severity encoded as shape, so it reads without relying on hue. */
private fun DrawScope.drawSeverityGlyph(severity: Severity, tint: Color) {
    val s = size.minDimension
    when (severity) {
        Severity.UNKNOWN, Severity.LOW -> drawCircle(tint, radius = s / 2f)
        Severity.MEDIUM -> drawRect(tint, topLeft = Offset.Zero, size = Size(s, s))
        Severity.HIGH -> drawPath(
            Path().apply {
                moveTo(s / 2f, 0f)
                lineTo(s, s)
                lineTo(0f, s)
                close()
            },
            tint,
        )
        Severity.CRITICAL -> drawPath(
            Path().apply {
                moveTo(s / 2f, 0f)
                lineTo(s, s / 2f)
                lineTo(s / 2f, s)
                lineTo(0f, s / 2f)
                close()
            },
            tint,
        )
    }
}
