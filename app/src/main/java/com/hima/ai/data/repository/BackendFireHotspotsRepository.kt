package com.hima.ai.data.repository

import com.hima.ai.core.common.ApiResult
import com.hima.ai.core.common.safeApiCall
import com.hima.ai.data.remote.backend.FireDetectionDto
import com.hima.ai.data.remote.backend.FiresResponseDto
import com.hima.ai.data.remote.backend.HimaBackendApi
import com.hima.ai.domain.model.FireHotspot
import com.hima.ai.domain.repository.AuthRepository
import com.hima.ai.domain.repository.FireHotspotsRepository
import com.hima.ai.domain.repository.FireLoadState
import com.squareup.moshi.Moshi
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** FIRMS' own WMS/WFS layers update roughly every 15 minutes — polling more
 *  often than this only spends the backend's NASA quota on identical data. */
private val REFRESH_INTERVAL: Duration = Duration.ofMinutes(15)

/** The freshest slice of the FIRMS Area API's 1-5 day window — enough for an
 *  MVP "what's burning right now" view without an oversized payload. */
private const val REQUEST_WINDOW_DAYS = 1

@Singleton
class BackendFireHotspotsRepository @Inject constructor(
    private val backendApi: HimaBackendApi,
    private val authRepository: AuthRepository,
    private val moshi: Moshi,
) : FireHotspotsRepository {

    private val _hotspots = MutableStateFlow<List<FireHotspot>>(emptyList())
    override val hotspots: StateFlow<List<FireHotspot>> = _hotspots.asStateFlow()

    private val _loadState = MutableStateFlow<FireLoadState>(FireLoadState.Idle)
    override val loadState: StateFlow<FireLoadState> = _loadState.asStateFlow()

    private var lastFetchedAt: Instant? = null

    override suspend fun refresh(force: Boolean) {
        val fetchedAt = lastFetchedAt
        val isFresh = fetchedAt != null && Duration.between(fetchedAt, Instant.now()) < REFRESH_INTERVAL
        if (!force && isFresh) return

        val token = authRepository.currentSession.value?.accessToken
        if (token == null) {
            _loadState.value = FireLoadState.Error("Not signed in.")
            return
        }

        _loadState.value = FireLoadState.Loading
        when (
            val result = moshi.safeApiCall(::parseError, label = "fires/firms") {
                backendApi.getFires("Bearer $token", days = REQUEST_WINDOW_DAYS)
            }
        ) {
            is ApiResult.Success -> {
                _hotspots.value = result.value.detections.orEmpty().mapNotNull { it.toDomain() }
                lastFetchedAt = Instant.now()
                _loadState.value = FireLoadState.Ready
            }
            // A FIRMS/backend outage never clears out already-loaded hotspots
            // and never touches report data — the map keeps working either way.
            is ApiResult.Failure -> _loadState.value = FireLoadState.Error(result.error.message)
        }
    }

    private fun parseError(raw: String): String? =
        runCatching { moshi.adapter(FiresResponseDto::class.java).fromJson(raw)?.error }.getOrNull()
}

private val ACQ_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm")

private fun FireDetectionDto.toDomain(): FireHotspot? {
    val lat = latitude
    val lng = longitude
    if (lat == null || lng == null || lat !in -90.0..90.0 || lng !in -180.0..180.0) return null
    return FireHotspot(
        latitude = lat,
        longitude = lng,
        brightness = brightness,
        confidence = confidence?.takeIf { it.isNotBlank() },
        satellite = satellite?.takeIf { it.isNotBlank() },
        acquiredAt = parseAcquiredAt(acqDate, acqTime),
    )
}

/** FIRMS reports `acq_date` ("2026-08-10") and `acq_time` ("1345", UTC, not
 *  always zero-padded to 4 digits) as two separate CSV columns. */
private fun parseAcquiredAt(acqDate: String?, acqTime: String?): Instant? {
    if (acqDate.isNullOrBlank() || acqTime.isNullOrBlank()) return null
    return runCatching {
        val paddedTime = acqTime.trim().padStart(4, '0')
        LocalDateTime.parse("${acqDate.trim()} $paddedTime", ACQ_DATE_TIME_FORMATTER)
            .toInstant(ZoneOffset.UTC)
    }.getOrNull()
}
