package com.hima.ai.data.mock

import com.hima.ai.R
import com.hima.ai.domain.model.IncidentCategory
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
            category = IncidentCategory.LOGGING,
            locationRes = R.string.loc_tuwaiq,
            timeRes = R.string.time_10min,
            severity = Severity.HIGH,
            status = ReportStatus.OPEN,
            scene = SceneKind.STUMP,
            demoImageRes = R.drawable.report_tree_cutting,
        ),
        ReportSummary(
            id = "h2",
            titleRes = R.string.incident_firewood,
            category = IncidentCategory.LOGGING,
            locationRes = R.string.loc_reem,
            timeRes = R.string.time_1hour,
            severity = Severity.MEDIUM,
            status = ReportStatus.OPEN,
            scene = SceneKind.FOREST,
            demoImageRes = R.drawable.report_firewood,
        ),
        ReportSummary(
            id = "h3",
            titleRes = R.string.incident_fire,
            category = IncidentCategory.FIRE,
            locationRes = R.string.loc_tuwaiq_short,
            timeRes = R.string.time_3hours,
            severity = Severity.CRITICAL,
            status = ReportStatus.OPEN,
            scene = SceneKind.FIRE,
            demoImageRes = R.drawable.report_wildfire,
        ),
        ReportSummary(
            id = "h4",
            titleRes = R.string.incident_water,
            category = IncidentCategory.POLLUTION,
            locationRes = R.string.loc_takhyeel,
            timeRes = R.string.time_may6_noon,
            severity = Severity.LOW,
            status = ReportStatus.RESOLVED,
            scene = SceneKind.WATER,
            demoImageRes = R.drawable.report_water_pollution,
        ),
        ReportSummary(
            id = "h5",
            titleRes = R.string.incident_waste,
            category = IncidentCategory.WASTE,
            locationRes = R.string.loc_north,
            timeRes = R.string.time_may6_evening,
            severity = Severity.MEDIUM,
            status = ReportStatus.RESOLVED,
            scene = SceneKind.WASTE,
            demoImageRes = R.drawable.report_illegal_waste,
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

    /**
     * Resolves a report id coming from a list row. Real map markers instead
     * resolve through [com.hima.ai.domain.repository.ReportsRepository.findById] —
     * see [com.hima.ai.presentation.report.detail.ReportDetailViewModel]. Returns
     * null for an unknown id — callers fall back to the freshly analysed
     * report rather than crashing.
     */
    fun findReport(id: String): ReportSummary? =
        recentReports.firstOrNull { it.id == id }
            ?: allReports.firstOrNull { it.id == id }
}
