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

    /**
     * Every field report for the map — not scoped to the caller's own reports
     * the way the Express `/reports` route is. Row Level Security is what
     * actually decides which rows come back; this client filters out only
     * rows with no usable coordinates, same as the backend's own insert path
     * already guarantees for anything written through `/analyze`.
     */
    @GET("rest/v1/reports")
    suspend fun getReports(
        @Header("Authorization") bearerToken: String,
        @Query("select") select: String = REPORT_SELECT,
        @Query("latitude") latitudeNotNull: String = "not.is.null",
        @Query("longitude") longitudeNotNull: String = "not.is.null",
        @Query("order") order: String = "created_at.desc",
    ): Response<List<ReportDto>>
}

private const val REPORT_SELECT =
    "id,type,severity,status,latitude,longitude,description,recommended_action,ai_analysis,confidence,created_at"

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

/** Matches the live `reports` table exactly — see the columns actually
 *  returned by `/rest/v1/reports`, not the (different, narrower) shape the
 *  Express backend's own `/reports` route returns. */
@JsonClass(generateAdapter = true)
data class ReportDto(
    val id: String,
    val type: String?,
    val severity: String,
    val status: String,
    val latitude: Double,
    val longitude: Double,
    val description: String? = null,
    @Json(name = "recommended_action") val recommendedAction: String? = null,
    @Json(name = "ai_analysis") val aiAnalysis: AiAnalysisDto? = null,
    val confidence: Int? = null,
    @Json(name = "created_at") val createdAt: String? = null,
)

@JsonClass(generateAdapter = true)
data class AiAnalysisDto(
    @Json(name = "risk_score") val riskScore: Int? = null,
)
