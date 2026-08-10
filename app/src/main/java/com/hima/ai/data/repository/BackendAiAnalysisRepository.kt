package com.hima.ai.data.repository

import android.content.Context
import android.net.Uri
import com.hima.ai.core.common.ApiResult
import com.hima.ai.core.common.AppError
import com.hima.ai.core.common.safeApiCall
import com.hima.ai.data.remote.backend.AiResultDto
import com.hima.ai.data.remote.backend.AnalyzeResponseDto
import com.hima.ai.data.remote.backend.HimaBackendApi
import com.hima.ai.domain.model.AiAnalysis
import com.hima.ai.domain.model.AnalysisResultCategory
import com.hima.ai.domain.model.Severity
import com.hima.ai.domain.repository.AiAnalysisRepository
import com.hima.ai.domain.repository.AuthRepository
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

@Singleton
class BackendAiAnalysisRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: HimaBackendApi,
    private val authRepository: AuthRepository,
    private val moshi: Moshi,
) : AiAnalysisRepository {

    override suspend fun analyzeImage(imageUri: Uri, description: String?): ApiResult<AiAnalysis> {
        val token = authRepository.currentSession.value?.accessToken
            ?: return ApiResult.Failure(AppError.Rejected("Sign in before analysing a report."))

        val imagePart = withContext(Dispatchers.IO) {
            runCatching { imageUri.toMultipartPart(context) }
        }.getOrElse { e ->
            return ApiResult.Failure(AppError.Unexpected(e.message ?: "Couldn't read the photo."))
        }
        val descriptionPart = description
            ?.takeIf { it.isNotBlank() }
            ?.toRequestBody("text/plain".toMediaTypeOrNull())

        val result = moshi.safeApiCall(::parseAnalyzeError) {
            api.analyzeImage("Bearer $token", imagePart, descriptionPart)
        }
        return when (result) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> result.value.toDomainOrFailure()
        }
    }

    private fun AnalyzeResponseDto.toDomainOrFailure(): ApiResult<AiAnalysis> {
        if (status != "success") {
            return ApiResult.Failure(AppError.Rejected(error ?: "The analysis failed."))
        }
        val result = aiResult
            ?: return ApiResult.Failure(AppError.Unexpected("The analysis response was missing expected fields."))

        return when (resultCategory) {
            "recyclable_waste" -> result.toRecyclableWasteOrFailure()
            else -> result.toEnvironmentalIncidentOrFailure()
        }
    }

    private fun AiResultDto.toEnvironmentalIncidentOrFailure(): ApiResult<AiAnalysis> {
        val severity = riskLevel?.let(::parseSeverity)
        if (issueType == null || description == null || riskScore == null || severity == null ||
            confidence == null || recommendation == null
        ) {
            return ApiResult.Failure(AppError.Unexpected("The analysis response was missing expected fields."))
        }
        return ApiResult.Success(
            AiAnalysis(
                category = AnalysisResultCategory.ENVIRONMENTAL_INCIDENT,
                description = description,
                confidence = confidence.roundToInt(),
                recommendation = recommendation,
                issueType = issueType,
                riskScore = riskScore.roundToInt(),
                riskLevel = severity,
            ),
        )
    }

    private fun AiResultDto.toRecyclableWasteOrFailure(): ApiResult<AiAnalysis> {
        if (description == null || confidence == null || recommendation == null ||
            materialCategory == null || disposalClassification == null
        ) {
            return ApiResult.Failure(AppError.Unexpected("The analysis response was missing expected fields."))
        }
        return ApiResult.Success(
            AiAnalysis(
                category = AnalysisResultCategory.RECYCLABLE_WASTE,
                description = description,
                confidence = confidence.roundToInt(),
                recommendation = recommendation,
                materialCategory = materialCategory,
                disposalClassification = disposalClassification,
                reuseSuggestion = reuseSuggestion,
            ),
        )
    }

    private fun parseSeverity(riskLevel: String): Severity? = when (riskLevel.lowercase()) {
        "low" -> Severity.LOW
        "medium" -> Severity.MEDIUM
        "high" -> Severity.HIGH
        "critical" -> Severity.CRITICAL
        else -> null
    }

    private fun parseAnalyzeError(raw: String): String? =
        runCatching { moshi.adapter(AnalyzeResponseDto::class.java).fromJson(raw)?.error }.getOrNull()
}

/** Reads the picked/captured photo into a multipart part without ever materialising a temp file. */
private fun Uri.toMultipartPart(context: Context): MultipartBody.Part {
    val resolver = context.contentResolver
    val mimeType = resolver.getType(this) ?: "image/jpeg"
    val bytes = resolver.openInputStream(this)?.use { it.readBytes() }
        ?: error("Couldn't open the photo.")
    val body: RequestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
    return MultipartBody.Part.createFormData("image", "evidence.jpg", body)
}
