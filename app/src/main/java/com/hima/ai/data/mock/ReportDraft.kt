package com.hima.ai.data.mock

import android.net.Uri
import com.hima.ai.domain.model.AiAnalysis
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Where the evidence photo came from — the two capture paths stay distinct end to end. */
enum class CaptureSource { CAMERA, GALLERY }

/**
 * A location typed in by hand instead of read from GPS.
 *
 * [labelRes] is the place's own name so the UI can show *where* was chosen
 * rather than raw degrees. Setting one of these never touches the location
 * permission or the device's real fix — it only changes the coordinates
 * attached to the report currently being composed.
 */
data class ManualLocation(val latitude: Double, val longitude: Double, val labelRes: Int)

/**
 * The report being composed, from picking a photo through to the AI result.
 *
 * The photo is captured on one screen, confirmed on another, and rendered by
 * two more, so it needs one owner rather than being threaded through
 * navigation arguments as an encoded Uri. This is the object that becomes the
 * upload payload once a real backend exists.
 */
@Singleton
class ReportDraft @Inject constructor() {

    private val _imageUri = MutableStateFlow<Uri?>(null)
    val imageUri: StateFlow<Uri?> = _imageUri.asStateFlow()

    private val _source = MutableStateFlow<CaptureSource?>(null)
    val source: StateFlow<CaptureSource?> = _source.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _analysisResult = MutableStateFlow<AiAnalysis?>(null)
    val analysisResult: StateFlow<AiAnalysis?> = _analysisResult.asStateFlow()

    /**
     * An optional hand-picked location for this one report. Null means the
     * normal path — the device's real GPS fix, exactly as before.
     */
    private val _manualLocation = MutableStateFlow<ManualLocation?>(null)
    val manualLocation: StateFlow<ManualLocation?> = _manualLocation.asStateFlow()

    fun setImage(uri: Uri, source: CaptureSource) {
        _imageUri.value = uri
        _source.value = source
    }

    fun clearImage() {
        _imageUri.value = null
        _source.value = null
    }

    fun setDescription(value: String) {
        _description.value = value
    }

    fun setAnalysisResult(result: AiAnalysis) {
        _analysisResult.value = result
    }

    fun setManualLocation(location: ManualLocation?) {
        _manualLocation.value = location
    }

    /** Called when a fresh report starts, so nothing leaks in from the previous run. */
    fun reset() {
        clearImage()
        _description.value = ""
        _analysisResult.value = null
        // Deliberately cleared too: an override is scoped to the single report
        // it was chosen for, never carried into the next one.
        _manualLocation.value = null
    }
}
