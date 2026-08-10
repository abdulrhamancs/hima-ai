package com.hima.ai.presentation.map

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.LocationServices
import com.hima.ai.R
import com.hima.ai.core.common.relativeTimeLabel
import com.hima.ai.core.designsystem.component.CurrentLocationMarker
import com.hima.ai.core.designsystem.component.FilterPillRow
import com.hima.ai.core.designsystem.component.FireHotspotMarker
import com.hima.ai.core.designsystem.component.HimaBottomNavigation
import com.hima.ai.core.designsystem.component.HimaIconButton
import com.hima.ai.core.designsystem.component.HimaPrimaryButton
import com.hima.ai.core.designsystem.component.HimaTab
import com.hima.ai.core.designsystem.component.HimaTextLink
import com.hima.ai.core.designsystem.component.MapClusterMarker
import com.hima.ai.core.designsystem.component.MapMarkerPin
import com.hima.ai.core.designsystem.component.ReportImage
import com.hima.ai.core.designsystem.component.SeverityBadge
import com.hima.ai.core.designsystem.theme.HimaRadius
import com.hima.ai.core.designsystem.theme.HimaTextStyles
import com.hima.ai.core.designsystem.theme.LocalHimaColors
import com.hima.ai.core.location.awaitCurrentLocation
import com.hima.ai.core.location.hasLocationPermission
import com.hima.ai.core.map.HimaMapView
import com.hima.ai.core.map.MapConfig
import com.hima.ai.core.map.MapLoadState
import com.hima.ai.core.map.distanceBearing
import com.hima.ai.core.map.recenterToDefault
import com.hima.ai.core.map.recenterToLocation
import com.hima.ai.domain.model.FireHotspot
import com.hima.ai.domain.model.IncidentCategory
import com.hima.ai.domain.model.MapIncident
import com.hima.ai.domain.model.ReportStatus
import com.hima.ai.domain.model.Severity
import com.hima.ai.domain.repository.FireLoadState
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
    onBackClick: () -> Unit,
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
    var fireMarkerItems by remember { mutableStateOf<List<Pair<FireHotspot, Offset>>>(emptyList()) }
    var userLocationScreenPos by remember { mutableStateOf<Offset?>(null) }
    // The map surface's own pixel size, used to cull markers projected
    // outside it — see refreshProjections.
    var mapSizePx by remember { mutableStateOf(IntSize.Zero) }
    // Bumping this forces HimaMapView (and the MapView it owns) to recreate,
    // which is the retry mechanism for a style that failed to load.
    var retryKey by remember { mutableIntStateOf(0) }

    fun refreshProjections(currentMap: MapLibreMap) {
        val projection = currentMap.projection
        val positioned = uiState.visibleIncidents.map { incident ->
            val point = projection.toScreenLocation(LatLng(incident.latitude, incident.longitude))
            incident to Offset(point.x, point.y)
        }
        // Cluster over every report, then drop the off-screen results: the
        // counts stay correct for clusters straddling the edge, while the
        // number of composed markers is bounded by the viewport rather than
        // by how many reports exist. Same reason the hotspot layer is culled.
        markerItems = clusterIncidents(positioned).filter { it.screenPosition.isOnScreen(mapSizePx) }
        // NASA hotspots are never clustered with reports (and not clustered
        // with each other for this MVP) — a wholly separate visual layer,
        // never sharing an id space with [MapIncident].
        fireMarkerItems = if (uiState.showNasaFires) {
            uiState.fireHotspots.mapNotNull { hotspot ->
                val point = projection.toScreenLocation(LatLng(hotspot.latitude, hotspot.longitude))
                val offset = Offset(point.x, point.y)
                if (offset.isOnScreen(mapSizePx)) hotspot to offset else null
            }
        } else {
            emptyList()
        }
        userLocationScreenPos = uiState.userLocation?.let { location ->
            val point = projection.toScreenLocation(location)
            Offset(point.x, point.y)
        }
    }

    // Camera movement is driven by MapLibre's own listener (registered
    // below), not recomposition — but the filter and the user's location can
    // both change the markers/dot without the camera moving, so they need
    // their own trigger.
    LaunchedEffect(map, uiState.visibleIncidents, uiState.fireHotspots, uiState.showNasaFires, uiState.userLocation) {
        map?.let(::refreshProjections)
    }

    LaunchedEffect(map, uiState.focusIncident) {
        val currentMap = map ?: return@LaunchedEffect
        val incident = uiState.focusIncident ?: return@LaunchedEffect
        currentMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(incident.latitude, incident.longitude),
                REPORT_FOCUS_ZOOM,
            ),
            REPORT_FOCUS_DURATION_MS,
        )
        viewModel.onFocusHandled()
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

    Column(modifier = modifier.fillMaxSize().background(colors.bg)) {
        Box(
            Modifier
                .weight(1f)
                .onSizeChanged { size ->
                    val previous = mapSizePx
                    mapSizePx = size
                    // A rotation/resize changes what counts as on-screen, so
                    // re-cull immediately instead of waiting for the next
                    // camera move.
                    if (previous != size) map?.let(::refreshProjections)
                },
        ) {
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
                        // MapTiler's outdoor style is intentionally shared by both themes.
                        // A translucent Hima surface makes it comfortable in dark mode
                        // without changing providers, API configuration, or map behavior.
                        if (colors.isDark) {
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .background(colors.bg.copy(alpha = 0.38f)),
                            )
                        }
                        userLocationScreenPos?.let { position ->
                            val x = with(density) { position.x.toDp() } - 20.dp
                            val y = with(density) { position.y.toDp() } - 20.dp
                            CurrentLocationMarker(modifier = Modifier.absoluteOffset(x = x, y = y))
                        }
                        // Drawn before the report pins below so a report
                        // sitting on top of a hotspot still wins the tap.
                        fireMarkerItems.forEach { (hotspot, screenPosition) ->
                            val x = with(density) { screenPosition.x.toDp() } - 14.dp
                            val y = with(density) { screenPosition.y.toDp() } - 14.dp
                            FireHotspotMarker(
                                onClick = { viewModel.onFireHotspotClick(hotspot) },
                                modifier = Modifier.absoluteOffset(x = x, y = y),
                            )
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
                // Irrelevant while the ranger is looking at the NASA layer,
                // which carries no report data at all.
                uiState.filter != MapFilter.NasaFires &&
                    uiState.reportsLoadState is ReportsLoadState.Error -> MapFallbackNotice(
                        titleRes = R.string.map_reports_error_title,
                        bodyRes = R.string.map_reports_error_body,
                        onRetry = viewModel::onRetryReports,
                        modifier = Modifier.align(Alignment.Center),
                    )
                // "Nothing to show" means no hotspots under the NASA layer,
                // and no matching reports under every other selection.
                uiState.mapLoadState == MapLoadState.Ready && when (uiState.filter) {
                    MapFilter.NasaFires ->
                        uiState.fireLoadState == FireLoadState.Ready && uiState.fireHotspots.isEmpty()
                    else ->
                        uiState.reportsLoadState == ReportsLoadState.Ready && uiState.visibleIncidents.isEmpty()
                } -> EmptyFilterNotice(
                    filter = uiState.filter,
                    modifier = Modifier.align(Alignment.Center),
                )
                else -> Unit
            }

            MapOverlayControls(
                filter = uiState.filter,
                onFilterSelected = viewModel::onFilterSelected,
                onMyLocationClick = ::onMyLocationClick,
                // Only worth nagging about when there's nothing already on
                // screen to fall back on — a stale-but-present hotspot list
                // from an earlier successful load says nothing is actually
                // broken right now.
                showFireErrorNotice = uiState.showNasaFires &&
                    uiState.fireLoadState is FireLoadState.Error &&
                    uiState.fireHotspots.isEmpty(),
                onRetryFireErrorClick = viewModel::onRetryFires,
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
        // Browsing the Map dismisses the selected marker normally. A focused
        // route opened by Detail returns to that same report in one press.
        val dismissSelection = {
            if (viewModel.openedForFocusedReport) onBackClick() else viewModel.onDismissSheet()
        }
        BackHandler(onBack = dismissSelection)
        val sheetState = rememberModalBottomSheetState()
        val distanceLabel = uiState.userLocation?.let { userLocation ->
            distanceBearing(userLocation, LatLng(incident.latitude, incident.longitude)).formatLabel()
        }
        ModalBottomSheet(
            onDismissRequest = dismissSelection,
            sheetState = sheetState,
            containerColor = colors.surface,
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

    val fireHotspot = uiState.selectedFireHotspot
    if (fireHotspot != null) {
        BackHandler(onBack = viewModel::onDismissFireSheet)
        val fireSheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = viewModel::onDismissFireSheet,
            sheetState = fireSheetState,
            containerColor = colors.surface,
            shape = RoundedCornerShape(topStart = HimaRadius.sheet, topEnd = HimaRadius.sheet),
            dragHandle = { IncidentSheetHandle() },
        ) {
            FireHotspotSheetContent(fireHotspot)
        }
    }
}

private const val REPORT_FOCUS_ZOOM = 15.0
private const val REPORT_FOCUS_DURATION_MS = 700

/** Enough slack that a marker anchored just past the edge still animates in
 *  smoothly rather than popping once its centre crosses the boundary. */
private const val MARKER_CULL_MARGIN_PX = 96f

/** Whether a projected marker is worth composing. Before the map surface has
 *  been measured nothing is culled — an unknown viewport must not silently
 *  hide every marker. */
private fun Offset.isOnScreen(viewport: IntSize): Boolean {
    if (viewport == IntSize.Zero) return true
    return x >= -MARKER_CULL_MARGIN_PX && x <= viewport.width + MARKER_CULL_MARGIN_PX &&
        y >= -MARKER_CULL_MARGIN_PX && y <= viewport.height + MARKER_CULL_MARGIN_PX
}

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
    filter: MapFilter,
    onFilterSelected: (Int) -> Unit,
    onMyLocationClick: () -> Unit,
    showFireErrorNotice: Boolean,
    onRetryFireErrorClick: () -> Unit,
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
                    .background(colors.surface)
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

        // "All", then one pill per report category, then the NASA layer last —
        // the order MapFilter.rowIndex/fromRowIndex both encode against.
        FilterPillRow(
            options = buildList {
                add(stringResource(R.string.map_filter_all))
                IncidentCategory.entries.forEach { add(stringResource(it.filterLabelRes)) }
                add(stringResource(R.string.map_filter_nasa_fires))
            },
            selectedIndex = filter.rowIndex,
            onSelect = onFilterSelected,
        )

        if (showFireErrorNotice) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.map_fires_error),
                style = HimaTextStyles.m.copy(fontSize = 11.5.sp),
                color = colors.sage,
                modifier = Modifier
                    .shadow(2.dp, RoundedCornerShape(50))
                    .clip(RoundedCornerShape(50))
                    .background(colors.surface)
                    .clickable(onClick = onRetryFireErrorClick)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
    }
}

