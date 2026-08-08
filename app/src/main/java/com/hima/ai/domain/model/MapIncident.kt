package com.hima.ai.domain.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.hima.ai.R

/**
 * The four incident categories shown on the reserve map. Each carries its own
 * marker glyph and filter label; severity (already on [ReportSummary]) drives
 * the marker's colour, category drives its icon.
 */
enum class IncidentCategory(@DrawableRes val iconRes: Int, @StringRes val filterLabelRes: Int) {
    FIRE(R.drawable.ic_category_fire, R.string.map_filter_fire),
    LOGGING(R.drawable.ic_category_logging, R.string.map_filter_logging),
    POACHING(R.drawable.ic_category_poaching, R.string.map_filter_poaching),
    POLLUTION(R.drawable.ic_category_pollution, R.string.map_filter_pollution),
}

/**
 * One marker on the reserve map. Wraps the same [ReportSummary] used in lists
 * elsewhere, so a marker's bottom sheet and "View report" action stay in sync
 * with the rest of the app — only [category] and the fractional position
 * (0..1 of the map canvas) are map-specific.
 */
data class MapIncident(
    val report: ReportSummary,
    val category: IncidentCategory,
    val xFraction: Float,
    val yFraction: Float,
)
