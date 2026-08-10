package com.hima.ai.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hima.ai.core.designsystem.theme.HimaTextStyles
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

fun severityColors(severity: Severity): SeverityColors = when (severity) {
    Severity.UNKNOWN -> SeverityColors(SeverityLowBg, SeverityLowFg)
    Severity.LOW -> SeverityColors(SeverityLowBg, SeverityLowFg)
    Severity.MEDIUM -> SeverityColors(SeverityMidBg, SeverityMidFg)
    Severity.HIGH -> SeverityColors(SeverityHighBg, SeverityHighFg)
    Severity.CRITICAL -> SeverityColors(SeverityCriticalBg, SeverityCriticalFg)
}

/**
 * A labelled severity pill — the app's only use of colour to encode meaning.
 * Tinted rather than saturated so it stays readable outdoors without shouting.
 */
@Composable
fun SeverityBadge(severity: Severity, modifier: Modifier = Modifier) {
    val colors = severityColors(severity)
    Text(
        text = stringResource(severity.labelRes),
        style = HimaTextStyles.m.copy(fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold),
        color = colors.foreground,
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(colors.background)
            .padding(horizontal = 11.dp, vertical = 5.dp),
    )
}
