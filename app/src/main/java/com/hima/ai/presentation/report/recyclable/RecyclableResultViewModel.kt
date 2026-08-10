package com.hima.ai.presentation.report.recyclable

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.hima.ai.data.mock.ReportDraft
import com.hima.ai.domain.model.CircularAction
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class RecyclableResultUiState(
    val imageUri: Uri? = null,
    val description: String = "",
    val materialCategory: String = "",
    val wasteType: String = "",
    val disposalClassification: String = "",
    val recyclable: Boolean? = null,
    val reusable: Boolean? = null,
    val repairable: Boolean? = null,
    val preferredAction: CircularAction? = null,
    val environmentalImpact: String? = null,
    val aiExplanation: String? = null,
    val recommendation: String = "",
    val reuseSuggestion: String? = null,
    val repairGuidance: String? = null,
    val recyclingGuidance: String? = null,
    val disposalGuidance: String? = null,
    val confidence: Int = 0,
    val reportId: String? = null,
) {
    val isLowConfidence: Boolean get() = confidence < LOW_CONFIDENCE_THRESHOLD

    private companion object {
        const val LOW_CONFIDENCE_THRESHOLD = 60
    }
}

/**
 * Reads the completed circular-economy analysis from [ReportDraft]. The
 * backend-created report id is retained so the user can continue into the
 * unified environmental report flow without duplicating report state here.
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
            wasteType = analysis.wasteType.orEmpty(),
            disposalClassification = analysis.disposalClassification.orEmpty(),
            recyclable = analysis.recyclable,
            reusable = analysis.reusable,
            repairable = analysis.repairable,
            preferredAction = analysis.preferredAction,
            environmentalImpact = analysis.environmentalImpact?.takeIf { it.isNotBlank() },
            aiExplanation = analysis.aiExplanation?.takeIf { it.isNotBlank() },
            recommendation = analysis.recommendation,
            reuseSuggestion = analysis.reuseSuggestion?.takeIf { it.isNotBlank() },
            repairGuidance = analysis.repairGuidance?.takeIf { it.isNotBlank() },
            recyclingGuidance = analysis.recyclingGuidance?.takeIf { it.isNotBlank() },
            disposalGuidance = analysis.disposalGuidance?.takeIf { it.isNotBlank() },
            confidence = analysis.confidence,
            reportId = analysis.reportId,
        )
    } ?: RecyclableResultUiState()
}
