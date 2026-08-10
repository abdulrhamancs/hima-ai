package com.hima.ai.presentation.report.detail

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hima.ai.R
import com.hima.ai.core.common.relativeTimeLabel
import com.hima.ai.core.designsystem.component.ConfidenceBar
import com.hima.ai.core.designsystem.component.HimaDivider
import com.hima.ai.core.designsystem.component.HimaPrimaryButton
import com.hima.ai.core.designsystem.component.HimaSecondaryButton
import com.hima.ai.core.designsystem.component.HimaTextLink
import com.hima.ai.core.designsystem.component.KeyValueRow
import com.hima.ai.core.designsystem.component.ReportImage
import com.hima.ai.core.designsystem.component.ScreenHeader
import com.hima.ai.core.designsystem.component.SeverityBadge
import com.hima.ai.core.designsystem.theme.HimaRadius
import com.hima.ai.core.designsystem.theme.HimaTextStyles
import com.hima.ai.core.designsystem.theme.Inter
import com.hima.ai.core.designsystem.theme.LocalHimaColors
import com.hima.ai.domain.model.AnalysisResultCategory
import com.hima.ai.domain.model.IncidentCategory
import com.hima.ai.domain.model.ReportStatus
import com.hima.ai.domain.model.ReportSummary
import com.hima.ai.domain.model.Severity

