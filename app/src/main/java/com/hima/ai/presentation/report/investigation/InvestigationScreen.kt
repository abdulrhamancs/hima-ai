package com.hima.ai.presentation.report.investigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hima.ai.R
import com.hima.ai.core.designsystem.component.AIMessage
import com.hima.ai.core.designsystem.component.HimaIconButton
import com.hima.ai.core.designsystem.component.HimaPrimaryButton
import com.hima.ai.core.designsystem.component.InvestigationOption
import com.hima.ai.core.designsystem.component.LoadingIndicator
import com.hima.ai.core.designsystem.component.MinTouchTarget
import com.hima.ai.core.designsystem.component.UserMessage
import com.hima.ai.core.designsystem.theme.HimaRadius
import com.hima.ai.core.designsystem.theme.HimaTextStyles
import com.hima.ai.core.designsystem.theme.LocalHimaColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** How long the "report updated" confirmation stays visible before returning. */
private const val UPDATED_CONFIRMATION_MS = 900L

/**
 * AI investigation — a focused follow-up attached to one report, not an
 * open-ended chatbot. The assistant asks a single contextual question; the
 * ranger answers by tapping, and the report is updated from the reply.
 */
@Composable
fun InvestigationScreen(
    onBackClick: () -> Unit,
    onReportUpdated: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InvestigationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalHimaColors.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // Keep the newest turn in view as the conversation grows.
    LaunchedEffect(uiState.responseRes, uiState.isThinking) {
        scope.launch { scrollState.animateScrollTo(scrollState.maxValue) }
    }

    // Show the "report updated" confirmation before returning. Previously the
    // button popped the screen in the same click that set the flag, so the
    // confirmation could never be seen.
    LaunchedEffect(uiState.reportUpdated) {
        if (uiState.reportUpdated) {
            delay(UPDATED_CONFIRMATION_MS)
            onReportUpdated()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        Spacer(Modifier.height(50.dp))
        InvestigationHeader(onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(6.dp))
            AIMessage(text = stringResource(R.string.investigation_q1))

            Text(
                text = stringResource(R.string.investigation_pick),
                style = HimaTextStyles.t,
                color = colors.ink,
                modifier = Modifier.padding(top = 24.dp, bottom = 12.dp),
            )

            uiState.choices.forEachIndexed { index, choice ->
                InvestigationOption(
                    text = stringResource(choice.labelRes),
                    onClick = { viewModel.onChoiceSelected(index) },
                    selected = uiState.selectedIndex == index,
                    enabled = !uiState.hasAnswered,
                    modifier = Modifier.padding(bottom = 9.dp),
                )
            }

            AnimatedVisibility(visible = uiState.isThinking, enter = fadeIn()) {
                Row(
                    modifier = Modifier.padding(top = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(11.dp),
                ) {
                    LoadingIndicator(diameter = 26.dp, strokeWidth = 3.dp)
                    Text(
                        text = stringResource(R.string.analysis_loading_title),
                        style = HimaTextStyles.m,
                        color = colors.sage,
                    )
                }
            }

            val responseRes = uiState.responseRes
            AnimatedVisibility(
                visible = responseRes != null,
                enter = fadeIn() + slideInVertically { it / 4 },
            ) {
                Column(Modifier.padding(top = 18.dp)) {
                    uiState.selectedIndex?.let { index ->
                        uiState.choices.getOrNull(index)?.let { choice ->
                            UserMessage(text = stringResource(choice.labelRes))
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                    uiState.customAnswerText?.let { text ->
                        UserMessage(text = text)
                        Spacer(Modifier.height(16.dp))
                    }
                    if (responseRes != null) {
                        AIMessage(text = stringResource(responseRes))
                    }

                    if (uiState.reportUpdated) {
                        UpdatedNotice(modifier = Modifier.padding(top = 22.dp))
                    } else {
                        Text(
                            text = stringResource(R.string.investigation_followup),
                            style = HimaTextStyles.b,
                            color = colors.ink2,
                            modifier = Modifier.padding(top = 22.dp, bottom = 12.dp),
                        )
                        HimaPrimaryButton(
                            text = stringResource(R.string.investigation_apply),
                            onClick = viewModel::onApplyToReport,
                            leadingIconRes = R.drawable.ic_check,
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        InvestigationInputBar(
            value = uiState.draftText,
            onValueChange = viewModel::onDraftTextChanged,
            onSend = viewModel::onCustomAnswerSubmitted,
            enabled = !uiState.hasAnswered,
        )
    }
}

@Composable
private fun InvestigationHeader(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalHimaColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HimaIconButton(
            iconRes = R.drawable.ic_back,
            contentDescription = stringResource(R.string.cd_back),
            onClick = onBackClick,
        )
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.investigation_title),
                style = HimaTextStyles.h2.copy(fontSize = 16.5.sp),
                color = colors.ink,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.investigation_subtitle),
                style = HimaTextStyles.m.copy(fontSize = 12.sp),
                color = colors.sage,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Icon(
            painter = painterResource(R.drawable.ic_spark),
            contentDescription = null,
            tint = colors.sage,
            modifier = Modifier.size(22.dp),
        )
    }
}

/** Confirmation that the answer was folded back into the report. */
@Composable
private fun UpdatedNotice(modifier: Modifier = Modifier) {
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
            text = stringResource(R.string.investigation_updated),
            style = HimaTextStyles.t.copy(fontSize = 14.5.sp),
            color = colors.green,
        )
    }
}

/**
 * A free-text alternative to tapping an option pill. Submitting produces the
 * same thinking -> reply exchange, so typing is a first-class second path
 * through the same single-question flow, not an open-ended chat.
 */
@Composable
private fun InvestigationInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = LocalHimaColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 26.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(colors.bg2)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
        ) {
            if (value.isEmpty()) {
                Text(
                    text = stringResource(R.string.investigation_input_hint),
                    style = HimaTextStyles.b.copy(fontSize = 15.sp),
                    color = colors.sage,
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                singleLine = true,
                textStyle = HimaTextStyles.b.copy(fontSize = 15.sp, color = colors.ink),
                cursorBrush = SolidColor(colors.green),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { if (value.isNotBlank()) onSend() }),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Box(
            modifier = Modifier
                .size(MinTouchTarget)
                .clip(RoundedCornerShape(14.dp))
                .background(if (enabled && value.isNotBlank()) colors.green else colors.sage.copy(alpha = 0.35f))
                .then(
                    if (enabled && value.isNotBlank()) {
                        Modifier.clickable(onClick = onSend)
                    } else {
                        Modifier
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_send),
                contentDescription = stringResource(R.string.cd_send),
                tint = colors.onGreen,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