/** Shown over the map when the active filter matches no markers. */
@Composable
private fun EmptyFilterNotice(filter: MapFilter, modifier: Modifier = Modifier) {
    val colors = LocalHimaColors.current
    Box(
        modifier = modifier
            .shadow(3.dp, RoundedCornerShape(HimaRadius.field))
            .clip(RoundedCornerShape(HimaRadius.field))
            .background(colors.surface)
            .padding(horizontal = 18.dp, vertical = 14.dp),
    ) {
        Text(
            text = stringResource(
                when {
                    filter == MapFilter.NasaFires -> R.string.map_empty_nasa_fires
                    filter == MapFilter.Category(IncidentCategory.WASTE) -> R.string.map_empty_waste
                    else -> R.string.map_empty_filter
                },
            ),
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
            .background(colors.surface)
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
            .background(colors.divider),
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
            ReportImage(
                imageUrl = report.imageUrl,
                demoImageRes = report.demoImageRes,
                scene = report.scene,
                contentDescription = report.titleOverride ?: stringResource(report.titleRes),
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(HimaRadius.thumb)),
            )
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
                if (report.category != IncidentCategory.WASTE && report.severity != Severity.UNKNOWN) {
                    SeverityBadge(report.severity)
                }
                Text(
                    text = stringResource(
                        when (report.status) {
                            ReportStatus.OPEN -> R.string.history_filter_open
                            ReportStatus.RESOLVED -> R.string.history_filter_done
                            ReportStatus.UNKNOWN -> R.string.report_status_unknown
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

/**
 * A NASA FIRMS detection's popup — deliberately carries none of
 * [IncidentSheetContent]'s report chrome (no status, no "View report", no
 * severity badge): there is no report behind this, only a satellite
 * observation, and the copy says so explicitly so it's never mistaken for
 * one (see string map_fire_alert_disclaimer).
 */
@Composable
private fun FireHotspotSheetContent(hotspot: FireHotspot, modifier: Modifier = Modifier) {
    val colors = LocalHimaColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "🔥", style = HimaTextStyles.t.copy(fontSize = 26.sp))
            Text(
                text = stringResource(R.string.map_fire_alert_title),
                style = HimaTextStyles.t.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                color = colors.ink,
                modifier = Modifier.padding(start = 10.dp),
            )
        }
        Text(
            text = stringResource(R.string.map_fire_alert_disclaimer),
            style = HimaTextStyles.m,
            color = colors.sage,
            modifier = Modifier.padding(top = 6.dp),
        )

        FireHotspotDetailRow(stringResource(R.string.map_fire_source_label), stringResource(R.string.map_fire_source_value))
        FireHotspotDetailRow(stringResource(R.string.map_fire_sensor_label), stringResource(R.string.map_fire_sensor_value))
        hotspot.acquiredAt?.let { acquiredAt ->
            FireHotspotDetailRow(stringResource(R.string.map_fire_time_label), relativeTimeLabel(acquiredAt))
        }
        FireHotspotDetailRow(stringResource(R.string.map_fire_location_label), formatFireCoordinates(hotspot))
        hotspot.confidence?.let { confidence ->
            FireHotspotDetailRow(stringResource(R.string.map_fire_confidence_label), confidence)
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun FireHotspotDetailRow(label: String, value: String) {
    val colors = LocalHimaColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = HimaTextStyles.m, color = colors.sage)
        Text(text = value, style = HimaTextStyles.m.copy(fontWeight = FontWeight.Medium), color = colors.ink)
    }
}

private fun formatFireCoordinates(hotspot: FireHotspot): String {
    val latHemisphere = if (hotspot.latitude >= 0) "N" else "S"
    val lngHemisphere = if (hotspot.longitude >= 0) "E" else "W"
    return String.format(
        java.util.Locale.US,
        "%.4f°%s, %.4f°%s",
        kotlin.math.abs(hotspot.latitude),
        latHemisphere,
        kotlin.math.abs(hotspot.longitude),
        lngHemisphere,
    )
}
