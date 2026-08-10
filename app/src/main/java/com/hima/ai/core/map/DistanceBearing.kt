package com.hima.ai.core.map

import android.location.Location
import java.util.Locale
import kotlin.math.roundToInt
import org.maplibre.android.geometry.LatLng

private val COMPASS_POINTS = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")

/** Great-circle distance and initial compass bearing between two points —
 *  local computation only, no backend call (see spec: "do not create a new
 *  backend endpoint for this"). */
data class DistanceBearing(val meters: Float, val bearingDegrees: Float) {
    val compassPoint: String
        get() {
            val normalized = ((bearingDegrees % 360) + 360) % 360
            val index = (normalized / 45.0).roundToInt() % COMPASS_POINTS.size
            return COMPASS_POINTS[index]
        }

    /** e.g. "850 m · NE" or "2.3 km · SW". */
    fun formatLabel(): String {
        val distanceText = if (meters < 1000f) {
            "${meters.roundToInt()} m"
        } else {
            String.format(Locale.US, "%.1f km", meters / 1000f)
        }
        return "$distanceText · $compassPoint"
    }
}

fun distanceBearing(from: LatLng, to: LatLng): DistanceBearing {
    val results = FloatArray(2)
    Location.distanceBetween(from.latitude, from.longitude, to.latitude, to.longitude, results)
    return DistanceBearing(meters = results[0], bearingDegrees = results[1])
}
