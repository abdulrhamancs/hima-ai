package com.hima.ai.presentation.report.newreport

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hima.ai.R
import com.hima.ai.core.designsystem.component.HimaPrimaryButton
import com.hima.ai.core.designsystem.component.HimaTextArea
import com.hima.ai.core.designsystem.component.ImagePickerCard
import com.hima.ai.core.designsystem.component.ScreenHeader
import com.hima.ai.core.designsystem.component.SelectedImageCard
import com.hima.ai.core.designsystem.component.StepIndicator
import com.hima.ai.core.designsystem.theme.HimaRadius
import com.hima.ai.core.designsystem.theme.HimaTextStyles
import com.hima.ai.core.designsystem.theme.LocalHimaColors

/**
 * New report — attach evidence, confirm the auto-detected location, add an
 * optional note, then hand off to the AI. The primary action stays disabled
 * until a photo exists, so the ranger can't reach a dead end.
 */
@Composable
fun NewReportScreen(
    onBackClick: () -> Unit,
    onAnalyzeClick: () -> Unit,
    onCaptureClick: () -> Unit,
    onGalleryClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NewReportViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalHimaColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        Spacer(Modifier.height(50.dp))
        ScreenHeader(
            title = stringResource(R.string.new_report_title),
            onBackClick = onBackClick,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            StepIndicator(
                currentStep = uiState.currentStep,
                modifier = Modifier.padding(top = 6.dp, bottom = 26.dp),
            )

            Text(
                text = stringResource(R.string.new_report_step_attach),
                style = HimaTextStyles.t,
                color = colors.ink,
                modifier = Modifier.padding(bottom = 11.dp),
            )

            val imageUri = uiState.imageUri
            if (imageUri == null) {
                ImagePickerCard(
                    onCaptureClick = onCaptureClick,
                    onGalleryClick = onGalleryClick,
                )
            } else {
                SelectedImageCard(imageUri = imageUri, onChangeClick = viewModel::onClearImage)
            }

            Text(
                text = stringResource(R.string.new_report_location),
                style = HimaTextStyles.t,
                color = colors.ink,
                modifier = Modifier.padding(top = 24.dp, bottom = 10.dp),
            )
            LocationField()

            Row(
                modifier = Modifier.padding(top = 24.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = stringResource(R.string.new_report_description),
                    style = HimaTextStyles.t,
                    color = colors.ink,
                )
                Text(
                    text = stringResource(R.string.new_report_description_optional),
                    style = HimaTextStyles.m,
                    color = colors.sage,
                )
            }
            HimaTextArea(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChange,
                hint = stringResource(R.string.new_report_description_hint),
            )

            AnimatedVisibility(
                visible = uiState.canAnalyze,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                HimaPrimaryButton(
                    text = stringResource(R.string.new_report_analyze),
                    onClick = onAnalyzeClick,
                    leadingIconRes = R.drawable.ic_spark,
                    modifier = Modifier.padding(top = 26.dp),
                )
            }
            Spacer(Modifier.height(30.dp))
        }
    }
}

/** The auto-detected location, shown as a resolved value rather than an input. */
@Composable
private fun LocationField(modifier: Modifier = Modifier) {
    val colors = LocalHimaColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(HimaRadius.field))
            .background(colors.bg2)
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_field_pin),
            contentDescription = null,
            tint = colors.green,
            modifier = Modifier.size(19.dp),
        )
        Text(
            text = stringResource(R.string.new_report_location_value),
            style = HimaTextStyles.t.copy(fontSize = 15.sp),
            color = colors.ink,
        )
    }
}
