package com.hima.ai.presentation.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HomeStatusTest {

    @Test
    fun `percentage is unavailable until report data has loaded`() {
        assertNull(calculateResolvedReportsPercentage(totalReports = 0, resolvedReports = 0, hasReportData = false))
    }

    @Test
    fun `loaded empty state has zero resolved percentage`() {
        assertEquals(0, calculateResolvedReportsPercentage(totalReports = 0, resolvedReports = 0, hasReportData = true))
    }

    @Test
    fun `percentage is calculated from real resolved and total counts`() {
        assertEquals(25, calculateResolvedReportsPercentage(totalReports = 8, resolvedReports = 2, hasReportData = true))
    }

    @Test
    fun `percentage is clamped when backend counts are temporarily inconsistent`() {
        assertEquals(100, calculateResolvedReportsPercentage(totalReports = 2, resolvedReports = 3, hasReportData = true))
    }
}
