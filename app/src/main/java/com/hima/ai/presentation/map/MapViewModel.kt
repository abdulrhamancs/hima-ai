package com.hima.ai.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.hima.ai.core.navigation.HimaDestinations
import com.hima.ai.core.map.MapConfig
import com.hima.ai.core.map.MapLoadState
import com.hima.ai.domain.model.FireHotspot
import com.hima.ai.domain.model.IncidentCategory
import com.hima.ai.domain.model.MapIncident
import com.hima.ai.domain.repository.FireHotspotsRepository
import com.hima.ai.domain.repository.FireLoadState
import com.hima.ai.domain.repository.ReportsLoadState
import com.hima.ai.domain.repository.ReportsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng

/** Matches [com.hima.ai.data.repository.BackendFireHotspotsRepository]'s own
 *  refresh window — FIRMS' underlying WMS/WFS layers don't change faster
 *  than this, so polling more often would be wasted backend/NASA quota. */
private val FIRE_REFRESH_INTERVAL = 15.minutes

/**
 * The map's single-select filter row. Report categories and the NASA layer
 * share one row because they are alternatives the ranger picks between, but
 * they stay separate *kinds* of selection here — [Category] filters Supabase
 * reports, [NasaFires] shows only satellite detections and no reports at all.
 */
sealed interface MapFilter {
    data object All : MapFilter
    data class Category(val category: IncidentCategory) : MapFilter
    data object NasaFires : MapFilter

    /** The filter row's pill order lives here rather than in the screen and
     *  the ViewModel separately — both encode and decode against this, so an
     *  added category can't leave the two disagreeing about what index means. */
    companion object {
        fun fromRowIndex(index: Int): MapFilter {
            val categories = IncidentCategory.entries
            return when (index) {
                0 -> All
                in 1..categories.size -> Category(categories[index - 1])
                else -> NasaFires
            }
        }
    }

    val rowIndex: Int
        get() = when (this) {
            All -> 0
            is Category -> IncidentCategory.entries.indexOf(category) + 1
            NasaFires -> IncidentCategory.entries.size + 1
        }
}

data class MapUiState(
    val incidents: List<MapIncident> = emptyList(),
    val reportsLoadState: ReportsLoadState = ReportsLoadState.Idle,
    val filter: MapFilter = MapFilter.All,
    val selectedIncident: MapIncident? = null,
    /** One-shot request from Report Detail to center/select this exact marker. */
    val focusIncident: MapIncident? = null,
    /** Map *rendering* state (style/tiles) — separate from [reportsLoadState],
     *  which is about the report *data*; the base map and the markers on it
     *  load independently. */
    val mapLoadState: MapLoadState = if (MapConfig.isConfigured) MapLoadState.Loading else MapLoadState.MissingConfig,
    /** Last camera position this ViewModel instance saw. The map view itself
     *  is torn down when the ranger navigates away (see [com.hima.ai.core.map.HimaMapView]),
     *  so without this a returning ranger would always land back on the
     *  Saudi Arabia default instead of where they were looking. */
    val lastCameraPosition: CameraPosition? = null,
    val userLocation: LatLng? = null,
    val locationPermissionGranted: Boolean = false,
    /** Denied at least once already, so offer Settings rather than re-prompting into a no-op. */
    val locationPermissionPermanentlyDenied: Boolean = false,
    /** NASA FIRMS satellite detections — a wholly separate source from
     *  [incidents]; never a user report, so it gets its own state and its own
     *  selection below rather than folding into [selectedIncident]. */
    val fireHotspots: List<FireHotspot> = emptyList(),
    val fireLoadState: FireLoadState = FireLoadState.Idle,
    val selectedFireHotspot: FireHotspot? = null,
) {
    /** Report markers matching the active filter. Picking the NASA layer
     *  hides reports entirely — the two sources are never interleaved under
     *  one selection. */
    val visibleIncidents: List<MapIncident>
        get() = when (filter) {
            MapFilter.All -> incidents
            is MapFilter.Category -> incidents.filter { it.category == filter.category }
            MapFilter.NasaFires -> emptyList()
        }

    /** Satellite detections are drawn alongside reports only under "All";
     *  choosing a report category means the ranger asked for that one thing. */
    val showNasaFires: Boolean
        get() = filter == MapFilter.All || filter == MapFilter.NasaFires
}

