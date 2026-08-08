package com.hima.ai.data.mock

import com.hima.ai.R
import com.hima.ai.domain.model.IncidentCategory
import com.hima.ai.domain.model.MapIncident
import com.hima.ai.domain.model.ReportStatus
import com.hima.ai.domain.model.ReportSummary
import com.hima.ai.domain.model.SceneKind
import com.hima.ai.domain.model.Severity

/**
 * Static prototype data. This is the single place fake content lives, so
 * swapping in Firestore later means replacing this object with a repository —
 * screens and ViewModels already consume the same domain models.
 */
object MockData {

    /** The analysed case that drives New report -> Analysis -> Report -> Investigation. */
    const val RISK_SCORE = "8.7 / 10"
    const val RISK_SCORE_ESCALATED = "9.1 / 10"
    const val CONFIDENCE_PERCENT = 92

    /**
     * The reserve's reports, newest first. This is the single canonical list:
     * Home shows the newest few and History shows all of them, so the same
     * incident carries the same id wherever it appears and opening it from
     * either screen lands on the same report.
     */
    val allReports = listOf(
        ReportSummary(
            id = "h1",
            titleRes = R.string.incident_logging,
            locationRes = R.string.loc_tuwaiq,
            timeRes = R.string.time_10min,
            severity = Severity.HIGH,
            status = ReportStatus.OPEN,
            scene = SceneKind.STUMP,
        ),
        ReportSummary(
            id = "h2",
            titleRes = R.string.incident_firewood,
            locationRes = R.string.loc_reem,
            timeRes = R.string.time_1hour,
            severity = Severity.MEDIUM,
            status = ReportStatus.OPEN,
            scene = SceneKind.FOREST,
        ),
        ReportSummary(
            id = "h3",
            titleRes = R.string.incident_fire,
            locationRes = R.string.loc_tuwaiq_short,
            timeRes = R.string.time_3hours,
            severity = Severity.CRITICAL,
            status = ReportStatus.OPEN,
            scene = SceneKind.FIRE,
        ),
        ReportSummary(
            id = "h4",
            titleRes = R.string.incident_water,
            locationRes = R.string.loc_takhyeel,
            timeRes = R.string.time_may6_noon,
            severity = Severity.LOW,
            status = ReportStatus.RESOLVED,
            scene = SceneKind.WATER,
        ),
        ReportSummary(
            id = "h5",
            titleRes = R.string.incident_waste,
            locationRes = R.string.loc_north,
            timeRes = R.string.time_may6_evening,
            severity = Severity.MEDIUM,
            status = ReportStatus.RESOLVED,
            scene = SceneKind.VALLEY,
        ),
    )

    /** What Home lists under "latest reports" — the newest slice of [allReports]. */
    val recentReports = allReports.take(3)

    /**
     * Home's counters, derived from [allReports] rather than standalone
     * constants — hardcoded totals drifted from the list they claimed to
     * summarise, and a real backend would compute these the same way.
     */
    val totalReports: Int get() = allReports.size
    val openReports: Int get() = allReports.count { it.status == ReportStatus.OPEN }
    val resolvedReports: Int get() = allReports.count { it.status == ReportStatus.RESOLVED }
    val criticalAlerts: Int get() = allReports.count { it.severity == Severity.CRITICAL }

    /** Markers for the reserve map. Positions are fractions (0..1) of the map canvas. */
    val mapIncidents = listOf(
        MapIncident(
            report = ReportSummary(
                id = "m1",
                titleRes = R.string.incident_fire,
                locationRes = R.string.loc_tuwaiq_short,
                timeRes = R.string.time_3hours,
                severity = Severity.CRITICAL,
                status = ReportStatus.OPEN,
                scene = SceneKind.FIRE,
            ),
            category = IncidentCategory.FIRE,
            xFraction = 0.62f,
            yFraction = 0.30f,
        ),
        MapIncident(
            report = ReportSummary(
                id = "m2",
                titleRes = R.string.incident_logging,
                locationRes = R.string.loc_tuwaiq,
                timeRes = R.string.time_10min,
                severity = Severity.HIGH,
                status = ReportStatus.OPEN,
                scene = SceneKind.STUMP,
            ),
            category = IncidentCategory.LOGGING,
            xFraction = 0.34f,
            yFraction = 0.42f,
        ),
        MapIncident(
            report = ReportSummary(
                id = "m3",
                titleRes = R.string.incident_firewood,
                locationRes = R.string.loc_reem,
                timeRes = R.string.time_1hour,
                severity = Severity.MEDIUM,
                status = ReportStatus.OPEN,
                scene = SceneKind.FOREST,
            ),
            category = IncidentCategory.LOGGING,
            xFraction = 0.71f,
            yFraction = 0.58f,
        ),
        MapIncident(
            report = ReportSummary(
                id = "m4",
                titleRes = R.string.incident_poaching,
                locationRes = R.string.loc_north,
                timeRes = R.string.time_may9,
                severity = Severity.HIGH,
                status = ReportStatus.OPEN,
                scene = SceneKind.VALLEY,
            ),
            category = IncidentCategory.POACHING,
            xFraction = 0.22f,
            yFraction = 0.68f,
        ),
        MapIncident(
            report = ReportSummary(
                id = "m5",
                titleRes = R.string.incident_water,
                locationRes = R.string.loc_takhyeel,
                timeRes = R.string.time_may6_noon,
                severity = Severity.LOW,
                status = ReportStatus.RESOLVED,
                scene = SceneKind.WATER,
            ),
            category = IncidentCategory.POLLUTION,
            xFraction = 0.50f,
            yFraction = 0.76f,
        ),
        MapIncident(
            report = ReportSummary(
                id = "m6",
                titleRes = R.string.incident_poaching,
                locationRes = R.string.loc_reem,
                timeRes = R.string.time_may9,
                severity = Severity.MEDIUM,
                status = ReportStatus.RESOLVED,
                scene = SceneKind.VALLEY,
            ),
            category = IncidentCategory.POACHING,
            xFraction = 0.80f,
            yFraction = 0.24f,
        ),
        // Deliberately close to m6, so the two merge into a cluster at rest
        // and split apart on zoom — a visible demo of the clustering behaviour.
        MapIncident(
            report = ReportSummary(
                id = "m7",
                titleRes = R.string.incident_firewood,
                locationRes = R.string.loc_reem,
                timeRes = R.string.time_may9,
                severity = Severity.HIGH,
                status = ReportStatus.OPEN,
                scene = SceneKind.FOREST,
            ),
            category = IncidentCategory.LOGGING,
            xFraction = 0.76f,
            yFraction = 0.20f,
        ),
    )

    /**
     * Resolves a report id coming from a list row or map marker. Ids are unique
     * across the three sets, so a single lookup covers Home, History, and the
     * map. Returns null for an unknown id — callers fall back to the freshly
     * analysed report rather than crashing.
     */
    fun findReport(id: String): ReportSummary? =
        recentReports.firstOrNull { it.id == id }
            ?: allReports.firstOrNull { it.id == id }
            ?: mapIncidents.firstOrNull { it.report.id == id }?.report
}
