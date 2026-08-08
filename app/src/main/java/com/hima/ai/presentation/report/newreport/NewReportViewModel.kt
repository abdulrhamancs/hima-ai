package com.hima.ai.presentation.report.newreport

import androidx.lifecycle.ViewModel
import com.hima.ai.data.mock.PrototypeSession
import com.hima.ai.domain.model.SceneKind
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class NewReportUiState(
    val selectedScene: SceneKind? = null,
    val description: String = "",
) {
    /** The analyse action stays disabled until there is something to analyse. */
    val canAnalyze: Boolean get() = selectedScene != null
    val currentStep: Int get() = if (selectedScene == null) 1 else 2
}

/**
 * New report state. Image capture is simulated for the prototype: both the
 * camera and gallery actions resolve to a generated scene, standing in for a
 * real CameraX capture or photo-picker result.
 */
@HiltViewModel
class NewReportViewModel @Inject constructor(
    session: PrototypeSession,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewReportUiState())
    val uiState: StateFlow<NewReportUiState> = _uiState.asStateFlow()

    init {
        // Each new report starts from the base risk score, not the previous run's.
        session.reset()
    }

    fun onImageSelected() {
        _uiState.update { it.copy(selectedScene = SceneKind.STUMP) }
    }

    fun onClearImage() {
        _uiState.update { it.copy(selectedScene = null) }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update { it.copy(description = value) }
    }
}
