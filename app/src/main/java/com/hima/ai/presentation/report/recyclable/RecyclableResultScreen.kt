package com.hima.ai.presentation.report.recyclable

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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.hima.ai.core.designsystem.component.KeyValueRow
import com.hima.ai.core.designsystem.component.ScreenHeader
import com.hima.ai.core.designsystem.theme.HimaRadius
import com.hima.ai.core.designsystem.theme.HimaTextStyles
import com.hima.ai.core.designsystem.theme.Inter
import com.hima.ai.core.designsystem.theme.LocalHimaColors

/**
 * Result screen for a recyclable/reusable item — a circular-economy result,
 * not an incident. It deliberately shares [com.hima.ai.presentation.report.detail.ReportDetailScreen]'s
 * visual language (same header, key/value rows, section style, button) but
 * not the screen itself: there is no severity, no risk score, no "save as
 * report" — the item was never written to the reports table, so the only
 * next step is analysing another photo.
 */
@Composable
fun RecyclableResultScreen(
    onBackClick: () -> Unit,
    onAnalyzeAnotherClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecyclableResultViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState
    val colors = LocalHimaColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        Spacer(Modifier.height(50.dp))
        ScreenHeader(
            title = stringResource(R.string.recyclable_result_title),
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
                val imageUri = uiState.imageUri
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = stringResource(R.string.cd_evidence_photo),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            Text(
                text = uiState.description,
                style = HimaTextStyles.h2.copy(fontSize = 19.sp, fontWeight = FontWeight.SemiBold),
                color = colors.ink,
                modifier = Modifier.padding(top = 18.dp),
            )

            Column(Modifier.padding(top = 14.dp)) {
                KeyValueRow(
                    label = stringResource(R.string.recyclable_category_label),
                    value = uiState.materialCategory,
                )
                HimaDivider()
                KeyValueRow(
                    label = stringResource(R.string.recyclable_classification_label),
                    valueContent = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = uiState.disposalClassification,
                                style = HimaTextStyles.t.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                                color = colors.green,
                            )
                            Icon(
                                painter = painterResource(R.drawable.ic_recycle),
                                contentDescription = null,
                                tint = colors.green,
                                modifier = Modifier.height(14.dp),
                            )
                        }
                    },
                )
                HimaDivider()
            }

            ReportSection(
                title = stringResource(R.string.report_action),
                body = uiState.recommendation,
            )

            val reuseSuggestion = uiState.reuseSuggestion
            if (reuseSuggestion != null) {
                ReportSection(
                    title = stringResource(R.string.recyclable_reuse_label),
                    body = reuseSuggestion,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 6.dp),
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

            HimaPrimaryButton(
                text = stringResource(R.string.recyclable_analyze_another),
                onClick = onAnalyzeAnotherClick,
                leadingIconRes = R.drawable.ic_camera,
                modifier = Modifier.padding(top = 26.dp, bottom = 30.dp),
            )
        }
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
