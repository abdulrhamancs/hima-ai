package com.hima.ai.core.common

/**
 * What can go wrong calling Supabase or our backend, distinguished by what the
 * UI should actually say and do — not by which library threw. A screen can
 * show [message] as-is (both come from the server in the ranger's language, or
 * are hand-written here) and decide whether "retry" makes sense from the type.
 */
sealed interface AppError {
    val message: String

    /** No connectivity, DNS failure, or the request timed out. Retrying makes sense. */
    data class Network(override val message: String) : AppError

    /** The server rejected the request as invalid (bad credentials, bad image, validation). */
    data class Rejected(override val message: String) : AppError

    /** The request reached the server but it failed unexpectedly (5xx). */
    data class Server(override val message: String) : AppError

    /** A response came back but didn't parse into what was expected. */
    data class Unexpected(override val message: String) : AppError
}

/** A fallible network operation's outcome — never throws past a repository boundary. */
sealed interface ApiResult<out T> {
    data class Success<T>(val value: T) : ApiResult<T>
    data class Failure(val error: AppError) : ApiResult<Nothing>
}

inline fun <T, R> ApiResult<T>.map(transform: (T) -> R): ApiResult<R> = when (this) {
    is ApiResult.Success -> ApiResult.Success(transform(value))
    is ApiResult.Failure -> this
}
