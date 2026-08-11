package com.hima.ai.presentation.report.analysis

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.hima.ai.R
import com.hima.ai.core.designsystem.component.AiScanOverlay
import com.hima.ai.core.designsystem.component.AnalysisPhaseText
import com.hima.ai.core.designsystem.component.CircularEconomyFlow
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
import kotlinx.coroutines.delay

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
    val result = uiState.result

    // How much of the verdict has landed. Hoisted up here (rather than living
    // beside the blocks it drives) because navigation waits on it — see below.
    var revealStage by remember { mutableIntStateOf(0) }
    // Whether the reveal has finished *playing*, as opposed to merely having
    // reached its last stage. These are not the same instant — see below.
    var revealComplete by remember { mutableStateOf(false) }
    LaunchedEffect(result) {
        if (result == null) {
            revealStage = 0
            revealComplete = false
            return@LaunchedEffect
        }
        delay(140L)
        revealStage = 1
        delay(REVEAL_GAP_MS)
        revealStage = 2
        delay(REVEAL_GAP_MS)
        revealStage = 3
        delay(REVEAL_GAP_MS)
        revealStage = 4
        // Reaching stage 4 *starts* the closing beat rather than ending it: the
        // action card is still rising, and the economy flow has only just been
        // told to light its four nodes, which it does one at a time. Leaving
        // now would cut both off mid-move. Hold for that beat to actually play.
        delay(FINAL_BEAT_MS)
        revealComplete = true
    }

    // The ViewModel calls the run complete ~500ms after the result arrives,
    // which is shorter than the reveal it kicks off. Holding navigation until
    // the reveal has finished playing keeps the sequence from being cut off —
    // a UI-timing concern, so it's gated here rather than by retiming the
    // ViewModel.
    LaunchedEffect(uiState.isComplete, revealComplete) {
        if (uiState.isComplete && result != null && revealComplete) {
            onAnalysisComplete(result)
        }
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
            val isIncident = result?.category == AnalysisResultCategory.ENVIRONMENTAL_INCIDENT
            val isAnalysing = result == null && uiState.errorMessage == null

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
                // The scanning treatment exists only while the model is really
                // working, so it can never sit there looking stuck on a result
                // that already arrived. Crossfaded on alpha rather than wrapped
                // in AnimatedVisibility: this Box sits inside a Column, which
                // makes the scoped AnimatedVisibility overloads ambiguous, and
                // a plain fade is all this needs.
                val overlayAlpha by animateFloatAsState(
                    targetValue = if (isAnalysing) 1f else 0f,
                    animationSpec = tween(280, easing = FastOutSlowInEasing),
                    label = "scanOverlayAlpha",
                )
                if (overlayAlpha > 0.01f) {
                    AiScanOverlay(
                        Modifier
                            .fillMaxSize()
                            .alpha(overlayAlpha),
                    )
                }
            }

            // Rotating "what the model is doing right now" line. Fading the
            // whole block out is what ends it cleanly, instead of cutting a
            // phrase off mid-transition when the response lands.
            AnimatedVisibility(
                visible = isAnalysing,
                enter = fadeIn(tween(300)),
                exit = fadeOut(tween(240)),
            ) {
                AnalysisPhaseText(
                    phases = listOf(
                        stringResource(R.string.analysis_phase_identify),
                        stringResource(R.string.analysis_phase_severity),
                        stringResource(R.string.analysis_phase_action),
                    ),
                    modifier = Modifier.padding(top = 24.dp, bottom = 6.dp),
                )
            }

            // Staged reveal — the verdict lands one beat at a time, so the model
            // reads as deciding rather than dumping a finished card. Driven by
            // revealStage, hoisted to the top of this composable.
            AnimatedVisibility(visible = revealStage >= 1 && result != null, enter = revealEnter(), exit = revealExit()) {
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
            AnimatedVisibility(visible = revealStage >= 2 && result != null, enter = revealEnter(), exit = revealExit()) {
                if (result != null) {
                    Column {
                        if (isIncident) {
                            KeyValueRow(
                                label = stringResource(R.string.analysis_severity),
                                valueContent = { VerdictBadge(result.riskLevel ?: Severity.LOW) },
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

            // The payoff of the sequence: what to actually do about it. Rises
            // from below — a vertical move, so nothing has to mirror in Arabic.
            AnimatedVisibility(
                visible = revealStage >= 3 && result != null,
                enter = slideInVertically(tween(420, easing = FastOutSlowInEasing)) { it / 2 } +
                    fadeIn(tween(420)),
                exit = revealExit(),
            ) {
                if (result != null) {
                    RecommendedActionCard(
                        text = result.recommendation,
                        modifier = Modifier.padding(top = 18.dp),
                    )
                }
            }

            // The value loop this report just fed into.
            AnimatedVisibility(
                visible = revealStage >= 3 && result != null,
                enter = fadeIn(tween(420)),
                exit = revealExit(),
            ) {
                Column(Modifier.padding(top = 22.dp)) {
                    Text(
                        text = stringResource(R.string.economy_flow_title),
                        style = HimaTextStyles.b.copy(fontSize = 13.sp),
                        color = colors.sage,
                    )
                    CircularEconomyFlow(
                        labels = listOf(
                            stringResource(R.string.economy_stage_detected),
                            stringResource(R.string.economy_stage_analysed),
                            stringResource(R.string.economy_stage_action),
                            stringResource(R.string.economy_stage_impact),
                        ),
                        litCount = if (revealStage >= 4) 4 else 0,
                        modifier = Modifier.padding(top = 14.dp),
                    )
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

/** Spacing between beats of the result reveal. */
private const val REVEAL_GAP_MS = 200L

/**
 * How long the closing beat needs to finish after the last stage is set: the
 * economy flow lights four nodes 220ms apart, and the fourth still has a 320ms
 * fade to run. Sized to that rather than guessed, so the sequence is never
 * navigated away from mid-move.
 */
private const val FINAL_BEAT_MS = 4 * 220L + 320L

/** Shared entrance for each staged result block: a short rise plus fade.
 *  Vertical by design — it has no start/end edge, so it needs no RTL mirroring. */
private fun revealEnter(): EnterTransition =
    slideInVertically(tween(360, easing = FastOutSlowInEasing)) { it / 3 } + fadeIn(tween(360))

/** Counterpart exit, so a block that goes away fades instead of hard-cutting. */
private fun revealExit(): ExitTransition = fadeOut(tween(200))

/**
 * The severity verdict landing. A spring with a little bounce carries it past
 * 1.0 and settles back, so it reads as a decision being made rather than a
 * label that was always sitting there.
 */
@Composable
private fun VerdictBadge(severity: Severity) {
    val scale = remember { Animatable(0.8f) }
    LaunchedEffect(severity) {
        scale.snapTo(0.8f)
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        )
    }
    SeverityBadge(severity, modifier = Modifier.scale(scale.value))
}

/** The recommended action — the resolution the whole reveal builds toward. */
@Composable
private fun RecommendedActionCard(text: String, modifier: Modifier = Modifier) {
    val colors = LocalHimaColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(HimaRadius.card))
            .background(colors.bg2)
            .padding(16.dp),
    ) {
        Text(
            text = stringResource(R.string.report_action),
            style = HimaTextStyles.b.copy(fontSize = 13.sp),
            color = colors.sage,
        )
        Text(
            text = text,
            style = HimaTextStyles.t.copy(fontSize = 15.sp, lineHeight = 21.sp),
            color = colors.ink,
            modifier = Modifier.padding(top = 6.dp),
        )
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
