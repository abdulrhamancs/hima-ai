package com.hima.ai.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hima.ai.core.designsystem.theme.HimaRadius
import com.hima.ai.core.designsystem.theme.HimaTextStyles
import com.hima.ai.core.designsystem.theme.LocalHimaColors
import com.hima.ai.domain.model.ReportSummary

/**
 * A single report in a list: scene thumbnail, title, location, time, severity.
 * Deliberately not a card — the thumbnail and badge carry the row, so lists
 * read as one continuous surface instead of a stack of boxes.
 */
@Composable
fun ReportRow(
    report: ReportSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalHimaColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(HimaRadius.field))
            .clickable(onClick = onClick)
            .heightIn(min = 56.dp)
            .padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        Box(
            Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(HimaRadius.thumb))
                .background(colors.warm),
        ) {
            SceneArt(kind = report.scene, modifier = Modifier.fillMaxSize())
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(report.titleRes),
                style = HimaTextStyles.t.copy(fontSize = 15.5.sp, fontWeight = FontWeight.SemiBold),
                color = colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(report.locationRes),
                style = HimaTextStyles.m,
                color = colors.sage,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 3.dp),
            )
            Text(
                text = stringResource(report.timeRes),
                style = HimaTextStyles.m,
                color = colors.sage,
                maxLines = 1,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        SeverityBadge(report.severity)
    }
}
