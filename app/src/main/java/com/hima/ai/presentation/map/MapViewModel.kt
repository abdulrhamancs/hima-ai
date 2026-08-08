package com.hima.ai.presentation.map

import androidx.lifecycle.ViewModel
import com.hima.ai.data.mock.MockData
import com.hima.ai.domain.model.IncidentCategory
import com.hima.ai.domain.model.MapIncident
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MapUiState(
    val incidents: List<MapIncident> = emptyList(),
    val filter: IncidentCategory? = null,
    val selectedIncident: MapIncident? = null,
) {
    /** Markers matching the active filter (null = all categories). */
    val visibleIncidents: List<MapIncident>
        get() = if (filter == null) incidents else incidents.filter { it.category == filter }
}

@HiltViewModel
class MapViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState(incidents = MockData.mapIncidents))
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

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
}
