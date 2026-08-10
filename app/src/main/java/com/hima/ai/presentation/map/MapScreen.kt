package com.hima.ai.presentation.map

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.LocationServices
import com.hima.ai.R
import com.hima.ai.core.common.relativeTimeLabel
import com.hima.ai.core.designsystem.component.CurrentLocationMarker
import com.hima.ai.core.designsystem.component.FilterPillRow
import com.hima.ai.core.designsystem.component.HimaBottomNavigation
import com.hima.ai.core.designsystem.component.HimaIconButton
import com.hima.ai.core.designsystem.component.HimaPrimaryButton
import com.hima.ai.core.designsystem.component.HimaTab
import com.hima.ai.core.designsystem.component.HimaTextLink
import com.hima.ai.core.designsystem.component.MapClusterMarker
import com.hima.ai.core.designsystem.component.MapMarkerPin
import com.hima.ai.core.designsystem.component.SceneArt
import com.hima.ai.core.designsystem.component.SeverityBadge
import com.hima.ai.core.designsystem.theme.HimaRadius
import com.hima.ai.core.designsystem.theme.HimaTextStyles
import com.hima.ai.core.designsystem.theme.LocalHimaColors
import com.hima.ai.core.location.awaitCurrentLocation
import com.hima.ai.core.map.HimaMapView
import com.hima.ai.core.map.MapConfig
import com.hima.ai.core.map.MapLoadState
import com.hima.ai.core.map.distanceBearing
import com.hima.ai.core.map.recenterToDefault
import com.hima.ai.core.map.recenterToLocation
import com.hima.ai.domain.model.IncidentCategory
import com.hima.ai.domain.model.MapIncident
import com.hima.ai.domain.model.ReportStatus
import com.hima.ai.domain.repository.ReportsLoadState
import kotlinx.coroutines.launch
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap

