package com.hima.ai.presentation.report.recyclable

import com.hima.ai.data.mock.ReportDraft
import com.hima.ai.domain.model.AiAnalysis
import com.hima.ai.domain.model.AnalysisResultCategory
import com.hima.ai.domain.model.CircularAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecyclableResultViewModelTest {

    @Test
    fun `maps the backend circular economy decision without inventing fields`() {
        val draft = ReportDraft().apply {
            setAnalysisResult(
                AiAnalysis(
                    category = AnalysisResultCategory.RECYCLABLE_WASTE,
                    description = "An old mobile phone",
                    confidence = 94,
                    recommendation = "Use a specialist e-waste collection route.",
                    reportId = "report-42",
                    environmentalImpact = "Electronics can contain recoverable and hazardous materials.",
                    aiExplanation = "The visible screen and casing indicate an electronic device.",
                    materialCategory = "Electronic waste",
                    wasteType = "Old mobile phone",
                    disposalClassification = "Specialized recovery",
                    recyclable = true,
                    reusable = false,
                    repairable = true,
                    preferredAction = CircularAction.REPAIR_REFURBISH,
                    repairGuidance = "Have the battery and device assessed by a qualified repair service.",
                    disposalGuidance = "Do not place it in ordinary household waste.",
                ),
            )
        }

        val state = RecyclableResultViewModel(draft).uiState

        assertEquals("Old mobile phone", state.wasteType)
        assertEquals("Electronic waste", state.materialCategory)
        assertEquals(CircularAction.REPAIR_REFURBISH, state.preferredAction)
        assertEquals(true, state.repairable)
        assertEquals("report-42", state.reportId)
        assertFalse(state.isLowConfidence)
    }

    @Test
    fun `low confidence and absent optional claims remain explicit`() {
        val draft = ReportDraft().apply {
            setAnalysisResult(
                AiAnalysis(
                    category = AnalysisResultCategory.RECYCLABLE_WASTE,
                    description = "Mixed material item",
                    confidence = 42,
                    recommendation = "Inspect the material markings before sorting.",
                ),
            )
        }

        val state = RecyclableResultViewModel(draft).uiState

        assertTrue(state.isLowConfidence)
        assertNull(state.recyclable)
        assertNull(state.reusable)
        assertNull(state.repairable)
        assertNull(state.preferredAction)
    }
}
