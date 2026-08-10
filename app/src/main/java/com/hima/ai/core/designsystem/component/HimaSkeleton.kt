package com.hima.ai.core.designsystem.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.hima.ai.core.designsystem.theme.HimaRadius
import com.hima.ai.core.designsystem.theme.LocalHimaColors

/** Reusable report placeholder used by Home and the report history screen. */
@Composable
fun ReportRowSkeleton(modifier: Modifier = Modifier) {
    val colors = LocalHimaColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clip(RoundedCornerShape(HimaRadius.card))
            .background(colors.surface)
            .height(102.dp)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        SkeletonBlock(
            modifier = Modifier.width(94.dp).height(84.dp),
            shape = RoundedCornerShape(HimaRadius.thumb),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            SkeletonBlock(Modifier.fillMaxWidth(0.72f).height(16.dp))
            SkeletonBlock(Modifier.fillMaxWidth(0.92f).height(12.dp))
            SkeletonBlock(Modifier.fillMaxWidth(0.58f).height(11.dp))
        }
        SkeletonBlock(
            modifier = Modifier.width(46.dp).height(24.dp),
            shape = RoundedCornerShape(12.dp),
        )
    }
}

@Composable
private fun SkeletonBlock(
    modifier: Modifier,
    shape: Shape = RoundedCornerShape(6.dp),
) {
    val colors = LocalHimaColors.current
    val transition = rememberInfiniteTransition(label = "himaSkeleton")
    val offset by transition.animateFloat(
        initialValue = -350f,
        targetValue = 900f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_150, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "himaSkeletonOffset",
    )
    val base = if (colors.isDark) colors.bg2 else colors.warm
    val highlight = if (colors.isDark) colors.divider.copy(alpha = 0.82f) else colors.surface
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(base, highlight, base),
                    start = Offset(offset - 260f, 0f),
                    end = Offset(offset, 180f),
                ),
            ),
    )
}
