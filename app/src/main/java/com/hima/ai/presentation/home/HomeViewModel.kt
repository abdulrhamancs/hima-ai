package com.hima.ai.presentation.home

import androidx.lifecycle.ViewModel
import com.hima.ai.data.mock.MockData
import com.hima.ai.domain.model.ReportSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HomeUiState(
    val totalReports: Int = 0,
    val openReports: Int = 0,
    val resolvedReports: Int = 0,
    val criticalAlerts: Int = 0,
    val recentReports: List<ReportSummary> = emptyList(),
)

/**
 * Home state. Backed by [MockData] for the prototype; swapping in a Firestore
 * repository later means changing only this class — the screen already reads
 * domain models.
 */
@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            totalReports = MockData.TOTAL_REPORTS,
            openReports = MockData.OPEN_REPORTS,
            resolvedReports = MockData.RESOLVED_REPORTS,
            criticalAlerts = MockData.CRITICAL_ALERTS,
            recentReports = MockData.recentReports,
        ),
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
}
