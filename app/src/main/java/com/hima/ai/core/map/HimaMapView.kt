package com.hima.ai.core.map

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

/**
 * The raw MapLibre rendering surface — a lifecycle-correct AndroidView host
 * plus MapTiler style loading, and nothing else. It knows nothing about
 * Hima's markers, filters, or bottom sheet; those stay in the map feature
 * and read the [MapLibreMap] this hands back via [onMapReady].
 *
 * [MapView] is a classic Android View wired to a GL surface, so unlike a
 * plain Composable it needs its Android lifecycle callbacks forwarded by
 * hand, and needs [MapView.onDestroy] called when this leaves composition —
 * navigating away from the Map screen doesn't destroy the host Activity, so
 * the Activity lifecycle alone would leak the GL context across visits.
 */
@Composable
fun HimaMapView(
    modifier: Modifier = Modifier,
    onMapReady: (MapLibreMap) -> Unit = {},
    onLoadStateChanged: (MapLoadState) -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnMapReady by rememberUpdatedState(onMapReady)
    val currentOnLoadStateChanged by rememberUpdatedState(onLoadStateChanged)

    val mapView = remember {
        MapLibre.getInstance(context)
        MapView(context).apply {
            // MapLibre's projection speaks raw LTR screen pixels, and the
            // marker overlay on top of it is positioned in that same space.
            // Compose's AndroidView already forwards an LTR LocalLayoutDirection
            // here today, so this is belt-and-braces — but it pins the
            // invariant to the map surface itself rather than leaving it
            // dependent on how the caller happens to nest its providers.
            layoutDirection = View.LAYOUT_DIRECTION_LTR
        }
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(null)
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    // One-shot: getMapAsync/setStyle/the failure listener must only be wired
    // once per MapView, not on every recomposition — re-running this on an
    // unrelated recomposition would re-request the style and could double up
    // the failure listener.
    LaunchedEffect(mapView) {
        currentOnLoadStateChanged(MapLoadState.Loading)
        mapView.addOnDidFailLoadingMapListener { error ->
            currentOnLoadStateChanged(MapLoadState.StyleError(error))
        }
        mapView.getMapAsync { map ->
            map.setStyle(MapConfig.styleUrl) {
                currentOnLoadStateChanged(MapLoadState.Ready)
                currentOnMapReady(map)
            }
        }
    }

    AndroidView(modifier = modifier, factory = { mapView })
}
