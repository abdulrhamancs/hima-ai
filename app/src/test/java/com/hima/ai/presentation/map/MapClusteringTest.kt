package com.hima.ai.presentation.map

import com.hima.ai.data.mock.MockData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Clustering drives what the ranger can actually tap on the map, so it has to
 * hold two guarantees: no incident is ever dropped, and zooming in separates
 * pins rather than leaving them merged.
 */
class MapClusteringTest {

    private val incidents = MockData.mapIncidents

    private fun flattened(items: List<MapMarkerItem>) = items.flatMap { item ->
        when (item) {
            is MapMarkerItem.Single -> listOf(item.incident)
            is MapMarkerItem.Cluster -> item.incidents
        }
    }

    @Test
    fun `clustering never loses or duplicates an incident`() {
        listOf(0.8f, 1f, 1.5f, 2f, 3f).forEach { scale ->
            val rendered = flattened(clusterIncidents(incidents, scale))
            assertEquals(
                "an incident became unreachable at scale=$scale",
                incidents.toSet(),
                rendered.toSet(),
            )
            assertEquals("an incident was rendered twice at scale=$scale", incidents.size, rendered.size)
        }
    }

    @Test
    fun `nearby incidents merge when zoomed out and separate when zoomed in`() {
        val atRest = clusterIncidents(incidents, 1f)
        assertTrue(
            "the two deliberately adjacent markers should merge at rest",
            atRest.any { it is MapMarkerItem.Cluster },
        )

        val zoomedIn = clusterIncidents(incidents, 3f)
        assertTrue(
            "zooming in should break clusters apart",
            zoomedIn.count { it is MapMarkerItem.Cluster } < atRest.count { it is MapMarkerItem.Cluster },
        )
    }

    @Test
    fun `a cluster sits at the centre of the incidents it represents`() {
        val cluster = clusterIncidents(incidents, 1f)
            .filterIsInstance<MapMarkerItem.Cluster>()
            .first()

        assertEquals(cluster.incidents.map { it.xFraction }.average().toFloat(), cluster.xFraction, 0.0001f)
        assertEquals(cluster.incidents.map { it.yFraction }.average().toFloat(), cluster.yFraction, 0.0001f)
    }

    @Test
    fun `an empty map produces no markers rather than throwing`() {
        assertEquals(emptyList<MapMarkerItem>(), clusterIncidents(emptyList(), 1f))
    }

    @Test
    fun `a degenerate scale does not divide by zero`() {
        // The screen can request scale 0 mid-gesture; clustering must survive it.
        val rendered = flattened(clusterIncidents(incidents, 0f))
        assertEquals(incidents.toSet(), rendered.toSet())
    }
}
