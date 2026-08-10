package com.hima.ai.presentation.map

import com.hima.ai.data.mock.MockData
import com.hima.ai.domain.model.FireHotspot
import com.hima.ai.domain.model.IncidentCategory
import com.hima.ai.domain.model.MapIncident
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The map's one filter row drives two genuinely separate data sources, so the
 * guarantee worth pinning down is that a selection never mixes them: picking a
 * report category must not leave satellite detections on screen, and picking
 * the NASA layer must not leave reports on screen.
 */
class MapFilterTest {

    private val waste = incident(IncidentCategory.WASTE, latitude = 24.0)
    private val fire = incident(IncidentCategory.FIRE, latitude = 25.0)
    private val pollution = incident(IncidentCategory.POLLUTION, latitude = 26.0)

    private val hotspot = FireHotspot(
        latitude = 24.5,
        longitude = 45.5,
        brightness = 330.0,
        confidence = "nominal",
        satellite = "N20",
        acquiredAt = null,
    )

    private val state = MapUiState(
        incidents = listOf(waste, fire, pollution),
        fireHotspots = listOf(hotspot),
    )

    private fun incident(category: IncidentCategory, latitude: Double) = MapIncident(
        report = MockData.allReports.first(),
        category = category,
        latitude = latitude,
        longitude = 45.0,
    )

    @Test
    fun `All shows every report alongside the satellite layer`() {
        val all = state.copy(filter = MapFilter.All)

        assertEquals(state.incidents, all.visibleIncidents)
        assertTrue("satellite detections belong on the unfiltered map", all.showNasaFires)
    }

    @Test
    fun `a report category shows only that category and hides the satellite layer`() {
        val wasteOnly = state.copy(filter = MapFilter.Category(IncidentCategory.WASTE))

        assertEquals(listOf(waste), wasteOnly.visibleIncidents)
        assertFalse(
            "asking for one report category should not leave NASA hotspots on screen",
            wasteOnly.showNasaFires,
        )
    }

    @Test
    fun `the NASA layer hides every user report`() {
        val nasaOnly = state.copy(filter = MapFilter.NasaFires)

        assertEquals(emptyList<MapIncident>(), nasaOnly.visibleIncidents)
        assertTrue(nasaOnly.showNasaFires)
    }

    @Test
    fun `every pill index round-trips back to the same selection`() {
        // The screen renders by rowIndex and the ViewModel decodes taps by
        // fromRowIndex; if those two ever disagree the row highlights one
        // pill while filtering by another.
        val lastIndex = IncidentCategory.entries.size + 1
        (0..lastIndex).forEach { index ->
            assertEquals(index, MapFilter.fromRowIndex(index).rowIndex)
        }
    }

    @Test
    fun `the pill row covers All, every category, and the NASA layer exactly once`() {
        val lastIndex = IncidentCategory.entries.size + 1
        val selections = (0..lastIndex).map { MapFilter.fromRowIndex(it) }

        assertEquals("no pill should resolve to the same filter twice", selections.size, selections.toSet().size)
        assertEquals(MapFilter.All, selections.first())
        assertEquals(MapFilter.NasaFires, selections.last())
        assertEquals(
            "every report category needs its own pill",
            IncidentCategory.entries.toSet(),
            selections.filterIsInstance<MapFilter.Category>().map { it.category }.toSet(),
        )
    }
}
