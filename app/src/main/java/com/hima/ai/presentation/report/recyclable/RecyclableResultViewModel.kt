package com.hima.ai.presentation.report.recyclable

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.hima.ai.data.mock.ReportDraft
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class RecyclableResultUiState(
    val imageUri: Uri? = null,
    val description: String = "",
    val materialCategory: String = "",
    val disposalClassification: String = "",
    val recommendation: String = "",
    val reuseSuggestion: String? = null,
    val confidence: Int = 0,
)

/**
 * A recyclable/reusable item never becomes a report — there's nothing to
 * save, escalate, or look up by id — so unlike [com.hima.ai.presentation.report.detail.ReportDetailViewModel]
 * this just reads [ReportDraft] once into a plain, immutable state.
 */
@HiltViewModel
class RecyclableResultViewModel @Inject constructor(
    draft: ReportDraft,
) : ViewModel() {

    val uiState: RecyclableResultUiState = draft.analysisResult.value?.let { analysis ->
        RecyclableResultUiState(
            imageUri = draft.imageUri.value,
            description = analysis.description,
            materialCategory = analysis.materialCategory.orEmpty(),
            disposalClassification = analysis.disposalClassification.orEmpty(),
            recommendation = analysis.recommendation,
            reuseSuggestion = analysis.reuseSuggestion?.takeIf { it.isNotBlank() },
            confidence = analysis.confidence,
        )
    } ?: RecyclableResultUiState()
}
