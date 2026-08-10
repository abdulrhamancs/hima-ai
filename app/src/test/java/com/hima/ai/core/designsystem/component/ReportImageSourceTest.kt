package com.hima.ai.core.designsystem.component

import org.junit.Assert.assertEquals
import org.junit.Test

class ReportImageSourceTest {

    @Test
    fun `persisted remote image always wins over a demo drawable`() {
        assertEquals(
            ReportImageSource.REMOTE,
            resolveReportImageSource(
                imageUrl = "https://example.test/user-upload.jpg",
                demoImageRes = 42,
            ),
        )
    }

    @Test
    fun `demo drawable is used only when no persisted image exists`() {
        assertEquals(
            ReportImageSource.DEMO,
            resolveReportImageSource(imageUrl = null, demoImageRes = 42),
        )
    }

    @Test
    fun `missing real and demo images use the existing placeholder`() {
        assertEquals(
            ReportImageSource.PLACEHOLDER,
            resolveReportImageSource(imageUrl = "  ", demoImageRes = null),
        )
    }
}
