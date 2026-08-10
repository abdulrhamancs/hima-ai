package com.hima.ai.presentation.history

import com.hima.ai.R
import com.hima.ai.domain.model.IncidentCategory
import com.hima.ai.domain.model.ReportStatus
import com.hima.ai.domain.model.ReportSummary
import com.hima.ai.domain.model.SceneKind
import com.hima.ai.domain.model.Severity
import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryUiStateTest {

    @Test
    fun `status and category filters compose without replacing real reports`() {
        val reports = listOf(
            report("waste", IncidentCategory.WASTE, ReportStatus.OPEN),
            report("fire", IncidentCategory.FIRE, ReportStatus.RESOLVED),
            report("pollution", IncidentCategory.POLLUTION, ReportStatus.OPEN),
        )

        val visible = HistoryUiState(
            filter = HistoryFilter.OPEN,
            categoryFilter = IncidentCategory.POLLUTION,
            allReports = reports,
        ).visibleReports

        assertEquals(listOf("pollution"), visible.map { it.id })
    }

    @Test
    fun `severity filters do not assign incident severity to waste reports`() {
        val reports = listOf(
            report("waste", IncidentCategory.WASTE, ReportStatus.OPEN, Severity.CRITICAL),
            report("fire", IncidentCategory.FIRE, ReportStatus.OPEN, Severity.CRITICAL),
            report("pollution", IncidentCategory.POLLUTION, ReportStatus.OPEN, Severity.LOW),
        )

        val visible = HistoryUiState(
            severityFilter = Severity.CRITICAL,
            allReports = reports,
        ).visibleReports

        assertEquals(listOf("fire"), visible.map { it.id })
    }

    private fun report(
        id: String,
        category: IncidentCategory,
        status: ReportStatus,
        severity: Severity = Severity.MEDIUM,
    ) = ReportSummary(
        id = id,
        titleRes = R.string.report_type_other,
        category = category,
        locationRes = R.string.report_location_unavailable,
        timeRes = R.string.time_unknown,
        severity = severity,
        status = status,
        scene = SceneKind.VALLEY,
    )
}
