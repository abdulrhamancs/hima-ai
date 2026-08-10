package com.hima.ai.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hima.ai.domain.model.IncidentCategory
import com.hima.ai.domain.model.ReportStatus
import com.hima.ai.domain.model.ReportSummary
import com.hima.ai.domain.model.Severity
import com.hima.ai.domain.repository.ReportsLoadState
import com.hima.ai.domain.repository.ReportsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class HistoryFilter { ALL, OPEN, RESOLVED }

data class HistoryUiState(
    val filter: HistoryFilter = HistoryFilter.ALL,
    val severityFilter: Severity? = null,
    val categoryFilter: IncidentCategory? = null,
    val allReports: List<ReportSummary> = emptyList(),
    val loadState: ReportsLoadState = ReportsLoadState.Idle,
) {
    val visibleReports: List<ReportSummary>
        get() {
            val byStatus = when (filter) {
                HistoryFilter.ALL -> allReports
                HistoryFilter.OPEN -> allReports.filter { it.status == ReportStatus.OPEN }
                HistoryFilter.RESOLVED -> allReports.filter { it.status == ReportStatus.RESOLVED }
            }
            val bySeverity = severityFilter?.let { severity ->
                byStatus.filter { it.category != IncidentCategory.WASTE && it.severity == severity }
            } ?: byStatus
            return categoryFilter?.let { category ->
                bySeverity.filter { it.category == category }
            } ?: bySeverity
        }
}

private data class HistorySelection(
    val filter: HistoryFilter = HistoryFilter.ALL,
    val severity: Severity? = null,
    val category: IncidentCategory? = null,
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val reportsRepository: ReportsRepository,
) : ViewModel() {

    private val selection = MutableStateFlow(HistorySelection())

    val uiState: StateFlow<HistoryUiState> = combine(
        reportsRepository.reports,
        reportsRepository.loadState,
        selection,
    ) { reports, loadState, selected ->
        HistoryUiState(
            filter = selected.filter,
            severityFilter = selected.severity,
            categoryFilter = selected.category,
            allReports = reports,
            loadState = loadState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryUiState(loadState = reportsRepository.loadState.value),
    )

    init {
        viewModelScope.launch { reportsRepository.refresh() }
    }

    fun onFilterSelected(index: Int) {
        val filter = HistoryFilter.entries.getOrNull(index) ?: return
        selection.update { it.copy(filter = filter) }
    }

    fun onSeverityFilterSelected(severity: Severity?) {
        val selectedSeverity = severity?.takeUnless { it == Severity.UNKNOWN }
        selection.update {
            it.copy(
                severity = selectedSeverity,
                category = if (selectedSeverity != null && it.category == IncidentCategory.WASTE) null else it.category,
            )
        }
    }

    fun onCategoryFilterSelected(index: Int) {
        val category = if (index == 0) null else IncidentCategory.entries.getOrNull(index - 1)
        selection.update {
            it.copy(
                category = category,
                severity = if (category == IncidentCategory.WASTE) null else it.severity,
            )
        }
    }

    fun onRetry() {
        viewModelScope.launch { reportsRepository.refresh(force = true) }
    }

    fun onRefresh() {
        viewModelScope.launch { reportsRepository.refresh(force = true) }
    }
}
