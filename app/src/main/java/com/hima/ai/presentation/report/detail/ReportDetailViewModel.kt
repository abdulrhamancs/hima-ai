package com.hima.ai.presentation.report.detail

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.hima.ai.R
import com.hima.ai.core.navigation.HimaDestinations
import com.hima.ai.data.mock.MockData
import com.hima.ai.data.mock.PrototypeSession
import com.hima.ai.data.mock.ReportDraft
import com.hima.ai.domain.model.ReportSummary
import com.hima.ai.domain.model.Severity
import com.hima.ai.domain.repository.ReportsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ReportDetailUiState(
    @StringRes val kindRes: Int = R.string.incident_logging,
    @StringRes val locationRes: Int = R.string.loc_tuwaiq,
    @StringRes val timeRes: Int = R.string.time_may7,
    /** The AI's own text for what the photo shows, when this came from a real analysis. */
    val kindOverride: String? = null,
    /** Formatted coordinates, for a real report opened by id — no place-name geocoding here. */
    val locationOverride: String? = null,
    /** A real report's actual timestamp — rendered via relativeTimeLabel instead of [timeRes]. */
    val createdAt: Instant? = null,
    val riskScore: String = "",
    val escalated: Boolean = false,
    val severity: Severity = Severity.HIGH,
    val reasonOverride: String? = null,
    val recommendationOverride: String? = null,
    val saved: Boolean = false,
    /** True when opened from a list/marker rather than produced by the analysis flow. */
    val isExistingReport: Boolean = false,
)

/**
 * Final report state.
 *
 * Two cases share this screen. Without a report id the screen shows the
 * report the analysis flow just produced — real data from `/analyze`, read
 * from [ReportDraft] — whose risk score/severity [PrototypeSession] can still
 * escalate, exactly as before, just layered on the real numbers now instead
 * of a fixed mock pair. With an id it shows a stored report instead — either
 * a mock one, or (since the map now shows real Supabase reports) a real one,
 * already held by [ReportsRepository] from when the map fetched it — so
 * opening a row from Home, History, or a map marker shows *that* report,
 * with its real title/severity/reason instead of a generic placeholder.
 */
@HiltViewModel
class ReportDetailViewModel @Inject constructor(
    private val session: PrototypeSession,
    private val draft: ReportDraft,
    private val reportsRepository: ReportsRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val existingReport: ReportSummary? =
        savedStateHandle.get<String>(HimaDestinations.REPORT_ARG_ID)
            ?.takeIf { it.isNotBlank() }
            ?.let { id -> MockData.findReport(id) ?: reportsRepository.findById(id) }

    private val _uiState = MutableStateFlow(buildState())
    val uiState: StateFlow<ReportDetailUiState> = _uiState.asStateFlow()

    private fun buildState(saved: Boolean = false): ReportDetailUiState {
        val report = existingReport
        if (report != null) {
            // A stored report: its own details, and a base risk score — the
            // session's escalation belongs to the report being analysed, not
            // to one opened from history.
            return ReportDetailUiState(
                kindRes = report.titleRes,
                kindOverride = report.titleOverride,
                locationRes = report.locationRes,
                locationOverride = report.locationOverride,
                timeRes = report.timeRes,
                createdAt = report.createdAt,
                riskScore = report.riskScore?.let { "$it / 100" } ?: MockData.RISK_SCORE,
                escalated = false,
                severity = report.severity,
                reasonOverride = report.reasonOverride,
                recommendationOverride = report.recommendationOverride,
                saved = saved,
                isExistingReport = true,
            )
        }

        val analysis = draft.analysisResult.value
        val escalated = session.escalated.value
        // Escalating used to jump to a fixed mock pair (8.7 -> 9.1). With a
        // real 0-100 score that pair no longer fits any scale, so escalation
        // now bumps the real score instead, capped at 100.
        val riskScoreText = analysis?.riskScore
            ?.let { (if (escalated) (it + 10).coerceAtMost(100) else it).toString() + " / 100" }
            ?: session.riskScore.value

        return ReportDetailUiState(
            kindOverride = analysis?.issueType,
            riskScore = riskScoreText,
            escalated = escalated,
            severity = if (escalated) Severity.CRITICAL else (analysis?.riskLevel ?: Severity.HIGH),
            reasonOverride = analysis?.description,
            recommendationOverride = analysis?.recommendation,
            saved = saved,
            isExistingReport = false,
        )
    }

    /** Re-reads session state each time the screen is shown (after the investigation). */
    fun refresh() {
        if (existingReport != null) return
        _uiState.value = buildState(saved = _uiState.value.saved)
    }

    fun onSave() {
        _uiState.update { it.copy(saved = true) }
    }
}
