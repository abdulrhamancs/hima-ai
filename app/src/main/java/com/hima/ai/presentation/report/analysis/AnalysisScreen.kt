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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hima.ai.R
import com.hima.ai.core.designsystem.component.AIAnalysisState
import com.hima.ai.core.designsystem.component.AnalysisStepRow
import com.hima.ai.core.designsystem.component.ConfidenceBar
import com.hima.ai.core.designsystem.component.HimaDivider
import com.hima.ai.core.designsystem.component.KeyValueRow
import com.hima.ai.core.designsystem.component.SceneArt
import com.hima.ai.core.designsystem.component.ScreenHeader
import com.hima.ai.core.designsystem.component.SeverityBadge
import com.hima.ai.core.designsystem.theme.HimaRadius
import com.hima.ai.core.designsystem.theme.HimaTextStyles
import com.hima.ai.core.designsystem.theme.Inter
import com.hima.ai.core.designsystem.theme.LocalHimaColors
import com.hima.ai.domain.model.SceneKind

/**
 * AI analysis — the evidence photo, a live progress component, and the
 * findings filling in as they resolve. Advances to the final report on its own
 * once the run completes.
 */
@Composable
fun AnalysisScreen(
    onBackClick: () -> Unit,
    onAnalysisComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AnalysisViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalHimaColors.current

    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) onAnalysisComplete()
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
                SceneArt(kind = SceneKind.STUMP, modifier = Modifier.fillMaxSize())
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

            AnimatedVisibility(visible = uiState.stepsDone >= 1, enter = fadeIn()) {
                Column(Modifier.padding(top = 10.dp)) {
                    KeyValueRow(
                        label = stringResource(R.string.analysis_kind),
                        value = stringResource(R.string.incident_logging),
                    )
                    HimaDivider()
                }
            }
            AnimatedVisibility(visible = uiState.stepsDone >= 3, enter = fadeIn()) {
                Column {
                    KeyValueRow(
                        label = stringResource(R.string.analysis_severity),
                        valueContent = { SeverityBadge(uiState.severity) },
                    )
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
                            text = "${uiState.confidence}%",
                            style = HimaTextStyles.num.copy(fontFamily = Inter, fontSize = 15.sp),
                            color = colors.ink,
                        )
                    }
                    ConfidenceBar(progress = uiState.confidence / 100f)
                }
            }

            Spacer(Modifier.height(30.dp))
        }
    }
}
