package com.hima.ai.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hima.ai.R
import com.hima.ai.core.designsystem.theme.HimaRadius
import com.hima.ai.core.designsystem.theme.HimaTextStyles
import com.hima.ai.core.designsystem.theme.LocalHimaColors

/**
 * The AI working state: progress ring, what's happening, and how long it takes.
 * A real component rather than a blocking spinner, so the wait feels like part
 * of the analysis instead of a stall.
 */
@Composable
fun AIAnalysisState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalHimaColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(HimaRadius.card))
            .background(colors.bg2)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(15.dp),
    ) {
        LoadingIndicator()
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = HimaTextStyles.t,
                color = colors.ink,
            )
            Text(
                text = subtitle,
                style = HimaTextStyles.m.copy(lineHeight = 19.sp),
                color = colors.sage,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

/**
 * One line of the analysis checklist. Completed steps get a filled check;
 * pending steps stay dim so the eye tracks progress down the list.
 */
@Composable
fun AnalysisStepRow(
    label: String,
    done: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = LocalHimaColors.current
    val alpha by animateFloatAsState(
        targetValue = if (done) 1f else 0.4f,
        animationSpec = tween(400),
        label = "stepAlpha",
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp)
            .alpha(alpha),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (done) colors.green else colors.warm),
            contentAlignment = Alignment.Center,
        ) {
            if (done) {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp),
                )
            }
        }
        Text(
            text = label,
            style = HimaTextStyles.b.copy(fontSize = 14.5.sp),
            color = colors.ink2,
        )
    }
}
