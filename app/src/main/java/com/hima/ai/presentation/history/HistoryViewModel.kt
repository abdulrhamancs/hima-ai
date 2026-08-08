package com.hima.ai.presentation.history

import androidx.lifecycle.ViewModel
import com.hima.ai.data.mock.MockData
import com.hima.ai.domain.model.ReportStatus
import com.hima.ai.domain.model.ReportSummary
import com.hima.ai.domain.model.Severity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** The three history filters, in display order. */
enum class HistoryFilter { ALL, OPEN, RESOLVED }

data class HistoryUiState(
    val filter: HistoryFilter = HistoryFilter.ALL,
    val severityFilter: Severity? = null,
    val allReports: List<ReportSummary> = emptyList(),
) {
    /** Reports matching the active status filter and, if set, severity filter. */
    val visibleReports: List<ReportSummary>
        get() {
            val byStatus = when (filter) {
                HistoryFilter.ALL -> allReports
                HistoryFilter.OPEN -> allReports.filter { it.status == ReportStatus.OPEN }
                HistoryFilter.RESOLVED -> allReports.filter { it.status == ReportStatus.RESOLVED }
            }
            return if (severityFilter == null) byStatus else byStatus.filter { it.severity == severityFilter }
        }
}

@HiltViewModel
class HistoryViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState(allReports = MockData.allReports))
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    fun onFilterSelected(index: Int) {
        val filter = HistoryFilter.entries.getOrNull(index) ?: return
        _uiState.update { it.copy(filter = filter) }
    }

    /** `null` clears the severity filter (shows all severities). */
    fun onSeverityFilterSelected(severity: Severity?) {
        _uiState.update { it.copy(severityFilter = severity) }
    }
}