@HiltViewModel
class MapViewModel @Inject constructor(
    private val reportsRepository: ReportsRepository,
    private val fireHotspotsRepository: FireHotspotsRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private var locationDenials = 0
    private var focusHandled = false
    private val focusReportId = savedStateHandle.get<String>(HimaDestinations.MAP_ARG_REPORT_ID)
        ?.takeIf { it.isNotBlank() }

    /** True only when Detail intentionally opened this map for one report. */
    val openedForFocusedReport: Boolean = focusReportId != null

    init {
        // Report data and map tiles load independently and in parallel —
        // neither needs to wait on the other.
        viewModelScope.launch {
            reportsRepository.mapIncidents.collect { list ->
                _uiState.update { state ->
                    val focused = if (!focusHandled) {
                        focusReportId?.let { id -> list.firstOrNull { it.report.id == id } }
                    } else {
                        null
                    }
                    state.copy(
                        incidents = list,
                        selectedIncident = focused ?: state.selectedIncident,
                        focusIncident = focused,
                    )
                }
            }
        }
        viewModelScope.launch { reportsRepository.loadState.collect { state -> _uiState.update { it.copy(reportsLoadState = state) } } }
        viewModelScope.launch { reportsRepository.refresh() }

        // Fire hotspots load fully independently of report data — a FIRMS
        // outage must never block or clear the report map (see MapScreen).
        viewModelScope.launch {
            fireHotspotsRepository.hotspots.collect { list -> _uiState.update { it.copy(fireHotspots = list) } }
        }
        viewModelScope.launch {
            fireHotspotsRepository.loadState.collect { state -> _uiState.update { it.copy(fireLoadState = state) } }
        }
        viewModelScope.launch { fireHotspotsRepository.refresh() }
        // Re-check every 15 minutes while the ranger stays on this screen —
        // refresh() itself is the freshness gate, so this never over-polls.
        viewModelScope.launch {
            while (true) {
                delay(FIRE_REFRESH_INTERVAL)
                fireHotspotsRepository.refresh(force = true)
            }
        }
    }

    fun onRetryReports() {
        viewModelScope.launch { reportsRepository.refresh(force = true) }
    }

    fun onRetryFires() {
        viewModelScope.launch { fireHotspotsRepository.refresh(force = true) }
    }

    /** [index] is a position in the map's filter pill row — see [MapFilter.fromRowIndex]. */
    fun onFilterSelected(index: Int) {
        val filter = MapFilter.fromRowIndex(index)
        _uiState.update { state ->
            // A filter that hides the open sheet's marker must close that
            // sheet too, or it outlives the thing it describes.
            val next = state.copy(filter = filter)
            next.copy(
                selectedIncident = next.selectedIncident?.takeIf { it in next.visibleIncidents },
                selectedFireHotspot = next.selectedFireHotspot?.takeIf { next.showNasaFires },
            )
        }
    }

    fun onMarkerClick(incident: MapIncident) {
        // The two sheets are mutually exclusive — selecting a report closes
        // any open NASA hotspot popup, matching how onFireHotspotClick below
        // closes the report sheet.
        _uiState.update { it.copy(selectedIncident = incident, selectedFireHotspot = null) }
    }

    fun onDismissSheet() {
        _uiState.update { it.copy(selectedIncident = null) }
    }

    fun onFireHotspotClick(hotspot: FireHotspot) {
        _uiState.update { it.copy(selectedFireHotspot = hotspot, selectedIncident = null) }
    }

    fun onDismissFireSheet() {
        _uiState.update { it.copy(selectedFireHotspot = null) }
    }

    fun onFocusHandled() {
        focusHandled = true
        _uiState.update { it.copy(focusIncident = null) }
    }

    fun onMapLoadStateChanged(state: MapLoadState) {
        _uiState.update { it.copy(mapLoadState = state) }
    }

    fun onCameraMoved(position: CameraPosition) {
        _uiState.update { it.copy(lastCameraPosition = position) }
    }

    fun onLocationPermissionResult(granted: Boolean) {
        if (!granted) locationDenials++
        _uiState.update {
            it.copy(
                locationPermissionGranted = granted,
                // Android stops showing the system dialog after the second
                // refusal, so only then is Settings the honest next step.
                locationPermissionPermanentlyDenied = !granted && locationDenials >= 2,
            )
        }
    }

    fun onLocationReceived(location: LatLng?) {
        _uiState.update { it.copy(userLocation = location) }
    }
}
