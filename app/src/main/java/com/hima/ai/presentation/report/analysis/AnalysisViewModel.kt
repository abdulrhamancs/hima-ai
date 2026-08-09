package com.hima.ai.presentation.report.analysis

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hima.ai.core.common.ApiResult
import com.hima.ai.data.mock.ReportDraft
import com.hima.ai.domain.model.AiAnalysis
import com.hima.ai.domain.repository.AiAnalysisRepository
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
    val imageUri: Uri? = null,
    val result: AiAnalysis? = null,
    val errorMessage: String? = null,
) {
    val totalSteps: Int get() = 3
}

/**
 * Sends the evidence photo to the backend's `/analyze` and drives the same
 * three-stage progress the approved design shows. The first two stages are a
 * fixed-timer "thinking" beat — there is no incremental progress from a
 * single HTTP call to reflect — but the third, "estimating severity", only
 * completes once the real result actually exists, so the animation never
 * claims to know something it doesn't yet.
 */
@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val draft: ReportDraft,
    private val aiAnalysisRepository: AiAnalysisRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalysisUiState(imageUri = draft.imageUri.value))
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    init {
        runAnalysis()
    }

    fun onRetry() {
        if (_uiState.value.errorMessage == null) return
        _uiState.value = AnalysisUiState(imageUri = draft.imageUri.value)
        runAnalysis()
    }

    private fun runAnalysis() {
        val imageUri = draft.imageUri.value
        if (imageUri == null) {
            // Unreachable via the UI — New report only enables "Analyze" once
            // a photo exists — but fail loudly rather than hang if it happens.
            _uiState.update { it.copy(errorMessage = "No photo was attached to analyse.") }
            return
        }

        viewModelScope.launch {
            val stepTimerJob = launch {
                delay(STEP_DELAY_MS)
                _uiState.update { it.copy(stepsDone = 1) }
                delay(STEP_DELAY_MS)
                _uiState.update { it.copy(stepsDone = 2) }
            }

            val result = aiAnalysisRepository.analyzeImage(
                imageUri = imageUri,
                description = draft.description.value.ifBlank { null },
            )
            stepTimerJob.join()

            when (result) {
                is ApiResult.Success -> {
                    draft.setAnalysisResult(result.value)
                    _uiState.update { it.copy(stepsDone = 3, result = result.value) }
                    delay(SETTLE_DELAY_MS)
                    _uiState.update { it.copy(isComplete = true) }
                }
                is ApiResult.Failure -> _uiState.update { it.copy(errorMessage = result.error.message) }
            }
        }
    }

    private companion object {
        const val STEP_DELAY_MS = 900L
        const val SETTLE_DELAY_MS = 500L
    }
}
