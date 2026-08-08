package com.hima.ai.presentation.report.detail

import androidx.lifecycle.ViewModel
import com.hima.ai.data.mock.PrototypeSession
import com.hima.ai.domain.model.Severity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ReportDetailUiState(
    val riskScore: String = "",
    val escalated: Boolean = false,
    val severity: Severity = Severity.HIGH,
    val saved: Boolean = false,
)

/**
 * Final report state. The risk score and severity read from [PrototypeSession],
 * so answers given in the AI investigation are reflected here when the ranger
 * comes back.
 */
@HiltViewModel
class ReportDetailViewModel @Inject constructor(
    private val session: PrototypeSession,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ReportDetailUiState(
            riskScore = session.riskScore.value,
            escalated = session.escalated.value,
            severity = if (session.escalated.value) Severity.CRITICAL else Severity.HIGH,
        ),
    )
    val uiState: StateFlow<ReportDetailUiState> = _uiState.asStateFlow()

    /** Re-reads session state each time the screen is shown. */
    fun refresh() {
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
