package com.hima.ai.core.map

import com.hima.ai.BuildConfig
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng

/**
 * MapTiler is the map's only style/tile provider — the one place that knows
 * its URL shape and key, so switching styles or providers later touches this
 * file instead of the map screen. See local.properties for MAPTILER_API_KEY;
 * get a free key at https://cloud.maptiler.com/account/keys/.
 */
object MapConfig {

    /** "outdoor" reads calmer than "streets" — natural, muted tones that fit
     *  a nature-reserve app and stay out of the way of report markers. */
    private const val STYLE_ID = "outdoor-v2"

    val isConfigured: Boolean
        get() = BuildConfig.MAPTILER_API_KEY.isNotBlank()

    val styleUrl: String
        get() = "https://api.maptiler.com/maps/$STYLE_ID/style.json?key=${BuildConfig.MAPTILER_API_KEY}"

    /** Geographic centre of Saudi Arabia. */
    val saudiArabiaCenter: LatLng = LatLng(23.8859, 45.0792)

    /** A zoom that shows the whole Kingdom with sensible margin — the Map
     *  screen's opening viewport. */
    private const val SAUDI_ARABIA_DEFAULT_ZOOM = 4.8

    val saudiArabiaDefaultCamera: CameraPosition = CameraPosition.Builder()
        .target(saudiArabiaCenter)
        .zoom(SAUDI_ARABIA_DEFAULT_ZOOM)
        .build()
}
