package com.hima.ai.domain.model

/** Which shape a `/analyze` result takes — an environmental incident (needs a
 *  field report) or an everyday recyclable/reusable item (circular economy,
 *  no report). The backend decides this per image; nothing here is guessed
 *  or hardcoded on the Android side. */
enum class AnalysisResultCategory { ENVIRONMENTAL_INCIDENT, RECYCLABLE_WASTE }

/**
 * The backend's `/analyze` response, mapped 1:1 — nothing invented beyond
 * what that endpoint actually returns. There is no "suggested authority" in
 * this contract; screens that show one keep their existing placeholder until
 * the backend adds it.
 *
 * Only the fields matching [category] are populated: an incident carries
 * [issueType]/[riskScore]/[riskLevel]; recyclable waste carries
 * [materialCategory]/[disposalClassification]/[reuseSuggestion] instead.
 * [description], [confidence], and [recommendation] apply to both.
 */
data class AiAnalysis(
    val category: AnalysisResultCategory,
    val description: String,
    val confidence: Int,
    val recommendation: String,
    // Environmental incident only:
    val issueType: String? = null,
    val riskScore: Int? = null,
    val riskLevel: Severity? = null,
    // Recyclable waste only:
    val materialCategory: String? = null,
    val disposalClassification: String? = null,
    val reuseSuggestion: String? = null,
)
