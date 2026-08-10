package com.hima.ai.core.map

/** What the map surface is currently showing, so the screen can render a
 *  Hima-styled fallback instead of a blank or crashed view. */
sealed interface MapLoadState {
    data object Loading : MapLoadState
    data object Ready : MapLoadState
    data object MissingConfig : MapLoadState
    data class StyleError(val message: String?) : MapLoadState
}
