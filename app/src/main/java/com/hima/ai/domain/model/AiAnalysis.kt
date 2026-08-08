package com.hima.ai.domain.model

/**
 * The backend's `/analyze` response, mapped 1:1 — nothing invented beyond
 * what that endpoint actually returns. There is no "suggested authority" in
 * this contract; screens that show one keep their existing placeholder until
 * the backend adds it.
 */
data class AiAnalysis(
    val issueType: String,
    val description: String,
    val riskScore: Int,
    val riskLevel: Severity,
    val confidence: Int,
    val recommendation: String,
)
