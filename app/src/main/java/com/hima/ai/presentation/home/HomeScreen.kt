package com.hima.ai.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hima.ai.R
import com.hima.ai.core.designsystem.component.HimaBottomNavigation
import com.hima.ai.core.designsystem.component.HimaIconButton
import com.hima.ai.core.designsystem.component.HimaTab
import com.hima.ai.core.designsystem.component.LanguageToggle
import com.hima.ai.core.designsystem.component.ReportRow
import com.hima.ai.core.designsystem.component.SectionHeader
import com.hima.ai.core.designsystem.component.StatsRow
import com.hima.ai.core.designsystem.component.StatusIndicator
import com.hima.ai.core.designsystem.theme.HimaTextStyles
import com.hima.ai.core.designsystem.theme.LocalHimaColors

/**
 * Home — greeting, reserve health, compact counters, and the latest reports.
 * Sections are separated by whitespace and one warm surface rather than being
 * wrapped in individual cards.
 */
@Composable
fun HomeScreen(
    onNewReportClick: () -> Unit,
    onReportClick: (String) -> Unit,
    onViewAllClick: () -> Unit,
    onMapClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalHimaColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 58.dp, bottom = 24.dp),
        ) {
            item { HomeHeader() }

            item {
                StatusIndicator(
                    label = stringResource(R.string.home_reserve_status),
                    value = stringResource(R.string.home_reserve_status_value),
                    note = stringResource(R.string.home_reserve_status_note),
                    modifier = Modifier.padding(top = 18.dp),
                )
            }

            item {
                StatsRow(
                    items = listOf(
                        uiState.totalReports.toString() to stringResource(R.string.home_stat_total),
                        uiState.openReports.toString() to stringResource(R.string.home_stat_open),
                        uiState.resolvedReports.toString() to stringResource(R.string.home_stat_done),
                        uiState.criticalAlerts.toString() to stringResource(R.string.home_stat_critical),
                    ),
                    emphasisIndex = 3,
                    emphasisColor = colors.severityCritical,
                    modifier = Modifier.padding(top = 18.dp),
                )
            }

            item {
                SectionHeader(
                    title = stringResource(R.string.home_recent),
                    actionLabel = stringResource(R.string.home_view_all),
                    onActionClick = onViewAllClick,
                )
            }

            items(uiState.recentReports, key = { it.id }) { report ->
                ReportRow(report = report, onClick = { onReportClick(report.id) })
            }

            item { Spacer(Modifier.height(8.dp)) }
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
}

@Composable
private fun HomeHeader(modifier: Modifier = Modifier) {
    val colors = LocalHimaColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        HimaIconButton(
            iconRes = R.drawable.ic_menu,
            contentDescription = stringResource(R.string.cd_menu),
            onClick = {},
            filled = true,
        )
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
            onClick = {},
            filled = true,
            badged = true,
        )
    }
}
