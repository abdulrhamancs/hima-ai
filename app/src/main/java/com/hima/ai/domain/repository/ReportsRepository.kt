package com.hima.ai.domain.repository

import com.hima.ai.domain.model.MapIncident
import com.hima.ai.domain.model.ReportSummary
import kotlinx.coroutines.flow.StateFlow

/** What [ReportsRepository.loadState] is currently showing, for a caller to
 *  render loading/ready/error/empty without its own network awareness. */
sealed interface ReportsLoadState {
    data object Idle : ReportsLoadState
    data object Loading : ReportsLoadState
    data object Ready : ReportsLoadState
    data class Error(val message: String) : ReportsLoadState
}

/**
 * Real field reports for the map, from Supabase directly — the same
 * direct-REST pattern [AuthRepository] already uses, not a new backend
 * endpoint. [refresh] is idempotent once loaded, so navigating to the map
 * repeatedly doesn't re-fetch every time; [findById] serves the already
 * -loaded list so opening a marker's full report never needs a second call.
 */
interface ReportsRepository {
    val mapIncidents: StateFlow<List<MapIncident>>
    val loadState: StateFlow<ReportsLoadState>

    suspend fun refresh(force: Boolean = false)

    fun findById(id: String): ReportSummary?
}
