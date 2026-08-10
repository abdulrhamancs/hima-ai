package com.hima.ai.domain.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.hima.ai.R

/**
 * The incident categories shown on the reserve map. Each carries its own
 * marker glyph, a plural filter-pill label, and a singular label for use as
 * a report's title. Severity (already on [ReportSummary]) drives the
 * marker's colour, category drives its icon.
 */
enum class IncidentCategory(
    @DrawableRes val iconRes: Int,
    @StringRes val filterLabelRes: Int,
    @StringRes val singularLabelRes: Int,
) {
    FIRE(R.drawable.ic_category_fire, R.string.map_filter_fire, R.string.report_type_fire),
    LOGGING(R.drawable.ic_category_logging, R.string.map_filter_logging, R.string.report_type_logging),
    POACHING(R.drawable.ic_category_poaching, R.string.map_filter_poaching, R.string.report_type_poaching),
    POLLUTION(R.drawable.ic_category_pollution, R.string.map_filter_pollution, R.string.report_type_pollution),
    WILDLIFE(R.drawable.ic_category_wildlife, R.string.map_filter_wildlife, R.string.report_type_wildlife),
    PLANT_DISEASE(R.drawable.ic_category_plant_disease, R.string.map_filter_plant_disease, R.string.report_type_plant_disease),
    OTHER(R.drawable.ic_category_other, R.string.map_filter_other, R.string.report_type_other);

    companion object {
        /**
         * Maps the backend's `reports.type` column (e.g. "ILLEGAL_LOGGING") onto
         * a category. Two backend types (dead/injured animal) share one
         * WILDLIFE category — a marker icon distinguishing "dead" from
         * "injured" isn't worth the visual noise; the report text does that.
         */
        fun fromBackendType(type: String?): IncidentCategory = when (type?.uppercase()) {
            "FIRE" -> FIRE
            "ILLEGAL_LOGGING" -> LOGGING
            "ILLEGAL_HUNTING" -> POACHING
            "WASTE" -> POLLUTION
            "DEAD_ANIMAL", "INJURED_ANIMAL" -> WILDLIFE
            "PLANT_DISEASE" -> PLANT_DISEASE
            else -> OTHER
        }
    }
}

/**
 * One marker on the reserve map. Wraps the same [ReportSummary] used in lists
 * elsewhere, so a marker's bottom sheet and "View report" action stay in sync
 * with the rest of the app — only [category] and the real-world position are
 * map-specific. Real latitude/longitude (not a fraction of a fake canvas) is
 * what lets this plug straight into a Supabase-backed source without
 * changing how the map places or clusters markers.
 */
data class MapIncident(
    val report: ReportSummary,
    val category: IncidentCategory,
    val latitude: Double,
    val longitude: Double,
)