/**
 * The reserve map — a real MapLibre/MapTiler map underneath the same
 * header/pill/bottom-sheet chrome used everywhere else in the app. Incident
 * markers are still Hima's own Compose pins (not native map symbols), kept
 * in sync with the camera via [MapLibreMap.getProjection]; that's what lets
 * [IncidentSheetContent] and clustering stay exactly as they were. Report
 * data is real (see [MapViewModel]); location is a one-shot fix on tap, never
 * a continuous follow.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onHomeClick: () -> Unit,
    onNewReportClick: () -> Unit,
    onReportsClick: () -> Unit,
    onMoreClick: () -> Unit,
    onViewReportClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalHimaColors.current
    val density = LocalDensity.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fusedLocationClient = remember(context) { LocationServices.getFusedLocationProviderClient(context) }

    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    var markerItems by remember { mutableStateOf<List<MapMarkerItem>>(emptyList()) }
    var userLocationScreenPos by remember { mutableStateOf<Offset?>(null) }
    // Bumping this forces HimaMapView (and the MapView it owns) to recreate,
    // which is the retry mechanism for a style that failed to load.
    var retryKey by remember { mutableIntStateOf(0) }

    fun refreshProjections(currentMap: MapLibreMap) {
        val projection = currentMap.projection
        val positioned = uiState.visibleIncidents.map { incident ->
            val point = projection.toScreenLocation(LatLng(incident.latitude, incident.longitude))
            incident to Offset(point.x, point.y)
        }
        markerItems = clusterIncidents(positioned)
        userLocationScreenPos = uiState.userLocation?.let { location ->
            val point = projection.toScreenLocation(location)
            Offset(point.x, point.y)
        }
    }

    // Camera movement is driven by MapLibre's own listener (registered
    // below), not recomposition — but the filter and the user's location can
    // both change the markers/dot without the camera moving, so they need
    // their own trigger.
    LaunchedEffect(map, uiState.visibleIncidents, uiState.userLocation) {
        map?.let(::refreshProjections)
    }

    DisposableEffect(map) {
        val currentMap = map ?: return@DisposableEffect onDispose {}
        val moveListener = MapLibreMap.OnCameraMoveListener { refreshProjections(currentMap) }
        val idleListener = MapLibreMap.OnCameraIdleListener {
            refreshProjections(currentMap)
            viewModel.onCameraMoved(currentMap.cameraPosition)
        }
        currentMap.addOnCameraMoveListener(moveListener)
        currentMap.addOnCameraIdleListener(idleListener)
        onDispose {
            currentMap.removeOnCameraMoveListener(moveListener)
            currentMap.removeOnCameraIdleListener(idleListener)
        }
    }

    suspend fun fetchLocation(): LatLng? {
        val location = fusedLocationClient.awaitCurrentLocation() ?: return null
        val latLng = LatLng(location.latitude, location.longitude)
        viewModel.onLocationReceived(latLng)
        return latLng
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        viewModel.onLocationPermissionResult(granted)
        if (granted) {
            scope.launch { fetchLocation()?.let { map?.recenterToLocation(it) } ?: map?.recenterToDefault() }
        } else {
            // Denied is a normal outcome, not an error — the map stays fully
            // usable, "My Location" just falls back to the default viewport.
            map?.recenterToDefault()
        }
    }

    fun onMyLocationClick() {
        if (context.hasLocationPermission()) {
            scope.launch { fetchLocation()?.let { map?.recenterToLocation(it) } ?: map?.recenterToDefault() }
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Silent check only — a location dot appears immediately if permission
    // was already granted in an earlier session, but this never itself
    // prompts; the system dialog only ever appears from an explicit tap.
    LaunchedEffect(Unit) {
        if (context.hasLocationPermission()) {
            viewModel.onLocationPermissionResult(true)
            fetchLocation()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Box(Modifier.weight(1f)) {
            // The map's own pixel space must not mirror under Arabic layout —
            // MapLibre's projection always returns LTR screen pixels, so the
            // Compose subtree reading them has to stay LTR too, or markers
            // land offset from where the map actually put them.
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                if (MapConfig.isConfigured) {
                    key(retryKey) {
                        HimaMapView(
                            modifier = Modifier.fillMaxSize(),
                            onMapReady = { readyMap ->
                                readyMap.uiSettings.isRotateGesturesEnabled = false
                                readyMap.uiSettings.isTiltGesturesEnabled = false
                                readyMap.uiSettings.isCompassEnabled = false
                                readyMap.uiSettings.isLogoEnabled = false
                                readyMap.cameraPosition = uiState.lastCameraPosition ?: MapConfig.saudiArabiaDefaultCamera
                                map = readyMap
                            },
                            onLoadStateChanged = viewModel::onMapLoadStateChanged,
                        )
                    }

                    if (uiState.mapLoadState == MapLoadState.Ready) {
                        userLocationScreenPos?.let { position ->
                            val x = with(density) { position.x.toDp() } - 20.dp
                            val y = with(density) { position.y.toDp() } - 20.dp
                            CurrentLocationMarker(modifier = Modifier.absoluteOffset(x = x, y = y))
                        }
                        markerItems.forEach { item ->
                            when (item) {
                                is MapMarkerItem.Single -> {
                                    val incident = item.incident
                                    val x = with(density) { item.screenPosition.x.toDp() } - 24.dp
                                    val y = with(density) { item.screenPosition.y.toDp() } - 24.dp
                                    MapMarkerPin(
                                        category = incident.category,
                                        severity = incident.report.severity,
                                        selected = uiState.selectedIncident?.report?.id == incident.report.id,
                                        onClick = { viewModel.onMarkerClick(incident) },
                                        modifier = Modifier.absoluteOffset(x = x, y = y),
                                    )
                                }
                                is MapMarkerItem.Cluster -> {
                                    val topSeverity = item.incidents.maxBy { it.report.severity.ordinal }.report.severity
                                    val x = with(density) { item.screenPosition.x.toDp() } - 22.dp
                                    val y = with(density) { item.screenPosition.y.toDp() } - 22.dp
                                    MapClusterMarker(
                                        count = item.incidents.size,
                                        severity = topSeverity,
                                        onClick = { map?.let { zoomToCluster(it, item.incidents) } },
                                        modifier = Modifier.absoluteOffset(x = x, y = y),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            when {
                !MapConfig.isConfigured -> MapFallbackNotice(
                    titleRes = R.string.map_config_missing_title,
                    bodyRes = R.string.map_config_missing_body,
                    modifier = Modifier.align(Alignment.Center),
                )
                uiState.mapLoadState is MapLoadState.StyleError -> MapFallbackNotice(
                    titleRes = R.string.map_error_title,
                    bodyRes = R.string.map_error_body,
                    onRetry = { retryKey++ },
                    modifier = Modifier.align(Alignment.Center),
                )
                uiState.mapLoadState == MapLoadState.Loading -> CircularProgressIndicator(
                    color = colors.green,
                    modifier = Modifier.align(Alignment.Center).size(28.dp),
                )
                // The base map loaded fine; only the report data failed —
                // panning/zooming an empty map is still better than nothing.
                uiState.reportsLoadState is ReportsLoadState.Error -> MapFallbackNotice(
                    titleRes = R.string.map_reports_error_title,
                    bodyRes = R.string.map_reports_error_body,
                    onRetry = viewModel::onRetryReports,
                    modifier = Modifier.align(Alignment.Center),
                )
                uiState.mapLoadState == MapLoadState.Ready &&
                    uiState.reportsLoadState == ReportsLoadState.Ready &&
                    uiState.visibleIncidents.isEmpty() -> EmptyFilterNotice(modifier = Modifier.align(Alignment.Center))
                else -> Unit
            }

            MapOverlayControls(
                filter = uiState.filter,
                onFilterSelected = viewModel::onFilterSelected,
                onMyLocationClick = ::onMyLocationClick,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .padding(top = 50.dp, start = 16.dp, end = 16.dp),
            )
        }

        HimaBottomNavigation(
            selected = HimaTab.MAP,
            onHomeClick = onHomeClick,
            onMapClick = {},
            onNewReportClick = onNewReportClick,
            onReportsClick = onReportsClick,
            onMoreClick = onMoreClick,
        )
    }

    val incident = uiState.selectedIncident
    if (incident != null) {
        // Without this, system Back skipped the sheet entirely and popped
        // the Map screen itself — one press took the ranger all the way back
        // to Home instead of just closing the incident sheet.
        BackHandler(onBack = viewModel::onDismissSheet)
        val sheetState = rememberModalBottomSheetState()
        val distanceLabel = uiState.userLocation?.let { userLocation ->
            distanceBearing(userLocation, LatLng(incident.latitude, incident.longitude)).formatLabel()
        }
        ModalBottomSheet(
            onDismissRequest = viewModel::onDismissSheet,
            sheetState = sheetState,
            containerColor = colors.bg,
            shape = RoundedCornerShape(topStart = HimaRadius.sheet, topEnd = HimaRadius.sheet),
            dragHandle = { IncidentSheetHandle() },
        ) {
            IncidentSheetContent(
                incident = incident,
                distanceLabel = distanceLabel,
                onViewReportClick = {
                    viewModel.onDismissSheet()
                    onViewReportClick(incident.report.id)
                },
            )
        }
    }
}

private fun android.content.Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED

/** Animates the camera to a cluster's geographic centroid at a closer zoom —
 *  the real-map equivalent of the old fraction-space "zoom to point". */
