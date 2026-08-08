package com.hima.ai.domain.model

import androidx.annotation.StringRes

/** Whether a report is still being worked or has been closed out. */
enum class ReportStatus { OPEN, RESOLVED }

/**
 * The illustration drawn as a report's thumbnail. The prototype has no photo
 * assets, so each report kind maps to generated scene art (see `SceneThumbnail`).
 */
enum class SceneKind { STUMP, FOREST, FIRE, WATER, VALLEY }

/**
 * One report row. String fields are [StringRes] ids rather than literals so the
 * whole list re-renders correctly when the user switches language.
 */
data class ReportSummary(
    val id: String,
    @StringRes val titleRes: Int,
    @StringRes val locationRes: Int,
    @StringRes val timeRes: Int,
    val severity: Severity,
    val status: ReportStatus,
    val scene: SceneKind,
)
