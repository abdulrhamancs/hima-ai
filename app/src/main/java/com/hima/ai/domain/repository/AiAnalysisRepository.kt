package com.hima.ai.domain.repository

import android.net.Uri
import com.hima.ai.core.common.ApiResult
import com.hima.ai.domain.model.AiAnalysis

/** Sends the report's evidence photo to our backend's `/analyze`, which calls Gemini. */
interface AiAnalysisRepository {
    suspend fun analyzeImage(imageUri: Uri, description: String?): ApiResult<AiAnalysis>
}
