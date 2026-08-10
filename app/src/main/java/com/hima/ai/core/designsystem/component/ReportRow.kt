package com.hima.ai.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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

/** A premium real-report card reused unchanged by Home and History. */
@Composable
fun ReportRow(
    report: ReportSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onViewOnMapClick: (() -> Unit)? = null,
) {
    val colors = LocalHimaColors.current
    val title = report.titleOverride ?: stringResource(report.titleRes)
    val location = report.locationOverride ?: stringResource(report.locationRes)
    val time = report.createdAt?.let { relativeTimeLabel(it) } ?: stringResource(report.timeRes)
    val showSeverity = report.category != IncidentCategory.WASTE && report.severity != Severity.UNKNOWN

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .shadow(
                elevation = if (colors.isDark) 1.dp else 5.dp,
                shape = RoundedCornerShape(HimaRadius.card),
                clip = false,
                ambientColor = colors.green.copy(alpha = 0.12f),
                spotColor = colors.green.copy(alpha = 0.12f),
            )
            .clip(RoundedCornerShape(HimaRadius.card))
            .background(colors.surface)
            .clickable(onClick = onClick)
            .heightIn(min = 102.dp)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        ReportImage(
            imageUrl = report.imageUrl,
            demoImageRes = report.demoImageRes,
            scene = report.scene,
            contentDescription = title,
            modifier = Modifier
                .width(94.dp)
                .height(84.dp)
                .clip(RoundedCornerShape(HimaRadius.thumb)),
        )
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = HimaTextStyles.t.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
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
                modifier = Modifier.padding(top = 4.dp),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier.padding(top = 3.dp),
            ) {
                Icon(
                    painter = painterResource(report.category.iconRes),
                    contentDescription = null,
                    tint = colors.green,
                    modifier = Modifier.size(13.dp),
                )
                Text(
                    text = "${stringResource(report.category.singularLabelRes)} · $time",
                    style = HimaTextStyles.m.copy(fontSize = 11.5.sp),
                    color = colors.sage,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
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
            onViewOnMapClick?.let { onMapClick ->
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(colors.bg2)
                        .clickable(onClick = onMapClick),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_tab_map),
                        contentDescription = stringResource(R.string.cd_view_on_map),
                        tint = colors.green,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

internal enum class ReportImageSource { REMOTE, DEMO, PLACEHOLDER }

/** Remote user evidence always wins; a local photo is eligible only when explicitly attached to demo data. */
internal fun resolveReportImageSource(
    imageUrl: String?,
    @DrawableRes demoImageRes: Int?,
): ReportImageSource = when {
    !imageUrl.isNullOrBlank() -> ReportImageSource.REMOTE
    demoImageRes != null -> ReportImageSource.DEMO
    else -> ReportImageSource.PLACEHOLDER
}

/** Persisted-image renderer with explicit, isolated support for static demo photos. */
@Composable
fun ReportImage(
    imageUrl: String?,
    scene: SceneKind,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    @DrawableRes demoImageRes: Int? = null,
) {
    val colors = LocalHimaColors.current
    Box(modifier.background(colors.warm)) {
        when (resolveReportImageSource(imageUrl, demoImageRes)) {
            ReportImageSource.REMOTE -> {
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
            ReportImageSource.DEMO -> {
                Image(
                    painter = painterResource(requireNotNull(demoImageRes)),
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            ReportImageSource.PLACEHOLDER -> {
                SceneArt(kind = scene, modifier = Modifier.fillMaxSize())
            }
        }
    }
}
