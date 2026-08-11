package com.hima.ai.core.common

import android.util.Log
import com.squareup.moshi.Moshi
import java.io.IOException
import retrofit2.Response

/**
 * Logcat tag for every backend/Supabase call the app makes. Filter on this to
 * see exactly which request failed and why:
 *
 *     adb logcat -s HimaApi
 */
const val API_LOG_TAG = "HimaApi"

/**
 * Turns a Retrofit call into an [ApiResult], parsing the error body with
 * [parseErrorMessage] so a failure surfaces the server's own message instead
 * of a bare status code. Network failures (no connectivity, DNS, timeout) and
 * anything else unexpected are caught here so a repository never has to.
 *
 * The user-facing message stays deliberately generic — it must never leak
 * hostnames, ports or stack-trace wording. Everything needed to actually
 * diagnose the failure goes to Logcat under [API_LOG_TAG] instead, tagged with
 * [label] so it's obvious which call broke. That split matters during a live
 * demo: "Couldn't reach the server" looks identical whether the local backend
 * isn't running, the Wi-Fi dropped, Gemini rejected the image, or Supabase
 * refused the insert — the log line tells them apart in seconds.
 */
suspend fun <T> Moshi.safeApiCall(
    parseErrorMessage: (String) -> String?,
    label: String = "api",
    call: suspend () -> Response<T>,
): ApiResult<T> {
    return try {
        val response = call()
        val body = response.body()
        when {
            response.isSuccessful && body != null -> ApiResult.Success(body)
            response.isSuccessful -> {
                Log.e(API_LOG_TAG, "[$label] HTTP ${response.code()} succeeded but carried no body — ${response.raw().request.url}")
                ApiResult.Failure(AppError.Unexpected("The server returned an empty response."))
            }
            else -> ApiResult.Failure(response.toFailure(parseErrorMessage, label))
        }
    } catch (e: IOException) {
        logTransportFailure(label, e)
        ApiResult.Failure(AppError.Network("Couldn't reach the server. Check your connection and try again."))
    } catch (e: Exception) {
        Log.e(API_LOG_TAG, "[$label] unexpected ${e.javaClass.simpleName}: ${e.message}", e)
        ApiResult.Failure(AppError.Unexpected("Something went wrong. Please try again."))
    }
}

/** Same as [safeApiCall], for endpoints whose success response has no body worth keeping. */
suspend fun Moshi.safeApiCallNoBody(
    parseErrorMessage: (String) -> String?,
    label: String = "api",
    call: suspend () -> Response<*>,
): ApiResult<Unit> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            ApiResult.Success(Unit)
        } else {
            ApiResult.Failure(response.toFailure(parseErrorMessage, label))
        }
    } catch (e: IOException) {
        logTransportFailure(label, e)
        ApiResult.Failure(AppError.Network("Couldn't reach the server. Check your connection and try again."))
    } catch (e: Exception) {
        Log.e(API_LOG_TAG, "[$label] unexpected ${e.javaClass.simpleName}: ${e.message}", e)
        ApiResult.Failure(AppError.Unexpected("Something went wrong. Please try again."))
    }
}

/**
 * A request that never reached a server. The exception text carries the
 * host:port that refused the connection, which is what separates "the local
 * backend isn't running" from "this device has no network" — so it is logged
 * verbatim, along with an explicit hint for the case that bites most often.
 */
private fun logTransportFailure(label: String, e: IOException) {
    val detail = e.toString()
    Log.e(API_LOG_TAG, "[$label] could not reach host — ${e.javaClass.simpleName}: $detail", e)
    if (detail.contains("10.0.2.2") || detail.contains("localhost") || detail.contains("127.0.0.1")) {
        Log.e(
            API_LOG_TAG,
            "[$label] that address is the local dev backend. Is it running? " +
                "Start it with: node backend/index.js  (or ./run-demo.sh)",
        )
    }
}

/**
 * An HTTP error the server did answer. The status code, the URL and the raw
 * body all go to the log: the backend returns distinct messages for a Gemini
 * failure, a Storage failure and a Supabase insert failure, so the body is
 * usually enough to name the culprit without touching the server console.
 */
private fun Response<*>.toFailure(parseErrorMessage: (String) -> String?, label: String): AppError {
    val rawBody = errorBody()?.string()
    Log.e(
        API_LOG_TAG,
        "[$label] HTTP ${code()} from ${raw().request.url} — body: ${rawBody ?: "<empty>"}",
    )
    val message = rawBody?.let(parseErrorMessage) ?: "Request failed (HTTP ${code()})"
    return if (code() in 400..499) AppError.Rejected(message) else AppError.Server(message)
}
