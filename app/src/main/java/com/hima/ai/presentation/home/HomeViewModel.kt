package com.hima.ai.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hima.ai.domain.model.ReportStatus
import com.hima.ai.domain.model.ReportSummary
import com.hima.ai.domain.model.Severity
import com.hima.ai.domain.repository.ReportsLoadState
import com.hima.ai.domain.repository.ReportsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val totalReports: Int = 0,
    val openReports: Int = 0,
    val resolvedReports: Int = 0,
    val criticalAlerts: Int = 0,
    val recentReports: List<ReportSummary> = emptyList(),
    val loadState: ReportsLoadState = ReportsLoadState.Idle,
)

/** Home derives both its counters and newest rows from the shared real repository. */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val reportsRepository: ReportsRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        reportsRepository.reports,
        reportsRepository.loadState,
    ) { reports, loadState ->
        HomeUiState(
            totalReports = reports.size,
            openReports = reports.count { it.status == ReportStatus.OPEN },
            resolvedReports = reports.count { it.status == ReportStatus.RESOLVED },
            criticalAlerts = reports.count { it.severity == Severity.CRITICAL },
            recentReports = reports.take(3),
            loadState = loadState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(loadState = reportsRepository.loadState.value),
    )

    init {
        viewModelScope.launch { reportsRepository.refresh() }
    }

    fun onRetry() {
        viewModelScope.launch { reportsRepository.refresh(force = true) }
    }
}
