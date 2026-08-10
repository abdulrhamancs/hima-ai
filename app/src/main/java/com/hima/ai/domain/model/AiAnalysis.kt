package com.hima.ai.domain.model

import androidx.annotation.StringRes
import com.hima.ai.R

/** Which shape a `/analyze` result takes: an environmental incident or a
 *  recyclable/reusable item following the circular-economy path.
 *  The backend decides this per image; nothing here is guessed
 *  or hardcoded on the Android side. */
enum class AnalysisResultCategory { ENVIRONMENTAL_INCIDENT, RECYCLABLE_WASTE }

/** The highest-value safe action supported by the image, ordered by circular priority. */
enum class CircularAction(@StringRes val labelRes: Int) {
    REUSE(R.string.circular_action_reuse),
    REPAIR_REFURBISH(R.string.circular_action_repair),
    DONATE_REPURPOSE(R.string.circular_action_donate),
    RECYCLE(R.string.circular_action_recycle),
    MATERIAL_RECOVERY(R.string.circular_action_material_recovery),
    SAFE_DISPOSAL(R.string.circular_action_safe_disposal),
}

/**
 * The backend's `/analyze` response, mapped 1:1 — nothing invented beyond
 * what that endpoint actually returns. There is no "suggested authority" in
 * this contract; screens that show one keep their existing placeholder until
 * the backend adds it.
 *
 * Only the fields matching [category] are used: an incident carries
 * [issueType]/[riskScore]/[riskLevel]; recyclable waste carries
 * [materialCategory]/[disposalClassification]/[reuseSuggestion] instead.
 * [description], [confidence], report metadata, and [recommendation] apply to both.
 */
data class AiAnalysis(
    val category: AnalysisResultCategory,
    val description: String,
    val confidence: Int,
    val recommendation: String,
    val reportId: String? = null,
    val imageUrl: String? = null,
    val environmentalImpact: String? = null,
    val aiExplanation: String? = null,
    // Environmental incident only:
    val issueType: String? = null,
    val riskScore: Int? = null,
    val riskLevel: Severity? = null,
    // Recyclable waste only:
    val materialCategory: String? = null,
    val wasteType: String? = null,
    val disposalClassification: String? = null,
    val recyclable: Boolean? = null,
    val reusable: Boolean? = null,
    val repairable: Boolean? = null,
    val preferredAction: CircularAction? = null,
    val reuseSuggestion: String? = null,
    val repairGuidance: String? = null,
    val recyclingGuidance: String? = null,
    val disposalGuidance: String? = null,
)
