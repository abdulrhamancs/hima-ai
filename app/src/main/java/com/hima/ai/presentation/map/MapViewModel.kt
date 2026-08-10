package com.hima.ai.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.hima.ai.core.navigation.HimaDestinations
import com.hima.ai.core.map.MapConfig
import com.hima.ai.core.map.MapLoadState
import com.hima.ai.domain.model.IncidentCategory
import com.hima.ai.domain.model.MapIncident
import com.hima.ai.domain.repository.ReportsLoadState
import com.hima.ai.domain.repository.ReportsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng

data class MapUiState(
    val incidents: List<MapIncident> = emptyList(),
    val reportsLoadState: ReportsLoadState = ReportsLoadState.Idle,
    val filter: IncidentCategory? = null,
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
) {
    /** Markers matching the active filter (null = all categories). */
    val visibleIncidents: List<MapIncident>
        get() = if (filter == null) incidents else incidents.filter { it.category == filter }
}

@HiltViewModel
class MapViewModel @Inject constructor(
    private val reportsRepository: ReportsRepository,
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
    }

    fun onRetryReports() {
        viewModelScope.launch { reportsRepository.refresh(force = true) }
    }

    /** `null` selects "All"; any other index maps to an [IncidentCategory]. */
    fun onFilterSelected(index: Int) {
        val category = if (index == 0) null else IncidentCategory.entries.getOrNull(index - 1)
        _uiState.update { it.copy(filter = category) }
    }

    fun onMarkerClick(incident: MapIncident) {
        _uiState.update { it.copy(selectedIncident = incident) }
    }

    fun onDismissSheet() {
        _uiState.update { it.copy(selectedIncident = null) }
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