/** Polished detail for the same persisted report object shown in lists and on the map. */
@Composable
fun ReportDetailScreen(
    onBackClick: () -> Unit,
    onInvestigateClick: () -> Unit,
    onViewOnMapClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReportDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalHimaColors.current

    Column(modifier = modifier.fillMaxSize().background(colors.bg)) {
        Spacer(Modifier.height(50.dp))
        ScreenHeader(
            title = stringResource(R.string.report_title),
            onBackClick = onBackClick,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        val report = uiState.report
        when {
            report != null -> ReportContent(
                report = report,
                onInvestigateClick = onInvestigateClick,
                onViewOnMapClick = { onViewOnMapClick(report.id) },
                modifier = Modifier.weight(1f),
            )
            uiState.isLoading -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.green, modifier = Modifier.size(28.dp))
            }
            else -> DetailState(
                text = stringResource(
                    if (uiState.notFound) R.string.report_not_found else R.string.reports_load_error,
                ),
                onRetry = if (uiState.hasError) viewModel::onRetry else null,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ReportContent(
    report: ReportSummary,
    onInvestigateClick: () -> Unit,
    onViewOnMapClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalHimaColors.current
    val context = LocalContext.current
    val analysis = report.analysis
    val isWaste = report.category == IncidentCategory.WASTE ||
        analysis?.category == AnalysisResultCategory.RECYCLABLE_WASTE
    val title = report.titleOverride ?: stringResource(report.titleRes)
    val location = report.locationOverride ?: stringResource(report.locationRes)
    val time = report.createdAt?.let { relativeTimeLabel(it) } ?: stringResource(report.timeRes)
    val status = stringResource(
        when (report.status) {
            ReportStatus.OPEN -> R.string.history_filter_open
            ReportStatus.RESOLVED -> R.string.history_filter_done
            ReportStatus.UNKNOWN -> R.string.report_status_unknown
        },
    )
    val shareText = if (isWaste) {
        stringResource(R.string.circular_report_share_text, title, report.recommendationOverride.orEmpty())
    } else {
        stringResource(
            R.string.report_share_text,
            title,
            location,
            time,
            report.riskScore?.let { "$it / 100" }.orEmpty(),
        )
    }

    fun shareReport() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        ReportImage(
            imageUrl = report.imageUrl,
            demoImageRes = report.demoImageRes,
            scene = report.scene,
            contentDescription = title,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(HimaRadius.hero)),
        )

        Row(
            modifier = Modifier.padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                painter = painterResource(report.category.iconRes),
                contentDescription = null,
                tint = colors.green,
                modifier = Modifier.size(17.dp),
            )
            Text(
                text = stringResource(report.category.singularLabelRes),
                style = HimaTextStyles.t.copy(fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold),
                color = colors.green,
            )
            Text(text = "·", style = HimaTextStyles.m, color = colors.beige)
            Text(text = status, style = HimaTextStyles.m, color = colors.sage)
        }

        Text(
            text = title,
            style = HimaTextStyles.h2.copy(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
            color = colors.ink,
            modifier = Modifier.padding(top = 8.dp),
        )

        Row(
            modifier = Modifier.padding(top = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_field_pin),
                contentDescription = null,
                tint = colors.sage,
                modifier = Modifier.size(14.dp),
            )
            Text(text = location, style = HimaTextStyles.m, color = colors.sage)
            Text(text = "·", style = HimaTextStyles.m, color = colors.beige)
            Text(text = time, style = HimaTextStyles.m, color = colors.sage)
        }

        Column(Modifier.padding(top = 18.dp)) {
            if (!isWaste && report.severity != Severity.UNKNOWN) {
                KeyValueRow(
                    label = stringResource(R.string.report_severity),
                    valueContent = { SeverityBadge(report.severity) },
                )
                HimaDivider()
            }
            if (!isWaste && report.riskScore != null) {
                KeyValueRow(
                    label = stringResource(R.string.report_risk_score),
                    value = "\u2066${report.riskScore} / 100\u2069",
                )
                HimaDivider()
            }
            if (isWaste) {
                analysis?.materialCategory?.takeIf { it.isNotBlank() }?.let {
                    KeyValueRow(label = stringResource(R.string.circular_material), value = it)
                    HimaDivider()
                }
                analysis?.reusable?.let {
                    BooleanValueRow(R.string.circular_reusable, it)
                    HimaDivider()
                }
                analysis?.repairable?.let {
                    BooleanValueRow(R.string.circular_repairable, it)
                    HimaDivider()
                }
                analysis?.recyclable?.let {
                    BooleanValueRow(R.string.circular_recyclable, it)
                    HimaDivider()
                }
                analysis?.preferredAction?.let {
                    KeyValueRow(
                        label = stringResource(R.string.circular_preferred_action),
                        value = stringResource(it.labelRes),
                    )
                    HimaDivider()
                }
            }
        }

        report.confidence?.let { confidence ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 7.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = stringResource(R.string.analysis_confidence), style = HimaTextStyles.b, color = colors.sage)
                Text(
                    text = "$confidence%",
                    style = HimaTextStyles.num.copy(fontFamily = Inter, fontSize = 15.sp),
                    color = colors.ink,
                )
            }
            ConfidenceBar(progress = confidence.coerceIn(0, 100) / 100f)
        }

        report.reasonOverride?.takeIf { it.isNotBlank() && it != title }?.let {
            ReportSection(stringResource(R.string.report_reason), it)
        }
        analysis?.aiExplanation?.takeIf { it.isNotBlank() }?.let {
            ReportSection(stringResource(R.string.circular_ai_explanation), it)
        }
        report.environmentalImpactOverride?.takeIf { it.isNotBlank() }?.let {
            ReportSection(stringResource(R.string.circular_environmental_impact), it)
        }
        report.recommendationOverride?.takeIf { it.isNotBlank() }?.let {
            ReportSection(stringResource(R.string.report_action), it, highlight = true)
        }

        if (isWaste) {
            val guidance = listOf(
                R.string.circular_reuse_guidance to analysis?.reuseSuggestion,
                R.string.circular_repair_guidance to analysis?.repairGuidance,
                R.string.circular_recycling_guidance to analysis?.recyclingGuidance,
                R.string.circular_disposal_guidance to analysis?.disposalGuidance,
            ).filter { (_, value) ->
                !value.isNullOrBlank() && value != report.recommendationOverride
            }.distinctBy { it.second }
            guidance.forEach { (titleRes, value) ->
                ReportSection(title = stringResource(titleRes), body = value.orEmpty())
            }
        }

        if (report.latitude != null && report.longitude != null) {
            HimaPrimaryButton(
                text = stringResource(R.string.report_view_on_map),
                onClick = onViewOnMapClick,
                leadingIconRes = R.drawable.ic_field_pin,
                modifier = Modifier.padding(top = 26.dp),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            HimaSecondaryButton(
                text = stringResource(R.string.report_share),
                onClick = ::shareReport,
                leadingIconRes = R.drawable.ic_share,
                modifier = Modifier.weight(1f),
            )
            if (!isWaste) {
                HimaSecondaryButton(
                    text = stringResource(R.string.report_ask_ai),
                    onClick = onInvestigateClick,
                    leadingIconRes = R.drawable.ic_spark,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Spacer(Modifier.height(30.dp))
    }
}

@Composable
private fun BooleanValueRow(labelRes: Int, value: Boolean) {
    val colors = LocalHimaColors.current
    KeyValueRow(
        label = stringResource(labelRes),
        valueContent = {
            Text(
                text = stringResource(if (value) R.string.common_yes else R.string.common_no),
                style = HimaTextStyles.t.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                color = if (value) colors.green else colors.ink2,
            )
        },
    )
}

@Composable
private fun ReportSection(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
) {
    val colors = LocalHimaColors.current
    Column(modifier.padding(top = 20.dp)) {
        Text(text = title, style = HimaTextStyles.t, color = colors.ink)
        Text(
            text = body,
            style = HimaTextStyles.b,
            color = if (highlight) colors.green else colors.ink2,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun DetailState(text: String, onRetry: (() -> Unit)?, modifier: Modifier = Modifier) {
    val colors = LocalHimaColors.current
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = text, style = HimaTextStyles.b, color = colors.sage, textAlign = TextAlign.Center)
        onRetry?.let {
            HimaTextLink(
                text = stringResource(R.string.common_retry),
                onClick = it,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}
