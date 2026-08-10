package com.hima.ai.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.android.gms.location.LocationServices
import com.hima.ai.R
import com.hima.ai.core.common.ApiResult
import com.hima.ai.core.common.AppError
import com.hima.ai.core.common.safeApiCall
import com.hima.ai.data.remote.backend.AiResultDto
import com.hima.ai.data.remote.backend.AnalyzeResponseDto
import com.hima.ai.data.remote.backend.HimaBackendApi
import com.hima.ai.domain.model.AiAnalysis
import com.hima.ai.domain.model.AnalysisResultCategory
import com.hima.ai.domain.model.CircularAction
import com.hima.ai.domain.model.Severity
import com.hima.ai.domain.repository.AiAnalysisRepository
import com.hima.ai.domain.repository.AuthRepository
import com.hima.ai.domain.repository.ReportsRepository
import com.hima.ai.core.util.currentAppLanguage
import com.hima.ai.core.location.awaitCurrentLocation
import com.hima.ai.core.location.hasLocationPermission
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import java.io.ByteArrayOutputStream
import java.io.InputStream
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
    private val reportsRepository: ReportsRepository,
    private val moshi: Moshi,
) : AiAnalysisRepository {

    override suspend fun analyzeImage(imageUri: Uri, description: String?): ApiResult<AiAnalysis> {
        val token = authRepository.currentSession.value?.accessToken
            ?: return ApiResult.Failure(AppError.Rejected("Sign in before analysing a report."))

        if (!context.hasLocationPermission()) {
            return ApiResult.Failure(AppError.Rejected(context.getString(R.string.analysis_location_required)))
        }
        val location = LocationServices.getFusedLocationProviderClient(context).awaitCurrentLocation()
            ?: return ApiResult.Failure(AppError.Rejected(context.getString(R.string.analysis_location_unavailable)))

        val imagePart = withContext(Dispatchers.IO) {
            runCatching { imageUri.toMultipartPart(context) }
        }.getOrElse { e ->
            return ApiResult.Failure(AppError.Unexpected(e.message ?: "Couldn't read the photo."))
        }
        val descriptionPart = description
            ?.takeIf { it.isNotBlank() }
            ?.toRequestBody("text/plain".toMediaTypeOrNull())
        val languagePart = currentAppLanguage().tag
            .toRequestBody("text/plain".toMediaTypeOrNull())
        val latitudePart = location.latitude.toString()
            .toRequestBody("text/plain".toMediaTypeOrNull())
        val longitudePart = location.longitude.toString()
            .toRequestBody("text/plain".toMediaTypeOrNull())

        val result = moshi.safeApiCall(::parseAnalyzeError) {
            api.analyzeImage(
                "Bearer $token",
                imagePart,
                descriptionPart,
                languagePart,
                latitudePart,
                longitudePart,
            )
        }
        val mapped = when (result) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> result.value.toDomainOrFailure()
        }
        if (mapped is ApiResult.Success) {
            // /analyze has already persisted the report at this point. One
            // forced refresh invalidates the shared Home/History/Map source so
            // every existing ViewModel observes the same new report instantly.
            reportsRepository.refresh(force = true)
        }
        return mapped
    }

    private fun AnalyzeResponseDto.toDomainOrFailure(): ApiResult<AiAnalysis> {
        if (status != "success") {
            return ApiResult.Failure(AppError.Rejected(error ?: "The analysis failed."))
        }
        val result = aiResult
            ?: return ApiResult.Failure(AppError.Unexpected("The analysis response was missing expected fields."))
        if (reportId.isNullOrBlank()) {
            return ApiResult.Failure(AppError.Unexpected("The saved report response was missing its report id."))
        }

        return when (resultCategory) {
            "recyclable_waste" -> result.toRecyclableWasteOrFailure(reportId, imageUrl)
            "environmental_incident" -> result.toEnvironmentalIncidentOrFailure(reportId, imageUrl)
            else -> ApiResult.Failure(AppError.Unexpected("The analysis returned an unsupported result category."))
        }
    }

    private fun AiResultDto.toEnvironmentalIncidentOrFailure(
        reportId: String?,
        imageUrl: String?,
    ): ApiResult<AiAnalysis> {
        val severity = riskLevel?.let(::parseSeverity)
        if (issueType.isNullOrBlank() || description.isNullOrBlank() || riskScore == null || severity == null ||
            confidence == null || recommendation.isNullOrBlank() ||
            riskScore !in 0.0..100.0 || confidence !in 0.0..100.0
        ) {
            return ApiResult.Failure(AppError.Unexpected("The analysis response was missing expected fields."))
        }
        return ApiResult.Success(
            AiAnalysis(
                category = AnalysisResultCategory.ENVIRONMENTAL_INCIDENT,
                description = description,
                confidence = confidence.roundToInt(),
                recommendation = recommendation,
                reportId = reportId,
                imageUrl = imageUrl,
                environmentalImpact = environmentalImpact,
                aiExplanation = aiExplanation,
                issueType = issueType,
                riskScore = riskScore.roundToInt(),
                riskLevel = severity,
            ),
        )
    }

    private fun AiResultDto.toRecyclableWasteOrFailure(
        reportId: String?,
        imageUrl: String?,
    ): ApiResult<AiAnalysis> {
        val circularAction = preferredAction?.let(::parseCircularAction)
        if (description.isNullOrBlank() || confidence == null || recommendation.isNullOrBlank() ||
            materialCategory.isNullOrBlank() || disposalClassification.isNullOrBlank() ||
            confidence !in 0.0..100.0 ||
            (!preferredAction.isNullOrBlank() && circularAction == null)
        ) {
            return ApiResult.Failure(AppError.Unexpected("The analysis response was missing expected fields."))
        }
        return ApiResult.Success(
            AiAnalysis(
                category = AnalysisResultCategory.RECYCLABLE_WASTE,
                description = description,
                confidence = confidence.roundToInt(),
                recommendation = recommendation,
                reportId = reportId,
                imageUrl = imageUrl,
                environmentalImpact = environmentalImpact,
                aiExplanation = aiExplanation,
                materialCategory = materialCategory,
                wasteType = wasteType,
                disposalClassification = disposalClassification,
                recyclable = recyclable,
                reusable = reusable,
                repairable = repairable,
                preferredAction = circularAction,
                reuseSuggestion = reuseSuggestion,
                repairGuidance = repairGuidance,
                recyclingGuidance = recyclingGuidance,
                disposalGuidance = disposalGuidance,
            ),
        )
    }

    private fun parseCircularAction(value: String): CircularAction? = when (value.lowercase()) {
        "reuse" -> CircularAction.REUSE
        "repair_refurbish" -> CircularAction.REPAIR_REFURBISH
        "donate_repurpose" -> CircularAction.DONATE_REPURPOSE
        "recycle" -> CircularAction.RECYCLE
        "material_recovery" -> CircularAction.MATERIAL_RECOVERY
        "safe_disposal" -> CircularAction.SAFE_DISPOSAL
        else -> null
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

private const val MAX_DIRECT_UPLOAD_BYTES = 8 * 1024 * 1024
private const val MAX_UPLOAD_DIMENSION = 2048

/**
 * Keeps normal CameraX/Photo Picker images byte-for-byte, but bounds memory and
 * upload size for unusually large gallery files before creating the request.
 */
private fun Uri.toMultipartPart(context: Context): MultipartBody.Part {
    val resolver = context.contentResolver
    val mimeType = resolver.getType(this) ?: "image/jpeg"
    require(mimeType.startsWith("image/")) { "The selected file is not an image." }

    val directBytes = resolver.openInputStream(this)?.use {
        it.readAtMost(MAX_DIRECT_UPLOAD_BYTES + 1)
    }
        ?: error("Couldn't open the photo.")

    val (bytes, uploadMimeType) = if (directBytes.size <= MAX_DIRECT_UPLOAD_BYTES) {
        directBytes to mimeType
    } else {
        resizeForUpload(context) to "image/jpeg"
    }
    val extension = if (uploadMimeType == "image/png") "png" else "jpg"
    val body: RequestBody = bytes.toRequestBody(uploadMimeType.toMediaTypeOrNull())
    return MultipartBody.Part.createFormData("image", "evidence.$extension", body)
}

/** API-26-safe bounded read; InputStream.readNBytes is unavailable on older devices. */
private fun InputStream.readAtMost(maxBytes: Int): ByteArray {
    val output = ByteArrayOutputStream(minOf(maxBytes, DEFAULT_BUFFER_SIZE))
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var remaining = maxBytes
    while (remaining > 0) {
        val count = read(buffer, 0, minOf(buffer.size, remaining))
        if (count < 0) break
        output.write(buffer, 0, count)
        remaining -= count
    }
    return output.toByteArray()
}

private fun Uri.resizeForUpload(context: Context): ByteArray {
    val resolver = context.contentResolver
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(this)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        ?: error("Couldn't reopen the photo.")
    require(bounds.outWidth > 0 && bounds.outHeight > 0) { "The selected image is invalid." }

    var sampleSize = 1
    while (bounds.outWidth / sampleSize > MAX_UPLOAD_DIMENSION * 2 ||
        bounds.outHeight / sampleSize > MAX_UPLOAD_DIMENSION * 2
    ) {
        sampleSize *= 2
    }

    val decoded = resolver.openInputStream(this)?.use {
        BitmapFactory.decodeStream(
            it,
            null,
            BitmapFactory.Options().apply { inSampleSize = sampleSize },
        )
    } ?: error("Couldn't decode the photo.")

    val scale = minOf(
        1f,
        MAX_UPLOAD_DIMENSION.toFloat() / maxOf(decoded.width, decoded.height),
    )
    val output = if (scale < 1f) {
        Bitmap.createScaledBitmap(
            decoded,
            (decoded.width * scale).roundToInt().coerceAtLeast(1),
            (decoded.height * scale).roundToInt().coerceAtLeast(1),
            true,
        ).also { decoded.recycle() }
    } else {
        decoded
    }

    return ByteArrayOutputStream().use { stream ->
        check(output.compress(Bitmap.CompressFormat.JPEG, 85, stream)) {
            "Couldn't prepare the photo for upload."
        }
        output.recycle()
        stream.toByteArray()
    }
}