private fun zoomToCluster(map: MapLibreMap, incidents: List<MapIncident>) {
    val avgLat = incidents.map { it.latitude }.average()
    val avgLng = incidents.map { it.longitude }.average()
    val targetZoom = (map.cameraPosition.zoom + 2.5).coerceAtMost(14.0)
    map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(avgLat, avgLng), targetZoom), 600)
}

@Composable
private fun MapOverlayControls(
    filter: IncidentCategory?,
    onFilterSelected: (Int) -> Unit,
    onMyLocationClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalHimaColors.current
    Column(modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .shadow(3.dp, RoundedCornerShape(50))
                    .clip(RoundedCornerShape(50))
                    .background(colors.bg)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_field_pin),
                    contentDescription = null,
                    tint = colors.green,
                    modifier = Modifier.size(15.dp),
                )
                Text(
                    text = stringResource(R.string.map_reserve_label),
                    style = HimaTextStyles.m.copy(fontSize = 12.5.sp, fontWeight = FontWeight.Medium),
                    color = colors.ink,
                )
            }
            Spacer(Modifier.weight(1f))
            Box(Modifier.shadow(3.dp, RoundedCornerShape(HimaRadius.icon))) {
                HimaIconButton(
                    iconRes = R.drawable.ic_my_location,
                    contentDescription = stringResource(R.string.cd_my_location),
                    onClick = onMyLocationClick,
                    filled = true,
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        val options = IncidentCategory.entries
        FilterPillRow(
            options = listOf(stringResource(R.string.map_filter_all)) + options.map { stringResource(it.filterLabelRes) },
            selectedIndex = if (filter == null) 0 else options.indexOf(filter) + 1,
            onSelect = onFilterSelected,
        )
    }
}

