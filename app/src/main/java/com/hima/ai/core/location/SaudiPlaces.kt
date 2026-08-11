package com.hima.ai.core.location

import com.hima.ai.R
import com.hima.ai.data.mock.ManualLocation

/**
 * Well-known places across Saudi Arabia, offered when a report's location is
 * set by hand.
 *
 * These are a fixed table rather than forward-geocoded lookups on purpose:
 * [android.location.Geocoder]'s name search needs a backend service that is
 * frequently absent on emulators and unreliable without network, and this list
 * exists precisely so a report can be placed anywhere in the country during a
 * demo. A fixed table always resolves, instantly and offline.
 *
 * Coordinates are city-centre approximations — enough to drop a marker in the
 * right place on a national-scale map, which is all this is for.
 */
object SaudiPlaces {

    val all: List<ManualLocation> = listOf(
        ManualLocation(24.7136, 46.6753, R.string.place_riyadh),
        ManualLocation(21.4858, 39.1925, R.string.place_jeddah),
        ManualLocation(21.3891, 39.8579, R.string.place_mecca),
        ManualLocation(24.5247, 39.5692, R.string.place_medina),
        ManualLocation(26.4207, 50.0888, R.string.place_dammam),
        ManualLocation(26.2172, 50.1971, R.string.place_khobar),
        ManualLocation(18.2164, 42.5053, R.string.place_abha),
        ManualLocation(16.8892, 42.5611, R.string.place_jazan),
        ManualLocation(17.4917, 44.1322, R.string.place_najran),
        ManualLocation(28.3998, 36.5715, R.string.place_tabuk),
        ManualLocation(27.5219, 41.6907, R.string.place_hail),
        ManualLocation(20.0129, 41.4677, R.string.place_albaha),
        ManualLocation(26.3260, 43.9750, R.string.place_buraidah),
        ManualLocation(24.0895, 38.0618, R.string.place_yanbu),
        ManualLocation(21.2703, 40.4158, R.string.place_taif),
        ManualLocation(29.0154, 40.0287, R.string.place_king_salman_reserve),
    )
}
