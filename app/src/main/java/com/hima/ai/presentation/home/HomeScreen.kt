package com.hima.ai.presentation.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hima.ai.R
import com.hima.ai.core.designsystem.component.HimaBottomNavigation
import com.hima.ai.core.designsystem.component.HimaIconButton
import com.hima.ai.core.designsystem.component.HimaTab
import com.hima.ai.core.designsystem.component.HimaTextLink
import com.hima.ai.core.designsystem.component.LanguageToggle
import com.hima.ai.core.designsystem.component.ReportRow
import com.hima.ai.core.designsystem.component.ReportRowSkeleton
import com.hima.ai.core.designsystem.component.SectionHeader
import com.hima.ai.core.designsystem.component.StatsRow
import com.hima.ai.core.designsystem.component.StatusIndicator
import com.hima.ai.core.designsystem.theme.HimaRadius
import com.hima.ai.core.designsystem.theme.HimaTextStyles
import com.hima.ai.core.designsystem.theme.LocalHimaColors
import com.hima.ai.domain.model.ReportSummary
import com.hima.ai.domain.repository.ReportsLoadState
import kotlin.math.roundToInt

/** Home — live reserve health, real repository counters, and latest reports. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNewReportClick: () -> Unit,
    onReportClick: (String) -> Unit,
    onViewAllClick: () -> Unit,
    onMapClick: () -> Unit,
    onViewReportOnMapClick: (String) -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalHimaColors.current
    var showNotifications by remember { mutableStateOf(false) }
    val hasReportData = uiState.loadState == ReportsLoadState.Ready || uiState.totalReports > 0
    val resolvedPercentage = calculateResolvedReportsPercentage(
        totalReports = uiState.totalReports,
        resolvedReports = uiState.resolvedReports,
        hasReportData = hasReportData,
    )
    val monitoredLocation = uiState.recentReports.firstOrNull()?.let { report ->
        report.locationOverride ?: stringResource(report.locationRes)
    } ?: stringResource(R.string.home_monitoring_scope)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        PullToRefreshBox(
            isRefreshing = uiState.loadState == ReportsLoadState.Loading && uiState.recentReports.isNotEmpty(),
            onRefresh = viewModel::onRefresh,
            modifier = Modifier.weight(1f),
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp),
                contentPadding = PaddingValues(top = 52.dp, bottom = 26.dp),
            ) {
            item {
                HomeHeader(
                    onMenuClick = onMoreClick,
                    onNotificationsClick = { showNotifications = true },
                )
            }

            item {
                StatusIndicator(
                    label = stringResource(R.string.home_reserve_status),
                    value = stringResource(
                        when {
                            !hasReportData -> R.string.home_reserve_status_unknown
                            uiState.criticalAlerts > 0 -> R.string.home_reserve_status_attention
                            else -> R.string.home_reserve_status_value
                        },
                    ),
                    note = when {
                        !hasReportData -> stringResource(R.string.home_reserve_status_loading)
                        uiState.criticalAlerts > 0 -> stringResource(
                            R.string.home_reserve_status_critical_note,
                            uiState.criticalAlerts,
                        )
                        else -> stringResource(R.string.home_reserve_status_note)
                    },
                    location = monitoredLocation,
                    resolvedPercentage = resolvedPercentage,
                    resolvedPercentageLabel = resolvedPercentage?.let {
                        stringResource(R.string.home_resolved_percentage, it)
                    } ?: stringResource(R.string.home_reserve_status_unknown),
                    resolvedLabel = stringResource(R.string.home_resolved_percentage_label),
                    modifier = Modifier.padding(top = 18.dp),
                )
            }

            item {
                val unavailable = stringResource(R.string.home_reserve_status_unknown)
                StatsRow(
                    items = listOf(
                        (if (hasReportData) uiState.totalReports.toString() else unavailable) to stringResource(R.string.home_stat_total),
                        (if (hasReportData) uiState.openReports.toString() else unavailable) to stringResource(R.string.home_stat_open),
                        (if (hasReportData) uiState.resolvedReports.toString() else unavailable) to stringResource(R.string.home_stat_done),
                        (if (hasReportData) uiState.criticalAlerts.toString() else unavailable) to stringResource(R.string.home_stat_critical),
                    ),
                    iconResIds = listOf(
                        R.drawable.ic_tab_reports,
                        R.drawable.ic_clock,
                        R.drawable.ic_check,
                        R.drawable.ic_bell,
                    ),
                    emphasisIndex = 3,
                    emphasisColor = colors.severityCritical,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }

            item {
                SectionHeader(
                    title = stringResource(R.string.home_recent),
                    actionLabel = stringResource(R.string.home_view_all),
                    onActionClick = onViewAllClick,
                )
            }

            when {
                uiState.loadState == ReportsLoadState.Loading && uiState.recentReports.isEmpty() -> item {
                    Column {
                        repeat(3) { ReportRowSkeleton() }
                    }
                }
                uiState.loadState is ReportsLoadState.Error && uiState.recentReports.isEmpty() -> item {
                    ReportsNotice(
                        text = stringResource(R.string.reports_load_error),
                        action = stringResource(R.string.common_retry),
                        onAction = viewModel::onRetry,
                    )
                }
                uiState.loadState == ReportsLoadState.Ready && uiState.recentReports.isEmpty() -> item {
                    ReportsNotice(text = stringResource(R.string.reports_empty))
                }
                else -> items(uiState.recentReports, key = { it.id }) { report ->
                    ReportRow(
                        report = report,
                        onClick = { onReportClick(report.id) },
                        onViewOnMapClick = { onViewReportOnMapClick(report.id) },
                    )
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
            }
        }

        HimaBottomNavigation(
            selected = HimaTab.HOME,
            onHomeClick = {},
            onMapClick = onMapClick,
            onNewReportClick = onNewReportClick,
            onReportsClick = onViewAllClick,
            onMoreClick = onMoreClick,
        )
    }

    if (showNotifications) {
        NotificationsSheet(
            reports = uiState.recentReports,
            onDismiss = { showNotifications = false },
            onReportClick = { id ->
                showNotifications = false
                onReportClick(id)
            },
        )
    }
}

@Composable
private fun ReportsNotice(
    text: String,
    action: String? = null,
    onAction: () -> Unit = {},
) {
    val colors = LocalHimaColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = text, style = HimaTextStyles.b, color = colors.sage, textAlign = TextAlign.Center)
        action?.let {
            HimaTextLink(text = it, onClick = onAction, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun HomeHeader(
    onMenuClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalHimaColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        HimaIconButton(
            iconRes = R.drawable.ic_menu,
            contentDescription = stringResource(R.string.cd_menu),
            onClick = onMenuClick,
            filled = true,
        )
        Box(
            modifier = Modifier
                .size(44.dp)
                .shadow(
                    elevation = if (colors.isDark) 1.dp else 5.dp,
                    shape = RoundedCornerShape(15.dp),
                )
                .clip(RoundedCornerShape(15.dp))
                .background(colors.surface),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_hima_mark),
                contentDescription = null,
                tint = colors.green,
                modifier = Modifier.size(31.dp),
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.home_greeting),
                style = HimaTextStyles.h2,
                color = colors.ink,
            )
            Text(
                text = stringResource(R.string.home_greeting_sub),
                style = HimaTextStyles.m,
                color = colors.sage,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        LanguageToggle()
        HimaIconButton(
            iconRes = R.drawable.ic_bell,
            contentDescription = stringResource(R.string.cd_notifications),
            onClick = onNotificationsClick,
            filled = true,
            badged = true,
        )
    }
}

/** The reserve's latest reports, reused as the notification feed — same rows, same data. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationsSheet(
    reports: List<ReportSummary>,
    onDismiss: () -> Unit,
    onReportClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalHimaColors.current
    // Without this, system Back skips the sheet and pops Home itself — on the
    // root screen that exits the app instead of just closing notifications.
    BackHandler(onBack = onDismiss)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = colors.surface,
        shape = RoundedCornerShape(topStart = HimaRadius.sheet, topEnd = HimaRadius.sheet),
        modifier = modifier,
    ) {
        Column(Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = stringResource(R.string.notifications_title),
                style = HimaTextStyles.h2.copy(fontSize = 17.sp),
                color = colors.ink,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            if (reports.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                ) {
                    Text(
                        text = stringResource(R.string.notifications_empty),
                        style = HimaTextStyles.b,
                        color = colors.sage,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                reports.forEach { report ->
                    ReportRow(report = report, onClick = { onReportClick(report.id) })
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

internal fun calculateResolvedReportsPercentage(
    totalReports: Int,
    resolvedReports: Int,
    hasReportData: Boolean,
): Int? = when {
    !hasReportData -> null
    totalReports <= 0 -> 0
    else -> ((resolvedReports.coerceAtLeast(0) * 100f) / totalReports).roundToInt().coerceIn(0, 100)
}
