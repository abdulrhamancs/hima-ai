package com.hima.ai.presentation.report.detail

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hima.ai.R
import com.hima.ai.core.designsystem.component.HimaDivider
import com.hima.ai.core.designsystem.component.HimaIconButton
import com.hima.ai.core.designsystem.component.HimaPrimaryButton
import com.hima.ai.core.designsystem.component.HimaSecondaryButton
import com.hima.ai.core.designsystem.component.KeyValueRow
import com.hima.ai.core.designsystem.component.ScreenHeader
import com.hima.ai.core.designsystem.component.SeverityBadge
import com.hima.ai.core.designsystem.theme.HimaRadius
import com.hima.ai.core.designsystem.theme.HimaTextStyles
import com.hima.ai.core.designsystem.theme.Inter
import com.hima.ai.core.designsystem.theme.LocalHimaColors
import kotlinx.coroutines.delay

/** How long the "saved" confirmation stays on screen before continuing to History. */
private const val SAVE_CONFIRMATION_MS = 700L

/**
 * Final report — the structured AI output. Values sit on plain white separated
 * by hairlines rather than in individual cards, so the whole assessment reads
 * as one document.
 */
@Composable
fun ReportDetailScreen(
    onBackClick: () -> Unit,
    onInvestigateClick: () -> Unit,
    onViewOnMapClick: () -> Unit,
    onSavedClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReportDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalHimaColors.current
    val context = LocalContext.current

    val kindText = stringResource(uiState.kindRes)
    val locationText = stringResource(uiState.locationRes)
    val timeText = stringResource(uiState.timeRes)
    val shareText = stringResource(R.string.report_share_text, kindText, locationText, timeText, uiState.riskScore)

    fun shareReport() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(intent, kindText))
    }

    LaunchedEffect(Unit) { viewModel.refresh() }

    // Let the "saved" confirmation register before moving on, so the action
    // reads as completed rather than as an unexplained jump.
    LaunchedEffect(uiState.saved) {
        if (uiState.saved) {
            delay(SAVE_CONFIRMATION_MS)
            onSavedClick()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        Spacer(Modifier.height(50.dp))
        ScreenHeader(
            title = stringResource(R.string.report_title),
            onBackClick = onBackClick,
            trailing = {
                HimaIconButton(
                    iconRes = R.drawable.ic_share,
                    contentDescription = stringResource(R.string.cd_share),
                    onClick = ::shareReport,
                )
            },
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            // The banner confirms something that just happened. A report opened
            // from Home, History, or a marker wasn't analysed just now, so it
            // only earns a banner once the ranger saves it.
            if (uiState.saved || !uiState.isExistingReport) {
                SuccessBanner(
                    text = if (uiState.saved) {
                        stringResource(R.string.report_saved_toast)
                    } else {
                        stringResource(R.string.report_success)
                    },
                )
            }

            Column(Modifier.padding(top = 8.dp)) {
                KeyValueRow(
                    label = stringResource(R.string.report_kind),
                    value = kindText,
                )
                HimaDivider()
                KeyValueRow(
                    label = stringResource(R.string.report_severity),
                    valueContent = { SeverityBadge(uiState.severity) },
                )
                HimaDivider()
                KeyValueRow(
                    label = stringResource(R.string.report_risk_score),
                    valueContent = {
                        Text(
                            text = uiState.riskScore,
                            style = HimaTextStyles.num.copy(fontFamily = Inter, fontSize = 15.sp),
                            color = if (uiState.escalated) colors.severityCritical else colors.ink,
                        )
                    },
                )
                HimaDivider()
                KeyValueRow(
                    label = stringResource(R.string.report_location),
                    modifier = Modifier.clickable(onClick = onViewOnMapClick),
                    valueContent = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = locationText,
                                style = HimaTextStyles.t.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                                color = colors.ink,
                            )
                            Icon(
                                painter = painterResource(R.drawable.ic_field_pin),
                                contentDescription = stringResource(R.string.cd_view_on_map),
                                tint = colors.green,
                                modifier = Modifier.size(15.dp),
                            )
                        }
                    },
                )
                HimaDivider()
                KeyValueRow(
                    label = stringResource(R.string.report_time),
                    value = timeText,
                )
            }

            ReportSection(
                title = stringResource(R.string.report_reason),
                body = stringResource(R.string.report_reason_value),
            )
            ReportSection(
                title = stringResource(R.string.report_action),
                body = stringResource(R.string.report_action_value),
            )
            ReportSection(
                title = stringResource(R.string.report_authority),
                body = stringResource(R.string.report_authority_value),
            )

            HimaSecondaryButton(
                text = stringResource(R.string.report_ask_ai),
                onClick = onInvestigateClick,
                leadingIconRes = R.drawable.ic_spark,
                modifier = Modifier.padding(top = 24.dp),
            )

            Row(
                modifier = Modifier.padding(top = 11.dp),
                horizontalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                HimaSecondaryButton(
                    text = stringResource(R.string.report_share),
                    onClick = ::shareReport,
                    leadingIconRes = R.drawable.ic_share,
                    modifier = Modifier.weight(1f),
                )
                HimaPrimaryButton(
                    text = stringResource(R.string.report_save),
                    onClick = viewModel::onSave,
                    leadingIconRes = R.drawable.ic_download,
                    modifier = Modifier.weight(1.35f),
                )
            }
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
private fun SuccessBanner(text: String, modifier: Modifier = Modifier) {
    val colors = LocalHimaColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(HimaRadius.field))
            .background(colors.warm)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_check),
            contentDescription = null,
            tint = colors.green,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = text,
            style = HimaTextStyles.t.copy(fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold),
            color = colors.green,
        )
    }
}

@Composable
private fun ReportSection(title: String, body: String, modifier: Modifier = Modifier) {
    val colors = LocalHimaColors.current
    Column(modifier.padding(top = 20.dp)) {
        Text(
            text = title,
            style = HimaTextStyles.t,
            color = colors.ink,
        )
        Text(
            text = body,
            style = HimaTextStyles.b,
            color = colors.ink2,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
