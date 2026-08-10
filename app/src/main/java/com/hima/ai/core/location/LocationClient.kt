package com.hima.ai.core.location

import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

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
        getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancellationSource.token)
            .addOnSuccessListener { location -> continuation.resume(location) }
            .addOnFailureListener { continuation.resume(null) }
    }
