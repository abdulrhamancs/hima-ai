package com.hima.ai.data.repository

import com.hima.ai.R
import com.hima.ai.core.common.ApiResult
import com.hima.ai.core.common.safeApiCall
import com.hima.ai.data.remote.supabase.AiAnalysisDto
import com.hima.ai.data.remote.supabase.PostgrestErrorResponse
import com.hima.ai.data.remote.supabase.ReportDto
import com.hima.ai.data.remote.supabase.SupabaseRestApi
import com.hima.ai.domain.model.AiAnalysis
import com.hima.ai.domain.model.AnalysisResultCategory
import com.hima.ai.domain.model.CircularAction
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
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The single report source used by Home, History, Map, and Detail. Supabase is
 * queried once and the map list is derived from the same objects, excluding
 * only rows that genuinely have no usable coordinates.
 */
@Singleton
class SupabaseReportsRepository @Inject constructor(
    private val restApi: SupabaseRestApi,
    private val authRepository: AuthRepository,
    private val moshi: Moshi,
) : ReportsRepository {

    private val _reports = MutableStateFlow<List<ReportSummary>>(emptyList())
    override val reports: StateFlow<List<ReportSummary>> = _reports.asStateFlow()

    private val _mapIncidents = MutableStateFlow<List<MapIncident>>(emptyList())
    override val mapIncidents: StateFlow<List<MapIncident>> = _mapIncidents.asStateFlow()

    private val _loadState = MutableStateFlow<ReportsLoadState>(ReportsLoadState.Idle)
    override val loadState: StateFlow<ReportsLoadState> = _loadState.asStateFlow()

    override suspend fun refresh(force: Boolean) {
        if (!force && _loadState.value == ReportsLoadState.Ready) return

        val token = authRepository.currentSession.value?.accessToken
        if (token == null) {
            _loadState.value = ReportsLoadState.Error("Not signed in.")
            return
        }

        _loadState.value = ReportsLoadState.Loading
        when (val result = moshi.safeApiCall(::parseError) { restApi.getReports("Bearer $token") }) {
            is ApiResult.Success -> {
                val mappedReports = result.value.map { it.toReportSummary() }
                _reports.value = mappedReports
                _mapIncidents.value = mappedReports.mapNotNull { report ->
                    val latitude = report.latitude
                    val longitude = report.longitude
                    if (latitude == null || longitude == null ||
                        latitude !in -90.0..90.0 || longitude !in -180.0..180.0
                    ) {
                        null
                    } else {
                        MapIncident(
                            report = report,
                            category = report.category,
                            latitude = latitude,
                            longitude = longitude,
                        )
                    }
                }
                _loadState.value = ReportsLoadState.Ready
            }
            is ApiResult.Failure -> _loadState.value = ReportsLoadState.Error(result.error.message)
        }
    }

    override fun findById(id: String): ReportSummary? =
        _reports.value.firstOrNull { it.id == id }

    private fun parseError(raw: String): String? =
        runCatching { moshi.adapter(PostgrestErrorResponse::class.java).fromJson(raw)?.message }.getOrNull()
}

internal fun ReportDto.toReportSummary(): ReportSummary {
    val category = IncidentCategory.fromBackendType(type)
    val severityValue = severity.toSeverity()
    val statusValue = when (status?.uppercase()) {
        "OPEN" -> ReportStatus.OPEN
        "RESOLVED" -> ReportStatus.RESOLVED
        else -> ReportStatus.UNKNOWN
    }
    val createdInstant = createdAt?.let { runCatching { Instant.parse(it) }.getOrNull() }
    val analysisValue = aiAnalysis?.toDomain(
        reportId = id,
        imageUrl = imageUrl,
        category = category,
        reportDescription = description,
        reportRecommendation = recommendedAction,
        reportEnvironmentalImpact = environmentalImpact,
        reportConfidence = confidence,
        reportSeverity = severityValue,
    )
    val detectedTitle = when (category) {
        IncidentCategory.WASTE -> analysisValue?.wasteType
        else -> analysisValue?.issueType?.takeUnless { it.isBackendCode() }
    }?.takeIf { it.isNotBlank() }

    return ReportSummary(
        id = id,
        titleRes = category.singularLabelRes,
        titleOverride = detectedTitle,
        category = category,
        locationRes = R.string.report_location_unavailable,
        locationOverride = if (latitude != null && longitude != null) {
            formatCoordinates(latitude, longitude)
        } else {
            null
        },
        timeRes = R.string.time_unknown,
        createdAt = createdInstant,
        severity = severityValue,
        status = statusValue,
        scene = category.toSceneKind(),
        imageUrl = imageUrl?.takeIf { it.isNotBlank() },
        latitude = latitude,
        longitude = longitude,
        reasonOverride = analysisValue?.description?.takeIf { it.isNotBlank() }
            ?: description?.takeIf { it.isNotBlank() },
        recommendationOverride = analysisValue?.recommendation?.takeIf { it.isNotBlank() }
            ?: recommendedAction?.takeIf { it.isNotBlank() },
        environmentalImpactOverride = analysisValue?.environmentalImpact?.takeIf { it.isNotBlank() }
            ?: environmentalImpact?.takeIf { it.isNotBlank() },
        confidence = analysisValue?.confidence ?: confidence?.roundToInt(),
        analysis = analysisValue,
        riskScore = analysisValue?.riskScore,
    )
}

