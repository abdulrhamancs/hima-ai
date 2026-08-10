package com.hima.ai.domain.repository

import com.hima.ai.domain.model.FireHotspot
import kotlinx.coroutines.flow.StateFlow

/** What [FireHotspotsRepository.loadState] is currently showing. A [Error]
 *  here must never block the report map — see [FireHotspotsRepository]. */
sealed interface FireLoadState {
    data object Idle : FireLoadState
    data object Loading : FireLoadState
    data object Ready : FireLoadState
    data class Error(val message: String) : FireLoadState
}

/**
 * NASA FIRMS active-fire hotspots for Saudi Arabia, proxied through our own
 * backend's `GET /fires` (see backend/routes/fires.js) so the NASA MAP_KEY
 * never reaches the app. FIRMS' own WMS/WFS layers refresh roughly every 15
 * minutes, so [refresh] is a no-op within that window unless [force] is set —
 * polling faster would only burn the backend's NASA quota for identical data.
 */
interface FireHotspotsRepository {
    val hotspots: StateFlow<List<FireHotspot>>
    val loadState: StateFlow<FireLoadState>

    suspend fun refresh(force: Boolean = false)
}
