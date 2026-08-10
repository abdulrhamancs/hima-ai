package com.hima.ai.domain.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import java.time.Instant

/** Whether a report is still being worked or has been closed out. */
enum class ReportStatus { UNKNOWN, OPEN, RESOLVED }

/** The generated scene used whenever a persisted or explicit demo image is unavailable. */
enum class SceneKind { STUMP, FOREST, FIRE, WATER, WASTE, VALLEY }

/**
 * One report row. String fields are [StringRes] ids rather than literals so the
 * whole list re-renders correctly when the user switches language — mock data
 * only ever sets those. A real Supabase report instead carries free text the
 * backend/AI produced, which resource ids can't represent; the `*Override`
 * fields hold that text and win over the resource id wherever both exist,
 * mirroring the pattern [com.hima.ai.presentation.report.detail.ReportDetailUiState]
 * already uses for a freshly analysed report.
 */
data class ReportSummary(
    val id: String,
    @StringRes val titleRes: Int,
    val titleOverride: String? = null,
    val category: IncidentCategory = IncidentCategory.OTHER,
    @StringRes val locationRes: Int,
    /** Formatted coordinates for a real report — no place-name geocoding here. */
    val locationOverride: String? = null,
    @StringRes val timeRes: Int,
    /** When set, the real report time — rendered via [com.hima.ai.core.common.relativeTimeLabel] instead of [timeRes]. */
    val createdAt: Instant? = null,
    val severity: Severity,
    val status: ReportStatus,
    val scene: SceneKind,
    val imageUrl: String? = null,
    /** Local representative photo for static demo data only; persisted reports leave this null. */
    @DrawableRes val demoImageRes: Int? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val reasonOverride: String? = null,
    val recommendationOverride: String? = null,
    val environmentalImpactOverride: String? = null,
    val confidence: Int? = null,
    val analysis: AiAnalysis? = null,
    /** 0..100, from the AI analysis — null for mock data, which keeps its fixed placeholder score. */
    val riskScore: Int? = null,
)