private fun AiAnalysisDto.toDomain(
    reportId: String,
    imageUrl: String?,
    category: IncidentCategory,
    reportDescription: String?,
    reportRecommendation: String?,
    reportEnvironmentalImpact: String?,
    reportConfidence: Double?,
    reportSeverity: Severity,
): AiAnalysis {
    val resultCategoryValue = if (
        resultCategory.equals("recyclable_waste", ignoreCase = true) || category == IncidentCategory.WASTE
    ) {
        AnalysisResultCategory.RECYCLABLE_WASTE
    } else {
        AnalysisResultCategory.ENVIRONMENTAL_INCIDENT
    }
    return AiAnalysis(
        category = resultCategoryValue,
        description = description?.takeIf { it.isNotBlank() }
            ?: reportDescription.orEmpty(),
        confidence = (confidence ?: reportConfidence ?: 0.0).roundToInt().coerceIn(0, 100),
        recommendation = recommendation?.takeIf { it.isNotBlank() }
            ?: reportRecommendation.orEmpty(),
        reportId = reportId,
        imageUrl = imageUrl,
        environmentalImpact = environmentalImpact?.takeIf { it.isNotBlank() }
            ?: reportEnvironmentalImpact,
        aiExplanation = aiExplanation,
        issueType = issueType,
        riskScore = riskScore?.roundToInt()?.coerceIn(0, 100),
        riskLevel = riskLevel.toSeverity().takeUnless { it == Severity.UNKNOWN } ?: reportSeverity,
        materialCategory = materialCategory,
        wasteType = wasteType,
        disposalClassification = disposalClassification,
        recyclable = recyclable,
        reusable = reusable,
        repairable = repairable,
        preferredAction = preferredAction.toCircularAction(),
        reuseSuggestion = reuseSuggestion,
        repairGuidance = repairGuidance,
        recyclingGuidance = recyclingGuidance,
        disposalGuidance = disposalGuidance,
    )
}

private fun String?.toSeverity(): Severity = when (this?.uppercase()) {
    "LOW" -> Severity.LOW
    "MEDIUM" -> Severity.MEDIUM
    "HIGH" -> Severity.HIGH
    "CRITICAL" -> Severity.CRITICAL
    else -> Severity.UNKNOWN
}

private fun String?.toCircularAction(): CircularAction? = when (this?.lowercase()) {
    "reuse" -> CircularAction.REUSE
    "repair_refurbish" -> CircularAction.REPAIR_REFURBISH
    "donate_repurpose" -> CircularAction.DONATE_REPURPOSE
    "recycle" -> CircularAction.RECYCLE
    "material_recovery" -> CircularAction.MATERIAL_RECOVERY
    "safe_disposal" -> CircularAction.SAFE_DISPOSAL
    else -> null
}

private fun String.isBackendCode(): Boolean = uppercase() in setOf(
    "FIRE",
    "ILLEGAL_LOGGING",
    "ILLEGAL_HUNTING",
    "WASTE",
    "WATER_POLLUTION",
    "PLANT_DISEASE",
    "INJURED_ANIMAL",
    "DEAD_ANIMAL",
    "OTHER",
)

private fun IncidentCategory.toSceneKind(): SceneKind = when (this) {
    IncidentCategory.FIRE -> SceneKind.FIRE
    IncidentCategory.LOGGING -> SceneKind.STUMP
    IncidentCategory.POACHING -> SceneKind.VALLEY
    IncidentCategory.POLLUTION -> SceneKind.WATER
    IncidentCategory.WASTE -> SceneKind.WASTE
    IncidentCategory.WILDLIFE, IncidentCategory.PLANT_DISEASE, IncidentCategory.OTHER -> SceneKind.FOREST
}

private fun formatCoordinates(latitude: Double, longitude: Double): String {
    val latHemisphere = if (latitude >= 0) "N" else "S"
    val lngHemisphere = if (longitude >= 0) "E" else "W"
    return String.format(
        Locale.US,
        "%.4f\u00B0%s, %.4f\u00B0%s",
        kotlin.math.abs(latitude),
        latHemisphere,
        kotlin.math.abs(longitude),
        lngHemisphere,
    )
}
