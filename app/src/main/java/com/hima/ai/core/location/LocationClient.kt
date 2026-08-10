package com.hima.ai.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

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
