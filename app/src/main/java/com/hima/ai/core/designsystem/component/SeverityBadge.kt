package com.hima.ai.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
 * A labelled severity pill — the app's only use of colour to encode meaning.
 * Tinted rather than saturated so it stays readable outdoors without shouting.
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
        Box(
            Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(colors.foreground),
        )
        Text(
            text = stringResource(severity.labelRes),
            style = HimaTextStyles.m.copy(fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold),
            color = colors.foreground,
        )
    }
}
