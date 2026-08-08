package com.hima.ai.presentation.report.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hima.ai.data.mock.MockData
import com.hima.ai.domain.model.Severity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AnalysisUiState(
    val stepsDone: Int = 0,
    val isComplete: Boolean = false,
    val severity: Severity = Severity.HIGH,
    val confidence: Int = MockData.CONFIDENCE_PERCENT,
) {
    val totalSteps: Int get() = 3
}

/**
 * Drives the simulated analysis run: three stages complete in sequence, then
 * the screen reports it is finished. Replacing this with a real Gemini call
 * later means emitting the same states from the API response instead of a timer.
 */
@HiltViewModel
class AnalysisViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(AnalysisUiState())
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repeat(_uiState.value.totalSteps) {
                delay(STEP_DELAY_MS)
                _uiState.update { state -> state.copy(stepsDone = state.stepsDone + 1) }
            }
            delay(SETTLE_DELAY_MS)
            _uiState.update { it.copy(isComplete = true) }
        }
    }

    private companion object {
        const val STEP_DELAY_MS = 900L
        const val SETTLE_DELAY_MS = 700L
    }
}
