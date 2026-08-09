package com.hima.ai.data.remote.supabase

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Supabase's GoTrue REST API, called directly — its anon key is meant to sit
 * in a client, unlike the Gemini key. The `apikey` header is attached by an
 * interceptor (see NetworkModule) rather than repeated on every call here.
 */
interface SupabaseAuthApi {
    @POST("auth/v1/signup")
    suspend fun signUp(@Body body: SupabaseCredentialsRequest): Response<SupabaseSessionResponse>

    @POST("auth/v1/token")
    suspend fun login(
        @Query("grant_type") grantType: String = "password",
        @Body body: SupabaseCredentialsRequest,
    ): Response<SupabaseSessionResponse>

    // Response<Void>, not Response<Unit> — a 204 has no body, and Moshi has no
    // adapter for Unit; Void is Retrofit's own no-body idiom and needs neither.
    @POST("auth/v1/logout")
    suspend fun logout(@Header("Authorization") bearerToken: String): Response<Void>
}

@JsonClass(generateAdapter = true)
data class SupabaseCredentialsRequest(
    val email: String,
    val password: String,
)

/**
 * Covers both shapes GoTrue can return from /signup: a full session when the
 * project auto-confirms new accounts (fields at top level, as our backend's
 * own signup handler already assumes), or — if email confirmation is
 * required — just the created user with no session fields.
 */
@JsonClass(generateAdapter = true)
data class SupabaseSessionResponse(
    @Json(name = "access_token") val accessToken: String? = null,
    @Json(name = "refresh_token") val refreshToken: String? = null,
    val user: SupabaseUserDto? = null,
    val id: String? = null,
    val email: String? = null,
)

@JsonClass(generateAdapter = true)
data class SupabaseUserDto(
    val id: String,
    val email: String? = null,
)

/** GoTrue's error responses aren't one fixed shape across versions; try each field in turn. */
@JsonClass(generateAdapter = true)
data class SupabaseErrorResponse(
    val msg: String? = null,
    val message: String? = null,
    @Json(name = "error_description") val errorDescription: String? = null,
    @Json(name = "error_code") val errorCode: String? = null,
) {
    fun readableMessage(): String? = msg ?: message ?: errorDescription ?: errorCode
}
