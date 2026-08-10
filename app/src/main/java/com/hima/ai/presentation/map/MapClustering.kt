package com.hima.ai.presentation.map

import androidx.compose.ui.geometry.Offset
import com.hima.ai.domain.model.MapIncident
import kotlin.math.sqrt
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.Projection

/** Markers whose projected screen positions land within this many pixels of
 *  each other merge into a cluster — roughly a marker's own footprint, so
 *  pins that would visually overlap merge instead of stacking illegibly. */
private const val CLUSTER_PIXEL_THRESHOLD = 70f

/** One thing to render on the map: either a lone incident or a merged group.
 *  [screenPosition] is this frame's projected on-screen offset (px), from
 *  the live MapLibre camera. */
sealed interface MapMarkerItem {
    val screenPosition: Offset

    data class Single(val incident: MapIncident, override val screenPosition: Offset) : MapMarkerItem
    data class Cluster(val incidents: List<MapIncident>, override val screenPosition: Offset) : MapMarkerItem
}

/**
 * Greedily groups incidents whose given on-screen positions land within
 * [CLUSTER_PIXEL_THRESHOLD] of each other. Pure and Projection-free by
 * design (see [MapClusteringTest][com.hima.ai.presentation.map.MapClusteringTest]) —
 * callers that need to go from real incidents to screen positions should use
 * [groupIncidents]/[MapMarkerGroup.project] instead of calling this directly
 * on every camera tick; see [MapMarkerGroup] for why.
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

/**
 * Which incidents are grouped together — membership only, no screen
 * position. Deciding membership is kept separate from projecting a
 * position on purpose: recomputing [clusterIncidents] on every single
 * camera-move tick (mid-pan or mid-zoom) meant two nearby markers' pixel
 * distance crossed [CLUSTER_PIXEL_THRESHOLD] back and forth many times a
 * second, so they visibly flickered between single pins and a merged
 * cluster — and because a cluster renders at its members' centroid rather
 * than any one incident's real spot, that same flicker also read as a
 * marker "jumping" toward whichever neighbour it had just merged with.
 * [groupIncidents] should only run on an idle camera or when the incident
 * set itself changes; [project] re-positions the (stable) result on every
 * other tick without touching membership.
 */
sealed interface MapMarkerGroup {
    data class Single(val incident: MapIncident) : MapMarkerGroup
    data class Cluster(val incidents: List<MapIncident>) : MapMarkerGroup
}

/** Projects [incidents] through the current camera and clusters them — call
 *  only on an idle camera or an incident-data change, never on every
 *  camera-move tick (see [MapMarkerGroup]). */
fun groupIncidents(incidents: List<MapIncident>, projection: Projection): List<MapMarkerGroup> {
    val positions = incidents.map { incident ->
        val point = projection.toScreenLocation(LatLng(incident.latitude, incident.longitude))
        incident to Offset(point.x, point.y)
    }
    return clusterIncidents(positions).map { item ->
        when (item) {
            is MapMarkerItem.Single -> MapMarkerGroup.Single(item.incident)
            is MapMarkerItem.Cluster -> MapMarkerGroup.Cluster(item.incidents)
        }
    }
}

/** Re-projects a group's existing members to this frame's screen position.
 *  Never changes membership — only [groupIncidents] does that. */
fun MapMarkerGroup.project(projection: Projection): MapMarkerItem = when (this) {
    is MapMarkerGroup.Single -> {
        val point = projection.toScreenLocation(LatLng(incident.latitude, incident.longitude))
        MapMarkerItem.Single(incident, Offset(point.x, point.y))
    }
    is MapMarkerGroup.Cluster -> {
        val points = incidents.map { projection.toScreenLocation(LatLng(it.latitude, it.longitude)) }
        val avgX = points.map { it.x }.average().toFloat()
        val avgY = points.map { it.y }.average().toFloat()
        MapMarkerItem.Cluster(incidents, Offset(avgX, avgY))
    }
}
