package com.hima.ai.presentation.map

import androidx.compose.ui.geometry.Offset
import com.hima.ai.domain.model.MapIncident
import kotlin.math.sqrt

/** Markers whose projected screen positions land within this many pixels of
 *  each other merge into a cluster — roughly a marker's own footprint, so
 *  pins that would visually overlap merge instead of stacking illegibly. */
private const val CLUSTER_PIXEL_THRESHOLD = 70f

/** One thing to render on the map: either a lone incident or a merged group.
 *  [screenPosition] is this frame's projected on-screen offset (px), from
 *  the live MapLibre camera — real geography drives clustering now, not a
 *  fraction of a fake canvas. */
sealed interface MapMarkerItem {
    val screenPosition: Offset

    data class Single(val incident: MapIncident, override val screenPosition: Offset) : MapMarkerItem
    data class Cluster(val incidents: List<MapIncident>, override val screenPosition: Offset) : MapMarkerItem
}

/**
 * Greedily groups incidents whose current on-screen projection lands within
 * [CLUSTER_PIXEL_THRESHOLD] of each other. Because the input is already in
 * real screen pixels (from [org.maplibre.android.maps.Projection]), clusters
 * pop apart naturally as the ranger zooms in — the map's own projection does
 * the zoom-scaling that used to be a manual factor in fraction-space.
 */
fun clusterIncidents(positions: List<Pair<MapIncident, Offset>>): List<MapMarkerItem> {
    val used = BooleanArray(positions.size)
    val result = mutableListOf<MapMarkerItem>()

    for (i in positions.indices) {
        if (used[i]) continue
        val group = mutableListOf(positions[i])
        used[i] = true
        for (j in i + 1 until positions.size) {
            if (used[j]) continue
            val dx = positions[i].second.x - positions[j].second.x
            val dy = positions[i].second.y - positions[j].second.y
            if (sqrt(dx * dx + dy * dy) <= CLUSTER_PIXEL_THRESHOLD) {
                group += positions[j]
                used[j] = true
            }
        }
        result += if (group.size == 1) {
            MapMarkerItem.Single(group[0].first, group[0].second)
        } else {
            val avgX = group.map { it.second.x }.average().toFloat()
            val avgY = group.map { it.second.y }.average().toFloat()
            MapMarkerItem.Cluster(group.map { it.first }, Offset(avgX, avgY))
        }
    }
    return result
}
