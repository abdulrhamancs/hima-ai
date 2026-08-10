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
            return severityFilter?.let { severity ->
                byStatus.filter { it.category != IncidentCategory.WASTE && it.severity == severity }
            } ?: byStatus
        }
}

private data class HistorySelection(
    val filter: HistoryFilter = HistoryFilter.ALL,
    val severity: Severity? = null,
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
        selection.update { it.copy(severity = severity?.takeUnless { it == Severity.UNKNOWN }) }
    }

    fun onRetry() {
        viewModelScope.launch { reportsRepository.refresh(force = true) }
    }
}
