package com.hima.ai.presentation.map

import com.hima.ai.domain.model.MapIncident
import kotlin.math.sqrt

/** A base "at rest" clustering radius in fraction-space; shrinks as the map zooms in. */
private const val CLUSTER_BASE_THRESHOLD = 0.10f

/** One thing to render on the map: either a lone incident or a merged group. */
sealed interface MapMarkerItem {
    data class Single(val incident: MapIncident) : MapMarkerItem
    data class Cluster(
        val incidents: List<MapIncident>,
        val xFraction: Float,
        val yFraction: Float,
    ) : MapMarkerItem
}

/**
 * Greedily groups incidents that are within [CLUSTER_BASE_THRESHOLD] / [scale] of
 * each other in fraction-space, so pins merge when zoomed out and separate as
 * the ranger zooms in — the threshold shrinking with scale is what makes
 * clusters "pop apart" on zoom rather than staying fixed on screen.
 */
fun clusterIncidents(incidents: List<MapIncident>, scale: Float): List<MapMarkerItem> {
    val threshold = CLUSTER_BASE_THRESHOLD / scale.coerceAtLeast(0.1f)
    val used = BooleanArray(incidents.size)
    val result = mutableListOf<MapMarkerItem>()

    for (i in incidents.indices) {
        if (used[i]) continue
        val group = mutableListOf(incidents[i])
        used[i] = true
        for (j in i + 1 until incidents.size) {
            if (used[j]) continue
            val dx = incidents[i].xFraction - incidents[j].xFraction
            val dy = incidents[i].yFraction - incidents[j].yFraction
            if (sqrt(dx * dx + dy * dy) <= threshold) {
                group += incidents[j]
                used[j] = true
            }
        }
        result += if (group.size == 1) {
            MapMarkerItem.Single(group[0])
        } else {
            MapMarkerItem.Cluster(
                incidents = group,
                xFraction = group.map { it.xFraction }.average().toFloat(),
                yFraction = group.map { it.yFraction }.average().toFloat(),
            )
        }
    }
    return result
}
