package com.hima.ai.data.remote.backend

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * Our Express backend. The only route the app calls is `/analyze` — it is
 * where Gemini lives, so the Gemini key never has to exist on-device.
 */
interface HimaBackendApi {
    @Multipart
    @POST("analyze")
    suspend fun analyzeImage(
        @Header("Authorization") bearerToken: String,
        @Part image: MultipartBody.Part,
        @Part("description") description: RequestBody?,
    ): Response<AnalyzeResponseDto>
}

/**
 * The backend's `/analyze` response, exactly as it defines it — see
 * backend/config/gemini.js's response schema and backend/index.js's handler.
 * `description` here is currently accepted but not read by that handler (its
 * Gemini prompt is fixed server-side), so sending it has no effect yet; it is
 * still sent so nothing needs to change here once the backend does read it.
 */
@JsonClass(generateAdapter = true)
data class AnalyzeResponseDto(
    val status: String? = null,
    @Json(name = "issue_type") val issueType: String? = null,
    val description: String? = null,
    @Json(name = "risk_score") val riskScore: Double? = null,
    @Json(name = "risk_level") val riskLevel: String? = null,
    val confidence: Double? = null,
    val recommendation: String? = null,
    val error: String? = null,
)