/** Shown over the map when the active filter matches no markers. */
@Composable
private fun EmptyFilterNotice(modifier: Modifier = Modifier) {
    val colors = LocalHimaColors.current
    Box(
        modifier = modifier
            .shadow(3.dp, RoundedCornerShape(HimaRadius.field))
            .clip(RoundedCornerShape(HimaRadius.field))
            .background(colors.bg)
            .padding(horizontal = 18.dp, vertical = 14.dp),
    ) {
        Text(
            text = stringResource(R.string.map_empty_filter),
            style = HimaTextStyles.b,
            color = colors.sage,
            textAlign = TextAlign.Center,
        )
    }
}

/** Missing config / style-load-failure / reports-fetch-failure fallback —
 *  the same pale-card language as [EmptyFilterNotice], so an unavailable map
 *  still reads as Hima rather than a broken third-party widget. */
@Composable
private fun MapFallbackNotice(
    titleRes: Int,
    bodyRes: Int,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    val colors = LocalHimaColors.current
    Column(
        modifier = modifier
            .padding(horizontal = 36.dp)
            .shadow(3.dp, RoundedCornerShape(HimaRadius.field))
            .clip(RoundedCornerShape(HimaRadius.field))
            .background(colors.bg)
            .padding(horizontal = 24.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_field_pin),
            contentDescription = null,
            tint = colors.sage,
            modifier = Modifier.size(26.dp),
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(titleRes),
            style = HimaTextStyles.t.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
            color = colors.ink,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(bodyRes),
            style = HimaTextStyles.b,
            color = colors.sage,
            textAlign = TextAlign.Center,
        )
        if (onRetry != null) {
            HimaTextLink(
                text = stringResource(R.string.map_retry),
                onClick = onRetry,
                modifier = Modifier.width(160.dp),
            )
        }
    }
}

@Composable
private fun IncidentSheetHandle(modifier: Modifier = Modifier) {
    val colors = LocalHimaColors.current
    Box(
        modifier
            .padding(top = 10.dp, bottom = 4.dp)
            .size(width = 36.dp, height = 4.dp)
            .clip(RoundedCornerShape(50))
            .background(colors.beige),
    )
}

@Composable
private fun IncidentSheetContent(
    incident: MapIncident,
    onViewReportClick: () -> Unit,
    modifier: Modifier = Modifier,
    distanceLabel: String? = null,
) {
    val colors = LocalHimaColors.current
    val report = incident.report
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(HimaRadius.thumb)),
            ) {
                SceneArt(kind = report.scene, modifier = Modifier.fillMaxSize())
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 13.dp, end = 13.dp),
            ) {
                Text(
                    text = report.titleOverride ?: stringResource(report.titleRes),
                    style = HimaTextStyles.t.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                    color = colors.ink,
                )
                // Distance/direction from the ranger's own position takes the
                // subtitle's first slot when a fix is available — a report's
                // relation to *me* is more useful in the field than its
                // coordinates alone.
                val locationText = distanceLabel ?: report.locationOverride ?: stringResource(report.locationRes)
                val timeText = report.createdAt?.let { relativeTimeLabel(it) } ?: stringResource(report.timeRes)
                Text(
                    text = "$locationText · $timeText",
                    style = HimaTextStyles.m,
                    color = colors.sage,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                SeverityBadge(report.severity)
                Text(
                    text = stringResource(
                        if (report.status == ReportStatus.OPEN) {
                            R.string.history_filter_open
                        } else {
                            R.string.history_filter_done
                        },
                    ),
                    style = HimaTextStyles.m.copy(fontSize = 11.sp),
                    color = colors.sage,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }

        HimaPrimaryButton(
            text = stringResource(R.string.map_view_report),
            onClick = onViewReportClick,
            leadingIconRes = R.drawable.ic_chevron,
            modifier = Modifier.padding(top = 22.dp, bottom = 28.dp),
        )
    }
}
