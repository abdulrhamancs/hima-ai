package com.hima.ai.presentation.badges

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hima.ai.R
import com.hima.ai.core.designsystem.component.BadgeRowStatus
import com.hima.ai.core.designsystem.component.BadgeTierRow
import com.hima.ai.core.designsystem.component.ScreenHeader
import androidx.compose.material3.Text
import com.hima.ai.core.designsystem.theme.HimaTextStyles
import com.hima.ai.core.designsystem.theme.LocalHimaColors
import com.hima.ai.domain.model.BadgeTier
import kotlinx.coroutines.delay

/**
 * The full reward ladder — every tier in order, each tagged current / obtained
 * / locked against the ranger's real verified-report count. Reached from the
 * overview's "Badge path" section via [BadgesScreen]'s "View all" action.
 */
@Composable
fun BadgeTierListScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BadgesViewModel = hiltViewModel(),
) {
    val colors = LocalHimaColors.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var revealedRows by remember { mutableIntStateOf(0) }
    var ctaVisible by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.currentTier, uiState.totalVerifiedReports) {
        revealedRows = 0
        ctaVisible = false
        delay(70)
        repeat(BadgeTier.ordered.size) {
            delay(55)
            revealedRows++
        }
        delay(100)
        ctaVisible = true
    }

    Column(modifier = modifier.fillMaxSize().background(colors.bg)) {
        Spacer(Modifier.height(50.dp))
        ScreenHeader(
            title = stringResource(R.string.badges_title),
            onBackClick = onBackClick,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
        ) {
            itemsIndexed(BadgeTier.ordered, key = { _, tier -> tier.name }) { index, tier ->
                AnimatedVisibility(visible = index < revealedRows, enter = badgeRevealEnter()) {
                    val status = when {
                        tier == uiState.currentTier -> BadgeRowStatus.CURRENT
                        tier.threshold <= uiState.totalVerifiedReports -> BadgeRowStatus.OBTAINED
                        else -> BadgeRowStatus.LOCKED
                    }
                    BadgeTierRow(
                        tier = tier,
                        status = status,
                        requirementText = stringResource(R.string.badges_requirement_reports, tier.threshold),
                        showTopConnector = index > 0,
                        trailing = when (status) {
                            BadgeRowStatus.LOCKED -> null
                            BadgeRowStatus.CURRENT -> {
                                {
                                    Text(
                                        text = stringResource(R.string.badges_date_placeholder),
                                        style = HimaTextStyles.m.copy(fontSize = 11.5.sp),
                                        color = colors.sage,
                                    )
                                }
                            }
                            BadgeRowStatus.OBTAINED -> {
                                val achievedAt = uiState.achievedTiers.firstOrNull { it.tier == tier }?.achievedAt
                                {
                                    Text(
                                        text = achievedDateLabel(achievedAt),
                                        style = HimaTextStyles.m.copy(fontSize = 11.5.sp),
                                        color = colors.sage,
                                    )
                                }
                            }
                        },
                    )
                }
            }
            item {
                AnimatedVisibility(visible = ctaVisible, enter = badgeRevealEnter()) {
                    ReportMoreBanner(modifier = Modifier.padding(top = 18.dp, bottom = 24.dp))
                }
            }
        }
    }
}
