package com.hima.ai.data.repository

import com.hima.ai.data.remote.supabase.AiAnalysisDto
import com.hima.ai.data.remote.supabase.ReportDto
import com.hima.ai.domain.model.AnalysisResultCategory
import com.hima.ai.domain.model.CircularAction
import com.hima.ai.domain.model.IncidentCategory
import com.hima.ai.domain.model.ReportStatus
import com.hima.ai.domain.model.Severity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SupabaseReportMappingTest {

    @Test
    fun `waste report keeps its persisted id image coordinates and circular fields`() {
        val report = ReportDto(
            id = "report-42",
            type = "WASTE",
            severity = null,
            status = "OPEN",
            latitude = 24.7136,
            longitude = 46.6753,
            description = "Persisted description",
            imageUrl = "https://example.test/bottle.jpg",
            recommendedAction = "Recycle the bottle.",
            environmentalImpact = "Plastic can remain in the environment.",
            confidence = 95.0,
            createdAt = "2026-08-10T02:30:00Z",
            aiAnalysis = AiAnalysisDto(
                resultCategory = "recyclable_waste",
                wasteType = "Plastic bottle",
                materialCategory = "Plastic",
                recyclable = true,
                reusable = true,
                repairable = false,
                preferredAction = "recycle",
                recyclingGuidance = "Sort with plastics.",
            ),
        ).toReportSummary()

        assertEquals("report-42", report.id)
        assertEquals(IncidentCategory.WASTE, report.category)
        assertEquals("Plastic bottle", report.titleOverride)
        assertEquals("https://example.test/bottle.jpg", report.imageUrl)
        assertEquals(24.7136, report.latitude!!, 0.0)
        assertEquals(46.6753, report.longitude!!, 0.0)
        assertEquals("24.7136°N, 46.6753°E", report.locationOverride)
        assertEquals(ReportStatus.OPEN, report.status)
        assertEquals(Severity.UNKNOWN, report.severity)
        assertEquals(AnalysisResultCategory.RECYCLABLE_WASTE, report.analysis?.category)
        assertEquals(CircularAction.RECYCLE, report.analysis?.preferredAction)
        assertEquals("Plastic", report.analysis?.materialCategory)
        assertEquals(true, report.analysis?.reusable)
        assertEquals(95, report.confidence)
    }

    @Test
    fun `environmental report uses specific AI title and incident values`() {
        val report = ReportDto(
            id = "incident-7",
            type = "ILLEGAL_LOGGING",
            severity = "HIGH",
            status = "RESOLVED",
            latitude = 27.0,
            longitude = 41.0,
            confidence = 88.0,
            aiAnalysis = AiAnalysisDto(
                resultCategory = "environmental_incident",
                issueType = "Tree cutting",
                riskScore = 73.0,
                riskLevel = "HIGH",
            ),
        ).toReportSummary()

        assertEquals(IncidentCategory.LOGGING, report.category)
        assertEquals("Tree cutting", report.titleOverride)
        assertEquals(Severity.HIGH, report.severity)
        assertEquals(ReportStatus.RESOLVED, report.status)
        assertEquals(73, report.riskScore)
        assertEquals(AnalysisResultCategory.ENVIRONMENTAL_INCIDENT, report.analysis?.category)
    }

    @Test
    fun `missing coordinates remain missing and never receive a fake location`() {
        val report = ReportDto(
            id = "no-location",
            type = "OTHER",
            severity = "UNKNOWN",
            status = null,
        ).toReportSummary()

        assertNull(report.latitude)
        assertNull(report.longitude)
        assertNull(report.locationOverride)
        assertEquals(IncidentCategory.OTHER, report.category)
        assertEquals(ReportStatus.UNKNOWN, report.status)
    }

    @Test
    fun `all persisted backend categories use the centralized mapping`() {
        assertEquals(IncidentCategory.WASTE, IncidentCategory.fromBackendType("WASTE"))
        assertEquals(IncidentCategory.FIRE, IncidentCategory.fromBackendType("FIRE"))
        assertEquals(IncidentCategory.LOGGING, IncidentCategory.fromBackendType("ILLEGAL_LOGGING"))
        assertEquals(IncidentCategory.POACHING, IncidentCategory.fromBackendType("ILLEGAL_HUNTING"))
        assertEquals(IncidentCategory.POLLUTION, IncidentCategory.fromBackendType("WATER_POLLUTION"))
        assertEquals(IncidentCategory.WILDLIFE, IncidentCategory.fromBackendType("INJURED_ANIMAL"))
        assertEquals(IncidentCategory.PLANT_DISEASE, IncidentCategory.fromBackendType("PLANT_DISEASE"))
        assertEquals(IncidentCategory.OTHER, IncidentCategory.fromBackendType("NEW_AI_CATEGORY"))
    }
}
