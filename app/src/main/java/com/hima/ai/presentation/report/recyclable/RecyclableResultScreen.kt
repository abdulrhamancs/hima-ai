package com.hima.ai.presentation.report.recyclable

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.hima.ai.R
import com.hima.ai.core.designsystem.component.ConfidenceBar
import com.hima.ai.core.designsystem.component.HimaDivider
import com.hima.ai.core.designsystem.component.HimaPrimaryButton
import com.hima.ai.core.designsystem.component.HimaSecondaryButton
import com.hima.ai.core.designsystem.component.KeyValueRow
import com.hima.ai.core.designsystem.component.ScreenHeader
import com.hima.ai.core.designsystem.theme.HimaRadius
import com.hima.ai.core.designsystem.theme.HimaTextStyles
import com.hima.ai.core.designsystem.theme.Inter
import com.hima.ai.core.designsystem.theme.LocalHimaColors
import com.hima.ai.domain.model.CircularAction

/** Circular-economy decision result, ordered from identification to value recovery. */
@Composable
fun RecyclableResultScreen(
    onBackClick: () -> Unit,
    onAnalyzeAnotherClick: () -> Unit,
    onViewReportClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecyclableResultViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState
    val colors = LocalHimaColors.current
    val itemName = uiState.wasteType.ifBlank { uiState.description }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        Spacer(Modifier.height(50.dp))
        ScreenHeader(
            title = stringResource(R.string.circular_result_title),
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
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(HimaRadius.hero)),
            ) {
                uiState.imageUri?.let { imageUri ->
                    AsyncImage(
                        model = imageUri,
                        contentDescription = stringResource(R.string.cd_evidence_photo),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            if (uiState.isLowConfidence) {
                Text(
                    text = stringResource(R.string.circular_low_confidence),
                    style = HimaTextStyles.b,
                    color = colors.ink2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .clip(RoundedCornerShape(HimaRadius.button))
                        .background(colors.warm)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                )
            }

            Text(
                text = itemName,
                style = HimaTextStyles.h2.copy(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
                color = colors.ink,
                modifier = Modifier.padding(top = 18.dp),
            )

            Column(Modifier.padding(top = 14.dp)) {
                if (uiState.materialCategory.isNotBlank()) {
                    KeyValueRow(
                        label = stringResource(R.string.circular_material),
                        value = uiState.materialCategory,
                    )
                    HimaDivider()
                }
                if (uiState.disposalClassification.isNotBlank()) {
                    KeyValueRow(
                        label = stringResource(R.string.recyclable_classification_label),
                        value = uiState.disposalClassification,
                    )
                    HimaDivider()
                }
                uiState.reusable?.let {
                    RecoveryStatusRow(R.string.circular_reusable, it)
                    HimaDivider()
                }
                uiState.repairable?.let {
                    RecoveryStatusRow(R.string.circular_repairable, it)
                    HimaDivider()
                }
                uiState.recyclable?.let {
                    RecoveryStatusRow(R.string.circular_recyclable, it)
                    HimaDivider()
                }
            }

            uiState.environmentalImpact?.let {
                ResultSection(stringResource(R.string.circular_environmental_impact), it)
            }
            uiState.aiExplanation?.let {
                ResultSection(stringResource(R.string.circular_ai_explanation), it)
            }
            uiState.preferredAction?.let {
                ResultSection(
                    title = stringResource(R.string.circular_preferred_action),
                    body = stringResource(it.labelRes),
                    highlight = true,
                )
            }
            if (uiState.recommendation.isNotBlank()) {
                ResultSection(
                    title = stringResource(R.string.circular_recommendation),
                    body = uiState.recommendation,
                )
            }
            preferredGuidance(uiState)?.let {
                ResultSection(stringResource(R.string.circular_how_to_act), it)
            }
            uiState.recyclingGuidance
                ?.takeIf {
                    uiState.preferredAction != CircularAction.RECYCLE &&
                        uiState.preferredAction != CircularAction.MATERIAL_RECOVERY &&
                        it.isNotBlank() && it != uiState.recommendation
                }
                ?.let {
                    ResultSection(stringResource(R.string.circular_next_recovery_step), it)
                }
            uiState.disposalGuidance
                ?.takeIf {
                    uiState.preferredAction != CircularAction.SAFE_DISPOSAL &&
                        it.isNotBlank() && it != uiState.recommendation
                }
                ?.let {
                    ResultSection(stringResource(R.string.circular_last_resort), it)
                }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.analysis_confidence),
                    style = HimaTextStyles.b.copy(fontSize = 14.5.sp),
                    color = colors.sage,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "${uiState.confidence}%",
                    style = HimaTextStyles.num.copy(fontFamily = Inter, fontSize = 15.sp),
                    color = colors.ink,
                )
            }
            ConfidenceBar(progress = uiState.confidence.coerceIn(0, 100) / 100f)

            uiState.reportId?.let { reportId ->
                HimaPrimaryButton(
                    text = stringResource(R.string.circular_view_report),
                    onClick = { onViewReportClick(reportId) },
                    leadingIconRes = R.drawable.ic_tab_reports,
                    modifier = Modifier.padding(top = 26.dp),
                )
                HimaSecondaryButton(
                    text = stringResource(R.string.recyclable_analyze_another),
                    onClick = onAnalyzeAnotherClick,
                    leadingIconRes = R.drawable.ic_camera,
                    modifier = Modifier.padding(top = 10.dp, bottom = 30.dp),
                )
            } ?: HimaPrimaryButton(
                text = stringResource(R.string.recyclable_analyze_another),
                onClick = onAnalyzeAnotherClick,
                leadingIconRes = R.drawable.ic_camera,
                modifier = Modifier.padding(top = 26.dp, bottom = 30.dp),
            )
        }
    }
}

@Composable
private fun RecoveryStatusRow(labelRes: Int, supported: Boolean) {
    val colors = LocalHimaColors.current
    KeyValueRow(
        label = stringResource(labelRes),
        valueContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (supported) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = null,
                        tint = colors.green,
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .size(15.dp),
                    )
                }
                Text(
                    text = stringResource(if (supported) R.string.common_yes else R.string.common_no),
                    style = HimaTextStyles.t.copy(fontSize = 15.sp),
                    color = if (supported) colors.green else colors.ink2,
                )
            }
        },
    )
}

private fun preferredGuidance(state: RecyclableResultUiState): String? {
    val candidate = when (state.preferredAction) {
        CircularAction.REUSE -> state.reuseSuggestion
        CircularAction.REPAIR_REFURBISH -> state.repairGuidance
        CircularAction.RECYCLE, CircularAction.MATERIAL_RECOVERY -> state.recyclingGuidance
        CircularAction.SAFE_DISPOSAL -> state.disposalGuidance
        CircularAction.DONATE_REPURPOSE, null -> null
    }
    return candidate?.takeIf { it.isNotBlank() && it != state.recommendation }
}

@Composable
private fun ResultSection(
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
