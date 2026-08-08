package com.hima.ai.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hima.ai.core.designsystem.theme.HimaTextStyles
import com.hima.ai.core.designsystem.theme.Inter
import com.hima.ai.core.designsystem.theme.LocalHimaColors

/**
 * Compact numbered progress for the report flow. Numerals are set in Inter so
 * they stay Latin digits in both languages, matching the reference.
 */
@Composable
fun StepIndicator(
    currentStep: Int,
    modifier: Modifier = Modifier,
    totalSteps: Int = 3,
) {
    val colors = LocalHimaColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (step in 1..totalSteps) {
            if (step > 1) {
                Box(
                    Modifier
                        .width(52.dp)
                        .height(2.dp)
                        .background(if (step <= currentStep) colors.green else colors.warm),
                )
            }
            val active = step <= currentStep
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(if (active) colors.green else colors.bg2),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = step.toString(),
                    style = HimaTextStyles.num.copy(
                        fontFamily = Inter,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = if (active) Color.White else colors.sage,
                )
            }
        }
    }
}
