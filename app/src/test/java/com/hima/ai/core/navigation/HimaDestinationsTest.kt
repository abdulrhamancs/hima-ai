package com.hima.ai.core.navigation

import com.hima.ai.data.mock.MockData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The report route carries an optional id. Getting this wrong is invisible at
 * compile time and shows up as every report opening the same content, so the
 * built routes are pinned here.
 */
class HimaDestinationsTest {

    @Test
    fun `a report with no id uses the bare route so the optional argument matches`() {
        assertEquals("report", HimaDestinations.report())
        assertEquals("report", HimaDestinations.report(null))
        assertEquals("report", HimaDestinations.report(""))
        assertTrue(HimaDestinations.REPORT_ROUTE.startsWith("report"))
    }

    @Test
    fun `a report with an id carries it as the declared query argument`() {
        assertEquals("report?reportId=h1", HimaDestinations.report("h1"))
        assertEquals(
            "the built route must use the same argument name the graph declares",
            "report?${HimaDestinations.REPORT_ARG_ID}={${HimaDestinations.REPORT_ARG_ID}}",
            HimaDestinations.REPORT_ROUTE,
        )
    }

    @Test
    fun `every real report id produces a distinct route`() {
        val routes = MockData.allReports.map { HimaDestinations.report(it.id) }
        assertEquals("two reports would open the same screen", routes.size, routes.toSet().size)
        assertTrue(routes.none { it == HimaDestinations.report() })
    }
}
