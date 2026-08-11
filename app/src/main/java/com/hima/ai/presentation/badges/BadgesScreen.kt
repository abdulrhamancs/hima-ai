package com.hima.ai.presentation.badges

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hima.ai.R
import com.hima.ai.core.designsystem.component.AnimatedCount
import com.hima.ai.core.designsystem.component.BadgeRowStatus
import com.hima.ai.core.designsystem.component.BadgeTierRow
import com.hima.ai.core.designsystem.component.HimaIconButton
import com.hima.ai.core.designsystem.component.MedalBadge
import com.hima.ai.core.designsystem.component.ScreenHeader
import com.hima.ai.core.designsystem.component.SectionHeader
import com.hima.ai.core.designsystem.component.StatsRow
import com.hima.ai.core.designsystem.theme.HimaRadius
import com.hima.ai.core.designsystem.theme.HimaTextStyles
import com.hima.ai.core.designsystem.theme.LocalHimaColors
import com.hima.ai.core.designsystem.theme.OnHero
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

/**
 * Badges & Achievements — the reward-ladder overview. Real figures throughout
 * (report counts, dates, join date), staged in on entry the same way the AI
 * analysis screen stages its verdict, so the tier reads as *earned* rather
 * than a static settings row.
 */
@Composable
fun BadgesScreen(
    onBackClick: () -> Unit,
    onViewAllTiersClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BadgesViewModel = hiltViewModel(),
) {
    val colors = LocalHimaColors.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showInfo by remember { mutableStateOf(false) }
    var detailTier by remember { mutableStateOf<AchievedTier?>(null) }

    var sectionStage by remember { mutableIntStateOf(0) }
    var revealedRows by remember { mutableIntStateOf(0) }
    LaunchedEffect(uiState.achievedTiers) {
        sectionStage = 0
        revealedRows = 0
        delay(80)
        sectionStage = 1 // hero
        delay(90)
        sectionStage = 2 // stats
        delay(90)
        sectionStage = 3 // path header
        repeat(uiState.achievedTiers.size) {
            delay(60)
            revealedRows++
        }
        delay(110)
        sectionStage = 4 // CTA banner
    }

    Column(modifier = modifier.fillMaxSize().background(colors.bg)) {
        Spacer(Modifier.height(50.dp))
        ScreenHeader(
            title = stringResource(R.string.badges_title),
            onBackClick = onBackClick,
            trailing = {
                HimaIconButton(
                    iconRes = R.drawable.ic_info,
                    contentDescription = stringResource(R.string.cd_badges_info),
                    onClick = { showInfo = true },
                )
            },
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
        ) {
            item {
                AnimatedVisibility(visible = sectionStage >= 1, enter = badgeRevealEnter()) {
                    BadgeHeroCard(uiState = uiState, modifier = Modifier.padding(top = 12.dp))
                }
            }
            item {
                AnimatedVisibility(visible = sectionStage >= 2, enter = badgeRevealEnter()) {
                    StatsRow(
                        items = listOf(
                            uiState.totalVerifiedReports.toString() to stringResource(R.string.badges_stat_total),
                            uiState.reportsProcessed.toString() to stringResource(R.string.badges_stat_processed),
                            uiState.daysSinceJoining.toString() to stringResource(R.string.badges_stat_days),
                        ),
                        iconResIds = listOf(R.drawable.ic_tab_reports, R.drawable.ic_check, R.drawable.ic_clock),
                        animatedValues = listOf(
                            uiState.totalVerifiedReports,
                            uiState.reportsProcessed,
                            uiState.daysSinceJoining,
                        ),
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }
            }
            item {
                AnimatedVisibility(visible = sectionStage >= 3, enter = badgeRevealEnter()) {
                    SectionHeader(
                        title = stringResource(R.string.badges_path_title),
                        actionLabel = stringResource(R.string.badges_view_all),
                        onActionClick = onViewAllTiersClick,
                    )
                }
            }
            if (uiState.achievedTiers.isEmpty()) {
                item {
                    AnimatedVisibility(visible = sectionStage >= 3, enter = badgeRevealEnter()) {
                        Text(
                            text = stringResource(R.string.badges_empty_path),
                            style = HimaTextStyles.b,
                            color = colors.sage,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        )
                    }
                }
            } else {
                itemsIndexed(uiState.achievedTiers, key = { _, achieved -> achieved.tier.name }) { index, achieved ->
                    AnimatedVisibility(visible = index < revealedRows, enter = badgeRevealEnter()) {
                        BadgeTierRow(
                            tier = achieved.tier,
                            status = BadgeRowStatus.OBTAINED,
                            requirementText = stringResource(R.string.badges_requirement_reports, achieved.tier.threshold),
                            showTopConnector = index > 0,
                            trailing = {
                                Text(
                                    text = achievedDateLabel(achieved.achievedAt),
                                    style = HimaTextStyles.m.copy(fontSize = 11.5.sp),
                                    color = colors.sage,
                                )
                            },
                            onClick = { detailTier = achieved },
                        )
                    }
                }
            }
            item {
                AnimatedVisibility(visible = sectionStage >= 4, enter = badgeRevealEnter()) {
                    ReportMoreBanner(modifier = Modifier.padding(top = 18.dp, bottom = 24.dp))
                }
            }
        }
    }

    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            containerColor = colors.surface,
            title = { Text(stringResource(R.string.badges_info_title), style = HimaTextStyles.h2, color = colors.ink) },
            text = { Text(stringResource(R.string.badges_info_message), style = HimaTextStyles.b, color = colors.ink2) },
            confirmButton = {
                TextButton(onClick = { showInfo = false }) {
                    Text(stringResource(R.string.common_close), color = colors.green)
                }
            },
        )
    }

    detailTier?.let { achieved ->
        AlertDialog(
            onDismissRequest = { detailTier = null },
            containerColor = colors.surface,
            title = { Text(stringResource(achieved.tier.nameRes), style = HimaTextStyles.h2, color = colors.ink) },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.badges_requirement_reports, achieved.tier.threshold),
                        style = HimaTextStyles.b,
                        color = colors.ink2,
                    )
                    Text(
                        text = achievedDateLabel(achieved.achievedAt),
                        style = HimaTextStyles.m,
                        color = colors.sage,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { detailTier = null }) {
                    Text(stringResource(R.string.common_close), color = colors.green)
                }
            },
        )
    }
}

