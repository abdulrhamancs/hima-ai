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
        // Accepted by the handler and folded into the Gemini prompt as extra
        // context; it does not change which fields come back.
        @Part("description") description: RequestBody?,
        @Part("language") language: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
    ): Response<AnalyzeResponseDto>
}

/**
 * The backend's `/analyze` response, exactly as it defines it — see
 * backend/config/gemini.js's response schema and backend/index.js's handler.
 * `ai_result` carries the fields specific to whichever [resultCategory] the
 * backend decided on — see [AiResultDto].
 */
@JsonClass(generateAdapter = true)
data class AnalyzeResponseDto(
    val status: String? = null,
    @Json(name = "result_category") val resultCategory: String? = null,
    @Json(name = "ai_result") val aiResult: AiResultDto? = null,
    @Json(name = "report_id") val reportId: String? = null,
    @Json(name = "image_url") val imageUrl: String? = null,
    val error: String? = null,
)

@JsonClass(generateAdapter = true)
data class AiResultDto(
    val description: String? = null,
    val confidence: Double? = null,
    val recommendation: String? = null,
    @Json(name = "environmental_impact") val environmentalImpact: String? = null,
    @Json(name = "ai_explanation") val aiExplanation: String? = null,
    // Environmental incident:
    @Json(name = "issue_type") val issueType: String? = null,
    @Json(name = "risk_score") val riskScore: Double? = null,
    @Json(name = "risk_level") val riskLevel: String? = null,
    // Recyclable waste:
    @Json(name = "material_category") val materialCategory: String? = null,
    @Json(name = "waste_type") val wasteType: String? = null,
    @Json(name = "disposal_classification") val disposalClassification: String? = null,
    val recyclable: Boolean? = null,
    val reusable: Boolean? = null,
    val repairable: Boolean? = null,
    @Json(name = "preferred_action") val preferredAction: String? = null,
    @Json(name = "reuse_suggestion") val reuseSuggestion: String? = null,
    @Json(name = "repair_guidance") val repairGuidance: String? = null,
    @Json(name = "recycling_guidance") val recyclingGuidance: String? = null,
    @Json(name = "disposal_guidance") val disposalGuidance: String? = null,
)
