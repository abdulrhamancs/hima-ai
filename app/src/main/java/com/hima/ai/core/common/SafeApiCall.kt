package com.hima.ai.core.common

import com.squareup.moshi.Moshi
import java.io.IOException
import retrofit2.Response

/**
 * Turns a Retrofit call into an [ApiResult], parsing the error body with
 * [parseErrorMessage] so a failure surfaces the server's own message instead
 * of a bare status code. Network failures (no connectivity, DNS, timeout) and
 * anything else unexpected are caught here so a repository never has to.
 */
suspend fun <T> Moshi.safeApiCall(
    parseErrorMessage: (String) -> String?,
    call: suspend () -> Response<T>,
): ApiResult<T> {
    return try {
        val response = call()
        val body = response.body()
        when {
            response.isSuccessful && body != null -> ApiResult.Success(body)
            response.isSuccessful -> ApiResult.Failure(AppError.Unexpected("The server returned an empty response."))
            else -> ApiResult.Failure(response.toFailure(parseErrorMessage))
        }
    } catch (e: IOException) {
        // Never surface the raw exception text (hostnames, stack-trace-ish
        // wording) to a user; only the log gets that.
        ApiResult.Failure(AppError.Network("Couldn't reach the server. Check your connection and try again."))
    } catch (e: Exception) {
        ApiResult.Failure(AppError.Unexpected("Something went wrong. Please try again."))
    }
}

/** Same as [safeApiCall], for endpoints whose success response has no body worth keeping. */
suspend fun Moshi.safeApiCallNoBody(
    parseErrorMessage: (String) -> String?,
    call: suspend () -> Response<*>,
): ApiResult<Unit> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            ApiResult.Success(Unit)
        } else {
            ApiResult.Failure(response.toFailure(parseErrorMessage))
        }
    } catch (e: IOException) {
        // Never surface the raw exception text (hostnames, stack-trace-ish
        // wording) to a user; only the log gets that.
        ApiResult.Failure(AppError.Network("Couldn't reach the server. Check your connection and try again."))
    } catch (e: Exception) {
        ApiResult.Failure(AppError.Unexpected("Something went wrong. Please try again."))
    }
}

private fun Response<*>.toFailure(parseErrorMessage: (String) -> String?): AppError {
    val raw = errorBody()?.string()
    val message = raw?.let(parseErrorMessage) ?: "Request failed (HTTP ${code()})"
    return if (code() in 400..499) AppError.Rejected(message) else AppError.Server(message)
}
