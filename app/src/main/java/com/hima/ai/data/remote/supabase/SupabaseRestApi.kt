package com.hima.ai.data.remote.supabase

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * PostgREST access to the `profiles` table, matching exactly what our
 * backend's own signup/login handlers do — insert a profile row right after
 * signup, read it back on login. Row Level Security decides whether these are
 * actually allowed for a given user's token; this client makes no assumption
 * beyond what the backend's own implementation already relies on.
 */
interface SupabaseRestApi {
    // Response<Void>, not Response<Unit> — "Prefer: return=minimal" means an
    // empty body, and Moshi has no adapter for Unit; Void needs neither.
    @Headers("Prefer: return=minimal")
    @POST("rest/v1/profiles")
    suspend fun insertProfile(
        @Header("Authorization") bearerToken: String,
        @Body body: ProfileInsertRequest,
    ): Response<Void>

    @GET("rest/v1/profiles")
    suspend fun getProfile(
        @Header("Authorization") bearerToken: String,
        @Query("id") idFilter: String,
        @Query("select") select: String = "full_name,role",
    ): Response<List<ProfileDto>>
}

@JsonClass(generateAdapter = true)
data class ProfileInsertRequest(
    val id: String,
    @Json(name = "full_name") val fullName: String,
    val role: String,
)

@JsonClass(generateAdapter = true)
data class ProfileDto(
    @Json(name = "full_name") val fullName: String,
    val role: String,
)

@JsonClass(generateAdapter = true)
data class PostgrestErrorResponse(
    val message: String? = null,
    val details: String? = null,
    val hint: String? = null,
)
