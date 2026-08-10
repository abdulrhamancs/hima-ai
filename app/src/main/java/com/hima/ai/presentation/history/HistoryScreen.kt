package com.hima.ai.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hima.ai.R
import com.hima.ai.core.designsystem.component.FilterSegments
import com.hima.ai.core.designsystem.component.HimaBottomNavigation
import com.hima.ai.core.designsystem.component.HimaIconButton
import com.hima.ai.core.designsystem.component.HimaTab
import com.hima.ai.core.designsystem.component.HimaTextLink
import com.hima.ai.core.designsystem.component.ReportRow
import com.hima.ai.core.designsystem.component.ScreenHeader
import com.hima.ai.core.designsystem.theme.HimaTextStyles
import com.hima.ai.core.designsystem.theme.LocalHimaColors
import com.hima.ai.domain.model.Severity
import com.hima.ai.domain.repository.ReportsLoadState

/**
 * Reports history — one segmented filter over a flat list. Rows are the same
 * component Home uses, so a report looks identical wherever it appears.
 */
@Composable
fun HistoryScreen(
    onBackClick: () -> Unit,
    onReportClick: (String) -> Unit,
    onHomeClick: () -> Unit,
    onNewReportClick: () -> Unit,
    onMapClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel(),
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
            title = stringResource(R.string.history_title),
            onBackClick = onBackClick,
            trailing = {
                SeverityFilterButton(
                    selected = uiState.severityFilter,
                    onSelect = viewModel::onSeverityFilterSelected,
                )
            },
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        FilterSegments(
            options = listOf(
                stringResource(R.string.history_filter_all),
                stringResource(R.string.history_filter_open),
                stringResource(R.string.history_filter_done),
            ),
            selectedIndex = uiState.filter.ordinal,
            onSelect = viewModel::onFilterSelected,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        )

        val reports = uiState.visibleReports
        if (uiState.loadState == ReportsLoadState.Loading && uiState.allReports.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = colors.green, modifier = Modifier.size(26.dp))
            }
        } else if (uiState.loadState is ReportsLoadState.Error && uiState.allReports.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(R.string.reports_load_error),
                    style = HimaTextStyles.b,
                    color = colors.sage,
                    textAlign = TextAlign.Center,
                )
                HimaTextLink(
                    text = stringResource(R.string.common_retry),
                    onClick = viewModel::onRetry,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        } else if (reports.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(
                        if (uiState.allReports.isEmpty()) R.string.reports_empty else R.string.history_empty,
                    ),
                    style = HimaTextStyles.b,
                    color = colors.sage,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                items(reports, key = { it.id }) { report ->
                    ReportRow(report = report, onClick = { onReportClick(report.id) })
                }
            }
        }

        HimaBottomNavigation(
            selected = HimaTab.REPORTS,
            onHomeClick = onHomeClick,
            onMapClick = onMapClick,
            onNewReportClick = onNewReportClick,
            onReportsClick = {},
            onMoreClick = onMoreClick,
        )
    }
}

/**
 * The filter icon, tinted green while a severity filter is active so the
 * header itself shows the list is narrowed — not just the empty state below.
 */
@Composable
private fun SeverityFilterButton(
    selected: Severity?,
    onSelect: (Severity?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalHimaColors.current
    var expanded by remember { mutableStateOf(false) }

    Box(modifier) {
        HimaIconButton(
            iconRes = R.drawable.ic_filter,
            contentDescription = stringResource(R.string.cd_filter),
            onClick = { expanded = true },
            tint = if (selected != null) colors.green else null,
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.history_filter_all)) },
                onClick = { onSelect(null); expanded = false },
            )
            Severity.entries.filterNot { it == Severity.UNKNOWN }.forEach { severity ->
                DropdownMenuItem(
                    text = { Text(stringResource(severity.labelRes)) },
                    onClick = { onSelect(severity); expanded = false },
                )
            }
        }
    }
}
