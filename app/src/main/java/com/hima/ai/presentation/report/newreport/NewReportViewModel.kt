package com.hima.ai.presentation.report.newreport

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hima.ai.data.mock.ManualLocation
import com.hima.ai.data.mock.PrototypeSession
import com.hima.ai.data.mock.ReportDraft
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class NewReportUiState(
    val imageUri: Uri? = null,
    val description: String = "",
    /** Non-null when the ranger has picked a location by hand for this report. */
    val manualLocation: ManualLocation? = null,
) {
    /** The analyse action stays disabled until there is something to analyse. */
    val canAnalyze: Boolean get() = imageUri != null
    val currentStep: Int get() = if (imageUri == null) 1 else 2
}

/**
 * New report state. The evidence photo is owned by [ReportDraft] — it is taken
 * on the capture screen and read back here — so this screen renders whatever
 * the ranger confirmed without needing to receive it through navigation.
 */
@HiltViewModel
class NewReportViewModel @Inject constructor(
    private val draft: ReportDraft,
    session: PrototypeSession,
) : ViewModel() {

    val uiState: StateFlow<NewReportUiState> =
        combine(draft.imageUri, draft.description, draft.manualLocation) { uri, description, manual ->
            NewReportUiState(imageUri = uri, description = description, manualLocation = manual)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NewReportUiState(),
        )

    init {
        // Each new report starts clean: base risk score, no carried-over photo.
        session.reset()
        draft.reset()
    }

    fun onClearImage() {
        draft.clearImage()
    }

    fun onDescriptionChange(value: String) {
        draft.setDescription(value)
    }

    /** Pass null to fall back to the device's real GPS fix. */
    fun onManualLocationChange(location: ManualLocation?) {
        draft.setManualLocation(location)
    }
}
