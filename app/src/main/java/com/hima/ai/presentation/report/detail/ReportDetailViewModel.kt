package com.hima.ai.presentation.report.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hima.ai.core.navigation.HimaDestinations
import com.hima.ai.domain.model.ReportSummary
import com.hima.ai.domain.repository.ReportsLoadState
import com.hima.ai.domain.repository.ReportsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ReportDetailUiState(
    val report: ReportSummary? = null,
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
    val notFound: Boolean = false,
)

/** Resolves Detail from the same persisted report object used by every other screen. */
@HiltViewModel
class ReportDetailViewModel @Inject constructor(
    private val reportsRepository: ReportsRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val reportId = savedStateHandle.get<String>(HimaDestinations.REPORT_ARG_ID)
        ?.takeIf { it.isNotBlank() }

    val uiState: StateFlow<ReportDetailUiState> = combine(
        reportsRepository.reports,
        reportsRepository.loadState,
    ) { reports, loadState ->
        val report = reportId?.let { id -> reports.firstOrNull { it.id == id } }
        ReportDetailUiState(
            report = report,
            isLoading = loadState == ReportsLoadState.Loading && report == null,
            hasError = loadState is ReportsLoadState.Error && report == null,
            notFound = reportId == null || (loadState == ReportsLoadState.Ready && report == null),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ReportDetailUiState(isLoading = reportId != null, notFound = reportId == null),
    )

    init {
        viewModelScope.launch { reportsRepository.refresh() }
    }

    fun onRetry() {
        viewModelScope.launch { reportsRepository.refresh(force = true) }
    }
}
