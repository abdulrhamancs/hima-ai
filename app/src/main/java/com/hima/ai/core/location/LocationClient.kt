package com.hima.ai.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.io.IOException
import java.util.Locale
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

/** Approximate location is sufficient for attaching a report to the map. */
fun Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED

/**
 * A single current-location fix — not a continuous stream. The map's "My
 * Location" is a deliberate, on-demand recentre rather than a live-follow
 * mode, so there's no [com.google.android.gms.location.LocationCallback] or
 * ongoing subscription to manage here, just one request per tap.
 *
 * Callers are expected to have already confirmed a location permission is
 * granted; this makes no permission check of its own.
 */
@SuppressLint("MissingPermission")
suspend fun FusedLocationProviderClient.awaitCurrentLocation(): Location? =
    suspendCancellableCoroutine { continuation ->
        val cancellationSource = CancellationTokenSource()
        continuation.invokeOnCancellation { cancellationSource.cancel() }

        fun resumeWithLastKnownLocation() {
            lastLocation
                .addOnSuccessListener { location ->
                    if (continuation.isActive) continuation.resume(location)
                }
                .addOnFailureListener {
                    if (continuation.isActive) continuation.resume(null)
                }
        }

        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
            .setMaxUpdateAgeMillis(MAX_LOCATION_AGE_MILLIS)
            .setDurationMillis(LOCATION_REQUEST_TIMEOUT_MILLIS)
            .build()

        getCurrentLocation(request, cancellationSource.token)
            .addOnSuccessListener { location ->
                if (location != null) {
                    if (continuation.isActive) continuation.resume(location)
                } else {
                    resumeWithLastKnownLocation()
                }
            }
            .addOnFailureListener { resumeWithLastKnownLocation() }
    }

private const val MAX_LOCATION_AGE_MILLIS = 60_000L
private const val LOCATION_REQUEST_TIMEOUT_MILLIS = 5_000L

/**
 * City/region-level place name for a coordinate, in [languageTag] (e.g. "ar"
 * or "en" — see [com.hima.ai.core.util.currentAppLanguage]), or null if it
 * can't be resolved (no geocoder service on device, no network, or no match).
 * Never throws — callers are expected to fall back to a neutral label.
 */
suspend fun Context.reverseGeocodeLocality(latitude: Double, longitude: Double, languageTag: String): String? {
    if (!Geocoder.isPresent()) return null
    val geocoder = Geocoder(this, Locale.forLanguageTag(languageTag))
    return try {
        val address = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { continuation ->
                geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                    if (continuation.isActive) continuation.resume(addresses.firstOrNull())
                }
            }
        } else {
            @Suppress("DEPRECATION")
            withContext(Dispatchers.IO) { geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull() }
        }
        address?.locality ?: address?.subAdminArea ?: address?.adminArea
    } catch (_: IOException) {
        null
    }
}
