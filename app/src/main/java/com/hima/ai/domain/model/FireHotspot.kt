package com.hima.ai.domain.model

import java.time.Instant

/**
 * One NASA FIRMS satellite thermal-hotspot detection — never a user report.
 * Kept fully separate from [MapIncident]/[ReportSummary] (no shared id space)
 * so the two data sources can never be confused with each other on the map.
 */
data class FireHotspot(
    val latitude: Double,
    val longitude: Double,
    val brightness: Double?,
    val confidence: String?,
    val satellite: String?,
    val acquiredAt: Instant?,
)
