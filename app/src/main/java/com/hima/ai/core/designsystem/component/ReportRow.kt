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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.hima.ai.R
import com.hima.ai.core.common.relativeTimeLabel
import com.hima.ai.core.designsystem.theme.HimaRadius
import com.hima.ai.core.designsystem.theme.HimaTextStyles
import com.hima.ai.core.designsystem.theme.LocalHimaColors
import com.hima.ai.domain.model.IncidentCategory
import com.hima.ai.domain.model.ReportStatus
import com.hima.ai.domain.model.ReportSummary
import com.hima.ai.domain.model.SceneKind
import com.hima.ai.domain.model.Severity

/** A lightweight real-report row reused unchanged by Home and History. */
@Composable
fun ReportRow(
    report: ReportSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalHimaColors.current
    val title = report.titleOverride ?: stringResource(report.titleRes)
    val location = report.locationOverride ?: stringResource(report.locationRes)
    val time = report.createdAt?.let { relativeTimeLabel(it) } ?: stringResource(report.timeRes)
    val showSeverity = report.category != IncidentCategory.WASTE && report.severity != Severity.UNKNOWN

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(HimaRadius.field))
            .clickable(onClick = onClick)
            .heightIn(min = 74.dp)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        ReportImage(
            imageUrl = report.imageUrl,
            scene = report.scene,
            contentDescription = title,
            modifier = Modifier
                .size(62.dp)
                .clip(RoundedCornerShape(HimaRadius.thumb)),
        )
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(report.category.iconRes),
                    contentDescription = null,
                    tint = colors.green,
                    modifier = Modifier
                        .padding(end = 7.dp)
                        .size(14.dp),
                )
                Text(
                    text = title,
                    style = HimaTextStyles.t.copy(fontSize = 15.5.sp, fontWeight = FontWeight.SemiBold),
                    color = colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = location,
                style = HimaTextStyles.m,
                color = colors.sage,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 3.dp),
            )
            Text(
                text = "${stringResource(report.category.singularLabelRes)} · $time",
                style = HimaTextStyles.m.copy(fontSize = 11.5.sp),
                color = colors.sage,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            if (showSeverity) SeverityBadge(report.severity)
            Text(
                text = stringResource(
                    when (report.status) {
                        ReportStatus.OPEN -> R.string.history_filter_open
                        ReportStatus.RESOLVED -> R.string.history_filter_done
                        ReportStatus.UNKNOWN -> R.string.report_status_unknown
                    },
                ),
                style = HimaTextStyles.m.copy(fontSize = 10.5.sp),
                color = colors.sage,
                modifier = Modifier.padding(top = if (showSeverity) 5.dp else 0.dp),
            )
        }
    }
}

/** Persisted-image renderer with the existing scene art as loading/error fallback. */
@Composable
fun ReportImage(
    imageUrl: String?,
    scene: SceneKind,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    val colors = LocalHimaColors.current
    Box(modifier.background(colors.warm)) {
        if (imageUrl.isNullOrBlank()) {
            SceneArt(kind = scene, modifier = Modifier.fillMaxSize())
        } else {
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                loading = { SceneArt(kind = scene, modifier = Modifier.fillMaxSize()) },
                error = { SceneArt(kind = scene, modifier = Modifier.fillMaxSize()) },
                success = { SubcomposeAsyncImageContent() },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
