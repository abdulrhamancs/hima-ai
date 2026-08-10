package com.hima.ai.presentation.report.analysis

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.hima.ai.R
import com.hima.ai.core.designsystem.component.AIAnalysisState
import com.hima.ai.core.designsystem.component.AnalysisStepRow
import com.hima.ai.core.designsystem.component.ConfidenceBar
import com.hima.ai.core.designsystem.component.HimaDivider
import com.hima.ai.core.designsystem.component.HimaTextLink
import com.hima.ai.core.designsystem.component.KeyValueRow
import com.hima.ai.core.designsystem.component.SceneArt
import com.hima.ai.core.designsystem.component.ScreenHeader
import com.hima.ai.core.designsystem.component.SeverityBadge
import com.hima.ai.core.designsystem.theme.HimaRadius
import com.hima.ai.core.designsystem.theme.HimaTextStyles
import com.hima.ai.core.designsystem.theme.Inter
import com.hima.ai.core.designsystem.theme.LocalHimaColors
import com.hima.ai.domain.model.AnalysisResultCategory
import com.hima.ai.domain.model.AiAnalysis
import com.hima.ai.domain.model.SceneKind
import com.hima.ai.domain.model.Severity

/**
 * AI analysis — the evidence photo, a live progress component, and the
 * findings filling in as they resolve. Advances to the final report on its own
 * once the run completes.
 */
@Composable
fun AnalysisScreen(
    onBackClick: () -> Unit,
    onAnalysisComplete: (AiAnalysis) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AnalysisViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalHimaColors.current

    LaunchedEffect(uiState.isComplete) {
        val result = uiState.result
        if (uiState.isComplete && result != null) onAnalysisComplete(result)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        Spacer(Modifier.height(50.dp))
        ScreenHeader(
            title = stringResource(R.string.analysis_title),
            onBackClick = onBackClick,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(196.dp)
                    .clip(RoundedCornerShape(HimaRadius.hero)),
            ) {
                // The evidence actually being analysed, carried from capture.
                val evidenceUri = uiState.imageUri
                if (evidenceUri != null) {
                    AsyncImage(
                        model = evidenceUri,
                        contentDescription = stringResource(R.string.cd_evidence_photo),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    SceneArt(kind = SceneKind.STUMP, modifier = Modifier.fillMaxSize())
                }
            }

            AIAnalysisState(
                title = stringResource(R.string.analysis_loading_title),
                subtitle = stringResource(R.string.analysis_loading_sub),
                modifier = Modifier.padding(top = 16.dp),
            )

            Column(Modifier.padding(top = 16.dp)) {
                AnalysisStepRow(
                    label = stringResource(R.string.analysis_step_vision),
                    done = uiState.stepsDone >= 1,
                )
                AnalysisStepRow(
                    label = stringResource(R.string.analysis_step_context),
                    done = uiState.stepsDone >= 2,
                )
                AnalysisStepRow(
                    label = stringResource(R.string.analysis_step_severity),
                    done = uiState.stepsDone >= 3,
                )
            }

            val result = uiState.result
            val isIncident = result?.category == AnalysisResultCategory.ENVIRONMENTAL_INCIDENT
            AnimatedVisibility(visible = uiState.stepsDone >= 1 && result != null, enter = fadeIn()) {
                if (result != null) {
                    Column(Modifier.padding(top = 10.dp)) {
                        KeyValueRow(
                            label = stringResource(if (isIncident) R.string.analysis_kind else R.string.analysis_material),
                            value = (if (isIncident) result.issueType else result.materialCategory).orEmpty(),
                        )
                        HimaDivider()
                    }
                }
            }
            AnimatedVisibility(visible = uiState.stepsDone >= 3 && result != null, enter = fadeIn()) {
                if (result != null) {
                    Column {
                        if (isIncident) {
                            KeyValueRow(
                                label = stringResource(R.string.analysis_severity),
                                valueContent = { SeverityBadge(result.riskLevel ?: Severity.LOW) },
                            )
                        } else {
                            KeyValueRow(
                                label = stringResource(R.string.analysis_classification),
                                value = result.disposalClassification.orEmpty(),
                            )
                        }
                        HimaDivider()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 14.dp, bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = stringResource(R.string.analysis_confidence),
                                style = HimaTextStyles.b.copy(fontSize = 14.5.sp),
                                color = colors.sage,
                            )
                            Text(
                                text = "${result.confidence}%",
                                style = HimaTextStyles.num.copy(fontFamily = Inter, fontSize = 15.sp),
                                color = colors.ink,
                            )
                        }
                        ConfidenceBar(progress = result.confidence / 100f)
                    }
                }
            }

            AnimatedVisibility(visible = uiState.errorMessage != null, enter = fadeIn()) {
                AnalysisErrorNotice(
                    message = uiState.errorMessage.orEmpty(),
                    onRetry = viewModel::onRetry,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }

            Spacer(Modifier.height(30.dp))
        }
    }
}

/** A failed analysis run — the photo stays visible above this, so retrying doesn't feel like starting over. */
@Composable
private fun AnalysisErrorNotice(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalHimaColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(HimaRadius.field))
            .background(colors.warm)
            .padding(16.dp),
    ) {
        Text(
            text = message,
            style = HimaTextStyles.b.copy(fontSize = 14.5.sp),
            color = colors.severityCritical,
        )
        HimaTextLink(
            text = stringResource(R.string.analysis_retry),
            onClick = onRetry,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
