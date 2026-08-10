package com.hima.ai.presentation.map

import androidx.compose.ui.geometry.Offset
import com.hima.ai.data.mock.MockData
import com.hima.ai.domain.model.IncidentCategory
import com.hima.ai.domain.model.MapIncident
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Clustering drives what the ranger can actually tap on the map, so it has to
 * hold two guarantees: no incident is ever dropped, and projected pins merge
 * only when their current on-screen positions overlap.
 */
class MapClusteringTest {

    private val incidents = MockData.allReports.take(3).mapIndexed { index, report ->
        MapIncident(
            report = report,
            category = IncidentCategory.OTHER,
            latitude = 24.0 + index,
            longitude = 45.0 + index,
        )
    }

    private val nearbyPositions = listOf(
        incidents[0] to Offset(20f, 20f),
        incidents[1] to Offset(60f, 20f),
        incidents[2] to Offset(240f, 180f),
    )

    private val separatedPositions = incidents.mapIndexed { index, incident ->
        incident to Offset(index * 120f, index * 100f)
    }

    private fun flattened(items: List<MapMarkerItem>) = items.flatMap { item ->
        when (item) {
            is MapMarkerItem.Single -> listOf(item.incident)
            is MapMarkerItem.Cluster -> item.incidents
        }
    }

    @Test
    fun `clustering never loses or duplicates an incident`() {
        listOf(nearbyPositions, separatedPositions).forEach { positions ->
            val rendered = flattened(clusterIncidents(positions))
            assertEquals(
                "an incident became unreachable for projected positions=$positions",
                incidents.toSet(),
                rendered.toSet(),
            )
            assertEquals("an incident was rendered twice", incidents.size, rendered.size)
        }
    }

    @Test
    fun `nearby projected incidents merge and separated incidents stay individual`() {
        val nearby = clusterIncidents(nearbyPositions)
        assertTrue(
            "the two deliberately adjacent markers should merge",
            nearby.any { it is MapMarkerItem.Cluster },
        )

        val separated = clusterIncidents(separatedPositions)
        assertTrue(
            "well-separated markers should not cluster",
            separated.none { it is MapMarkerItem.Cluster },
        )
    }

    @Test
    fun `a cluster sits at the centre of the incidents it represents`() {
        val cluster = clusterIncidents(nearbyPositions)
            .filterIsInstance<MapMarkerItem.Cluster>()
            .first()

        assertEquals(40f, cluster.screenPosition.x, 0.0001f)
        assertEquals(20f, cluster.screenPosition.y, 0.0001f)
    }

    @Test
    fun `an empty map produces no markers rather than throwing`() {
        assertEquals(emptyList<MapMarkerItem>(), clusterIncidents(emptyList()))
    }
}
