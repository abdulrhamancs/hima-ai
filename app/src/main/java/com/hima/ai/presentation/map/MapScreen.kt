package com.hima.ai.presentation.map

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hima.ai.R
import com.hima.ai.core.designsystem.component.CurrentLocationMarker
import com.hima.ai.core.designsystem.component.FilterPillRow
import com.hima.ai.core.designsystem.component.HimaBottomNavigation
import com.hima.ai.core.designsystem.component.HimaIconButton
import com.hima.ai.core.designsystem.component.HimaPrimaryButton
import com.hima.ai.core.designsystem.component.HimaTab
import com.hima.ai.core.designsystem.component.MapClusterMarker
import com.hima.ai.core.designsystem.component.MapMarkerPin
import com.hima.ai.core.designsystem.component.SceneArt
import com.hima.ai.core.designsystem.component.SeverityBadge
import com.hima.ai.core.designsystem.theme.HimaEasing
import com.hima.ai.core.designsystem.theme.HimaMotionDuration
import com.hima.ai.core.designsystem.theme.HimaRadius
import com.hima.ai.core.designsystem.theme.HimaTextStyles
import com.hima.ai.core.designsystem.theme.LocalHimaColors
import com.hima.ai.domain.model.IncidentCategory
import com.hima.ai.domain.model.MapIncident
import com.hima.ai.domain.model.ReportStatus
import kotlinx.coroutines.launch

/** Eased glide shared by camera recenter/zoom — a smooth pan, not a linear one. */
private fun <T> cameraGlideSpec() = tween<T>(
    durationMillis = HimaMotionDuration.ScreenPush,
    easing = HimaEasing,
)

/**
 * The reserve map — pan/zoom terrain, incident markers, and minimal floating
 * controls, matching the same header/button/pill language used everywhere
 * else in the app rather than a generic maps UI.
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
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }
    val translation = remember { Animatable(Offset.Zero, Offset.VectorConverter) }

    // A no-overshoot spring reads as a physical camera glide rather than a
    // mechanical linear pan — the same settle feel iOS Maps uses.
    fun recenter() {
        scope.launch { scale.animateTo(1f, cameraGlideSpec()) }
        scope.launch { translation.animateTo(Offset.Zero, cameraGlideSpec()) }
    }

    // Centres a fraction-space point on screen at a target zoom level. Compose's
    // graphicsLayer scales around the layer's own centre, so the translation
    // needed to bring a point to that centre is (centre - point) * targetScale.
    fun zoomToPoint(fx: Float, fy: Float, boxWidthPx: Float, boxHeightPx: Float) {
        val targetScale = (scale.value * 1.8f).coerceIn(1f, 3f)
        val target = Offset(
            x = boxWidthPx * (0.5f - fx) * targetScale,
            y = boxHeightPx * (0.5f - fy) * targetScale,
        )
        scope.launch { scale.animateTo(targetScale, cameraGlideSpec()) }
        scope.launch { translation.animateTo(target, cameraGlideSpec()) }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Box(Modifier.weight(1f)) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scope.launch {
                                scale.snapTo((scale.value * zoom).coerceIn(0.8f, 3f))
                                translation.snapTo(translation.value + pan)
                            }
                        }
                    },
            ) {
                val boxWidth = maxWidth
                val boxHeight = maxHeight
                val boxWidthPx = with(density) { boxWidth.toPx() }
                val boxHeightPx = with(density) { boxHeight.toPx() }
                // The map is geography, not text: its coordinate space must not
                // mirror with the UI language. ReserveTerrain is a Canvas drawn
                // in absolute coordinates, so pinning this subtree to LTR keeps
                // marker offsets measured from the same origin the terrain uses.
                // Without it, Arabic put markers on the wrong side of the map
                // (plain offset) or off-screen entirely (absoluteOffset).
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale.value
                            scaleY = scale.value
                            translationX = translation.value.x
                            translationY = translation.value.y
                        },
                ) {
                    ReserveTerrain(Modifier.fillMaxSize())

                    CurrentLocationMarker(
                        modifier = Modifier.absoluteOffset(
                            x = boxWidth * 0.5f - 20.dp,
                            y = boxHeight * 0.5f - 20.dp,
                        ),
                    )

                    // Clustering is O(n²); reading scale.value directly re-ran it
                    // on every frame of a pinch or pan. Quantising the zoom means
                    // it only re-runs when the grouping could actually change.
                    val visibleIncidents = uiState.visibleIncidents
                    val zoomStep = (scale.value * 4f).toInt()
                    val markerItems = remember(visibleIncidents, zoomStep) {
                        clusterIncidents(visibleIncidents, zoomStep / 4f)
                    }
                    markerItems.forEach { item ->
                        when (item) {
                            is MapMarkerItem.Single -> {
                                val incident = item.incident
                                MapMarkerPin(
                                    category = incident.category,
                                    severity = incident.report.severity,
                                    selected = uiState.selectedIncident?.report?.id == incident.report.id,
                                    onClick = { viewModel.onMarkerClick(incident) },
                                    modifier = Modifier.absoluteOffset(
                                        x = boxWidth * incident.xFraction - 24.dp,
                                        y = boxHeight * incident.yFraction - 24.dp,
                                    ),
                                )
                            }
                            is MapMarkerItem.Cluster -> {
                                val topSeverity = item.incidents.maxBy { it.report.severity.ordinal }.report.severity
                                MapClusterMarker(
                                    count = item.incidents.size,
                                    severity = topSeverity,
                                    onClick = { zoomToPoint(item.xFraction, item.yFraction, boxWidthPx, boxHeightPx) },
                                    modifier = Modifier.absoluteOffset(
                                        x = boxWidth * item.xFraction - 22.dp,
                                        y = boxHeight * item.yFraction - 22.dp,
                                    ),
                                )
                            }
                        }
                    }
                }
                }
            }

            MapOverlayControls(
                filter = uiState.filter,
                onFilterSelected = viewModel::onFilterSelected,
                onMyLocationClick = ::recenter,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .padding(top = 50.dp, start = 16.dp, end = 16.dp),
            )

            if (uiState.visibleIncidents.isEmpty()) {
                EmptyFilterNotice(modifier = Modifier.align(Alignment.Center))
            }
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
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = viewModel::onDismissSheet,
            sheetState = sheetState,
            containerColor = colors.bg,
            shape = RoundedCornerShape(topStart = HimaRadius.sheet, topEnd = HimaRadius.sheet),
            dragHandle = { IncidentSheetHandle() },
        ) {
            IncidentSheetContent(
                incident = incident,
                onViewReportClick = {
                    viewModel.onDismissSheet()
                    onViewReportClick(incident.report.id)
                },
            )
        }
    }
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

/** Shown over the terrain when the active filter matches no markers. */
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
                    text = stringResource(report.titleRes),
                    style = HimaTextStyles.t.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                    color = colors.ink,
                )
                Text(
                    text = "${stringResource(report.locationRes)} · ${stringResource(report.timeRes)}",
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
