package com.hima.ai.data.mock

import android.net.Uri
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Where the evidence photo came from — the two capture paths stay distinct end to end. */
enum class CaptureSource { CAMERA, GALLERY }

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

    /** Called when a fresh report starts, so nothing leaks in from the previous run. */
    fun reset() {
        clearImage()
        _description.value = ""
    }
}
