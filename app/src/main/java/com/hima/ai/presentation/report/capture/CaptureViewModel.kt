package com.hima.ai.presentation.report.capture

import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.hima.ai.R
import com.hima.ai.core.navigation.HimaDestinations
import com.hima.ai.data.mock.CaptureSource
import com.hima.ai.data.mock.ReportDraft
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CaptureUiState(
    val source: CaptureSource = CaptureSource.CAMERA,
    val capturedUri: Uri? = null,
    val isCapturing: Boolean = false,
    val cameraPermissionGranted: Boolean = false,
    /** Denied at least once already, so offer Settings rather than re-prompting into a no-op. */
    val permissionPermanentlyDenied: Boolean = false,
    @StringRes val errorRes: Int? = null,
)

/**
 * Holds which capture path is running and the photo it produced.
 *
 * The photo is only written to [ReportDraft] on confirm, so backing out of the
 * review step leaves the report exactly as it was.
 */
@HiltViewModel
class CaptureViewModel @Inject constructor(
    private val draft: ReportDraft,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CaptureUiState(
            source = savedStateHandle.get<String>(HimaDestinations.CAPTURE_ARG_SOURCE)
                ?.let { runCatching { CaptureSource.valueOf(it) }.getOrNull() }
                ?: CaptureSource.CAMERA,
        ),
    )
    val uiState: StateFlow<CaptureUiState> = _uiState.asStateFlow()

    private var denials = 0

    fun onCameraPermissionResult(granted: Boolean) {
        if (!granted) denials++
        _uiState.update {
            it.copy(
                cameraPermissionGranted = granted,
                // Android stops showing the system dialog after the second
                // refusal, so only then is Settings the honest next step.
                permissionPermanentlyDenied = !granted && denials >= 2,
            )
        }
    }

    fun onCaptureRequested() {
        _uiState.update { it.copy(isCapturing = true, errorRes = null) }
    }

    fun onImageCaptured(uri: Uri) {
        _uiState.update { it.copy(capturedUri = uri, isCapturing = false) }
    }

    fun onImagePicked(uri: Uri) {
        _uiState.update { it.copy(capturedUri = uri, isCapturing = false) }
    }

    fun onCaptureFailed() {
        _uiState.update { it.copy(isCapturing = false, errorRes = R.string.capture_error) }
    }

    /** Back to the viewfinder/picker without touching the report. */
    fun onDiscard() {
        _uiState.update { it.copy(capturedUri = null, errorRes = null) }
    }

    fun onConfirm() {
        val uri = _uiState.value.capturedUri ?: return
        draft.setImage(uri, _uiState.value.source)
    }
}
