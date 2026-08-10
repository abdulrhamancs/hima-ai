package com.hima.ai.data.mock

import com.hima.ai.domain.model.ReportStatus
import com.hima.ai.domain.model.Severity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the prototype's data against the inconsistencies that made report
 * rows resolve to the wrong record: Home and History must describe the same
 * reports, ids must be unique, and every mock id a list screen can hand to
 * navigation must resolve back to a report. Map incidents are now real
 * Supabase data and are covered at the repository/clustering boundary.
 */
class MockDataConsistencyTest {

    @Test
    fun `report ids are unique across every source`() {
        val ids = MockData.allReports.map { it.id }
        assertEquals("duplicate report ids would make lookups ambiguous", ids.size, ids.toSet().size)
    }

    @Test
    fun `home's recent reports are the newest slice of the canonical list`() {
        assertTrue(
            "Home must not hold its own copies of reports History does not know about",
            MockData.allReports.containsAll(MockData.recentReports),
        )
        assertEquals(MockData.allReports.take(3), MockData.recentReports)
    }

    @Test
    fun `every id reachable from a list row or marker resolves`() {
        val reachable = MockData.recentReports.map { it.id } +
            MockData.allReports.map { it.id }

        reachable.forEach { id ->
            assertNotNull("tapping the row for id=$id must open a report", MockData.findReport(id))
        }
    }

    @Test
    fun `findReport returns the matching report, not merely any report`() {
        MockData.allReports.forEach { report ->
            assertEquals(report, MockData.findReport(report.id))
        }
    }

    @Test
    fun `findReport returns null for an unknown id rather than throwing`() {
        assertNull(MockData.findReport("does-not-exist"))
        assertNull(MockData.findReport(""))
    }

    @Test
    fun `home counters agree with the list they summarise`() {
        assertEquals(MockData.allReports.size, MockData.totalReports)
        assertEquals(
            MockData.allReports.count { it.status == ReportStatus.OPEN },
            MockData.openReports,
        )
        assertEquals(
            MockData.allReports.count { it.status == ReportStatus.RESOLVED },
            MockData.resolvedReports,
        )
        assertEquals(
            MockData.allReports.count { it.severity == Severity.CRITICAL },
            MockData.criticalAlerts,
        )
        assertEquals(MockData.totalReports, MockData.openReports + MockData.resolvedReports)
    }

}
