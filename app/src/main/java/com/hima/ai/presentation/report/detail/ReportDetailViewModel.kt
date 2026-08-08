package com.hima.ai.presentation.report.detail

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.hima.ai.R
import com.hima.ai.core.navigation.HimaDestinations
import com.hima.ai.data.mock.MockData
import com.hima.ai.data.mock.PrototypeSession
import com.hima.ai.domain.model.ReportSummary
import com.hima.ai.domain.model.Severity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ReportDetailUiState(
    @StringRes val kindRes: Int = R.string.incident_logging,
    @StringRes val locationRes: Int = R.string.loc_tuwaiq,
    @StringRes val timeRes: Int = R.string.time_may7,
    val riskScore: String = "",
    val escalated: Boolean = false,
    val severity: Severity = Severity.HIGH,
    val saved: Boolean = false,
    /** True when opened from a list/marker rather than produced by the analysis flow. */
    val isExistingReport: Boolean = false,
)

/**
 * Final report state.
 *
 * Two cases share this screen. Without a report id the screen shows the report
 * the analysis flow just produced, whose risk score reads from
 * [PrototypeSession] so answers given in the AI investigation are reflected
 * here. With an id it shows that stored report instead, so opening a row from
 * Home, History, or a map marker shows *that* report rather than the flow's.
 */
@HiltViewModel
class ReportDetailViewModel @Inject constructor(
    private val session: PrototypeSession,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val existingReport: ReportSummary? =
        savedStateHandle.get<String>(HimaDestinations.REPORT_ARG_ID)
            ?.takeIf { it.isNotBlank() }
            ?.let(MockData::findReport)

    private val _uiState = MutableStateFlow(buildState())
    val uiState: StateFlow<ReportDetailUiState> = _uiState.asStateFlow()

    private fun buildState(saved: Boolean = false): ReportDetailUiState {
        val report = existingReport
        return if (report != null) {
            // A stored report: its own details, and a base risk score — the
            // session's escalation belongs to the report being analysed, not
            // to one opened from history.
            ReportDetailUiState(
                kindRes = report.titleRes,
                locationRes = report.locationRes,
                timeRes = report.timeRes,
                riskScore = MockData.RISK_SCORE,
                escalated = false,
                severity = report.severity,
                saved = saved,
                isExistingReport = true,
            )
        } else {
            ReportDetailUiState(
                riskScore = session.riskScore.value,
                escalated = session.escalated.value,
                severity = if (session.escalated.value) Severity.CRITICAL else Severity.HIGH,
                saved = saved,
                isExistingReport = false,
            )
        }
    }

    /** Re-reads session state each time the screen is shown (after the investigation). */
    fun refresh() {
        if (existingReport != null) return
        _uiState.update {
            it.copy(
                riskScore = session.riskScore.value,
                escalated = session.escalated.value,
                severity = if (session.escalated.value) Severity.CRITICAL else Severity.HIGH,
            )
        }
    }

    fun onSave() {
        _uiState.update { it.copy(saved = true) }
    }
}
