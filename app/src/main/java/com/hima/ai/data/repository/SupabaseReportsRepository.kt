package com.hima.ai.data.repository

import com.hima.ai.core.common.ApiResult
import com.hima.ai.core.common.safeApiCall
import com.hima.ai.data.remote.supabase.PostgrestErrorResponse
import com.hima.ai.data.remote.supabase.ReportDto
import com.hima.ai.data.remote.supabase.SupabaseRestApi
import com.hima.ai.domain.model.IncidentCategory
import com.hima.ai.domain.model.MapIncident
import com.hima.ai.domain.model.ReportStatus
import com.hima.ai.domain.model.ReportSummary
import com.hima.ai.domain.model.SceneKind
import com.hima.ai.domain.model.Severity
import com.hima.ai.domain.repository.AuthRepository
import com.hima.ai.domain.repository.ReportsLoadState
import com.hima.ai.domain.repository.ReportsRepository
import com.squareup.moshi.Moshi
import java.time.Instant
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class SupabaseReportsRepository @Inject constructor(
    private val restApi: SupabaseRestApi,
    private val authRepository: AuthRepository,
    private val moshi: Moshi,
) : ReportsRepository {

    private val _mapIncidents = MutableStateFlow<List<MapIncident>>(emptyList())
    override val mapIncidents: StateFlow<List<MapIncident>> = _mapIncidents.asStateFlow()

    private val _loadState = MutableStateFlow<ReportsLoadState>(ReportsLoadState.Idle)
    override val loadState: StateFlow<ReportsLoadState> = _loadState.asStateFlow()

    override suspend fun refresh(force: Boolean) {
        // Already have a good list — repeated Map visits shouldn't re-fetch,
        // only an explicit retry or the first-ever load should.
        if (!force && _loadState.value == ReportsLoadState.Ready) return

        val token = authRepository.currentSession.value?.accessToken
        if (token == null) {
            _loadState.value = ReportsLoadState.Error("Not signed in.")
            return
        }

        _loadState.value = ReportsLoadState.Loading
        when (val result = moshi.safeApiCall(::parseError) { restApi.getReports("Bearer $token") }) {
            is ApiResult.Success -> {
                _mapIncidents.value = result.value.mapNotNull { it.toMapIncident() }
                _loadState.value = ReportsLoadState.Ready
            }
            is ApiResult.Failure -> _loadState.value = ReportsLoadState.Error(result.error.message)
        }
    }

    override fun findById(id: String): ReportSummary? =
        _mapIncidents.value.firstOrNull { it.report.id == id }?.report

    private fun parseError(raw: String): String? =
        runCatching { moshi.adapter(PostgrestErrorResponse::class.java).fromJson(raw)?.message }.getOrNull()
}

/** A row missing a parseable severity is malformed data, not just an unknown
 *  category — safer to drop it than guess a severity for a marker's colour. */
private fun ReportDto.toMapIncident(): MapIncident? {
    val severityEnum = runCatching { Severity.valueOf(severity.uppercase()) }.getOrNull() ?: return null
    val statusEnum = runCatching { ReportStatus.valueOf(status.uppercase()) }.getOrDefault(ReportStatus.OPEN)
    val category = IncidentCategory.fromBackendType(type)
    val createdInstant = createdAt?.let { runCatching { Instant.parse(it) }.getOrNull() }

    return MapIncident(
        report = ReportSummary(
            id = id,
            titleRes = category.singularLabelRes,
            locationRes = category.singularLabelRes, // unused: locationOverride is always set below
            locationOverride = formatCoordinates(latitude, longitude),
            timeRes = category.singularLabelRes, // unused: createdAt drives display when present
            createdAt = createdInstant,
            severity = severityEnum,
            status = statusEnum,
            scene = category.toSceneKind(),
            reasonOverride = description,
            recommendationOverride = recommendedAction,
            riskScore = aiAnalysis?.riskScore,
        ),
        category = category,
        latitude = latitude,
        longitude = longitude,
    )
}

private fun IncidentCategory.toSceneKind(): SceneKind = when (this) {
    IncidentCategory.FIRE -> SceneKind.FIRE
    IncidentCategory.LOGGING -> SceneKind.STUMP
    IncidentCategory.POACHING -> SceneKind.VALLEY
    IncidentCategory.POLLUTION -> SceneKind.WATER
    IncidentCategory.WILDLIFE -> SceneKind.FOREST
    IncidentCategory.PLANT_DISEASE -> SceneKind.FOREST
    IncidentCategory.OTHER -> SceneKind.FOREST
}

private fun formatCoordinates(latitude: Double, longitude: Double): String {
    val latHemisphere = if (latitude >= 0) "N" else "S"
    val lngHemisphere = if (longitude >= 0) "E" else "W"
    return String.format(
        Locale.US,
        "%.4f°%s, %.4f°%s",
        Math.abs(latitude),
        latHemisphere,
        Math.abs(longitude),
        lngHemisphere,
    )
}
