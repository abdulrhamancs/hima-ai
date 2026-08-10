package com.hima.ai.core.map

import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap

/** Camera/viewport behaviour kept separate from both the raw view ([HimaMapView])
 *  and the feature's marker/filter state ([com.hima.ai.presentation.map.MapViewModel]). */

private const val RECENTER_DURATION_MS = 900

/** A close, "where am I standing" zoom for the My Location recentre. */
private const val USER_LOCATION_ZOOM = 13.0

/** Glides back to the opening Saudi Arabia viewport — used both as the map's
 *  reset action and as the "My Location" fallback when no fix is available. */
fun MapLibreMap.recenterToDefault() {
    animateCamera(CameraUpdateFactory.newCameraPosition(MapConfig.saudiArabiaDefaultCamera), RECENTER_DURATION_MS)
}

/** Glides to a real GPS fix at a close, local-area zoom — a deliberate,
 *  one-time recentre on tap, never a continuous follow. */
fun MapLibreMap.recenterToLocation(location: LatLng) {
    val targetZoom = maxOf(cameraPosition.zoom, USER_LOCATION_ZOOM)
    animateCamera(CameraUpdateFactory.newLatLngZoom(location, targetZoom), RECENTER_DURATION_MS)
}