internal fun badgeRevealEnter() =
    slideInVertically(tween(360, easing = FastOutSlowInEasing)) { it / 4 } + fadeIn(tween(360))

@Composable
internal fun achievedDateLabel(instant: Instant?): String {
    if (instant == null) return stringResource(R.string.badges_date_placeholder)
    val locale = LocalConfiguration.current.locales[0] ?: Locale.getDefault()
    val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", locale).withZone(ZoneId.systemDefault())
    return formatter.format(instant)
}

@Composable
private fun BadgeHeroCard(uiState: BadgesUiState, modifier: Modifier = Modifier) {
    val colors = LocalHimaColors.current
    val nextTier = uiState.nextTier
    val animatedProgress by animateFloatAsState(
        targetValue = uiState.progressFraction,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "badgeHeroProgress",
    )
    val animatedPercent = (animatedProgress * 100).toInt().coerceIn(0, 100)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(HimaRadius.hero))
            .background(Brush.linearGradient(listOf(colors.green, colors.greenDeep)))
            .padding(20.dp),
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                MedalBadge(tier = uiState.currentTier, size = 72.dp, glowing = true)
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.badges_current_badge_label),
                        style = HimaTextStyles.m.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium),
                        color = OnHero.copy(alpha = 0.74f),
                    )
                    Text(
                        text = stringResource(uiState.currentTier.nameRes),
                        style = HimaTextStyles.h1.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold),
                        color = OnHero,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        modifier = Modifier.padding(top = 8.dp),
                    ) {
                        Box(Modifier.size(7.dp).clip(CircleShape).background(OnHero.copy(alpha = 0.85f)))
                        Text(
                            text = stringResource(uiState.currentTier.statusRes),
                            style = HimaTextStyles.m.copy(fontSize = 12.sp),
                            color = OnHero.copy(alpha = 0.84f),
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 10.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.badges_points_label),
                            style = HimaTextStyles.m.copy(fontSize = 11.5.sp),
                            color = OnHero.copy(alpha = 0.68f),
                        )
                        AnimatedCount(
                            value = uiState.environmentalPoints,
                            style = HimaTextStyles.num.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                            color = OnHero,
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            if (nextTier != null) {
                Text(
                    text = stringResource(R.string.badges_progress_of_next, uiState.progressCount, nextTier.threshold),
                    style = HimaTextStyles.t.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                    color = OnHero,
                )
                Text(
                    text = stringResource(R.string.badges_progress_to_reach, stringResource(nextTier.nameRes)),
                    style = HimaTextStyles.m.copy(fontSize = 12.sp),
                    color = OnHero.copy(alpha = 0.74f),
                    modifier = Modifier.padding(top = 2.dp, bottom = 10.dp),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.16f)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(animatedProgress.coerceIn(0f, 1f))
                                .clip(RoundedCornerShape(50))
                                .background(OnHero),
                        )
                    }
                    Text(
                        text = stringResource(R.string.badges_percent_value, animatedPercent),
                        style = HimaTextStyles.num.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
                        color = OnHero,
                    )
                }
            } else {
                FullyAchievedRow()
            }
        }
    }
}

@Composable
private fun FullyAchievedRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(38.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_trophy),
                contentDescription = null,
                tint = OnHero,
                modifier = Modifier.size(20.dp),
            )
        }
        Column {
            Text(
                text = stringResource(R.string.badges_fully_achieved_title),
                style = HimaTextStyles.t.copy(fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold),
                color = OnHero,
            )
            Text(
                text = stringResource(R.string.badges_fully_achieved_subtitle),
                style = HimaTextStyles.m.copy(fontSize = 12.sp),
                color = OnHero.copy(alpha = 0.78f),
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
internal fun ReportMoreBanner(modifier: Modifier = Modifier) {
    val colors = LocalHimaColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(HimaRadius.card))
            .background(colors.bg2)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier.size(42.dp).clip(CircleShape).background(colors.green.copy(alpha = if (colors.isDark) 0.20f else 0.10f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_trophy),
                contentDescription = null,
                tint = colors.green,
                modifier = Modifier.size(22.dp),
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.badges_cta_title),
                style = HimaTextStyles.t.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                color = colors.ink,
            )
            Text(
                text = stringResource(R.string.badges_cta_subtitle),
                style = HimaTextStyles.m.copy(fontSize = 12.sp, lineHeight = 17.sp),
                color = colors.sage,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
        Icon(
            painter = painterResource(R.drawable.ic_plant_hand),
            contentDescription = null,
            tint = colors.green,
            modifier = Modifier.size(30.dp),
        )
    }
}
