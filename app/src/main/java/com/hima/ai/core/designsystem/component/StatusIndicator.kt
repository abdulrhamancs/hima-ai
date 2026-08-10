package com.hima.ai.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hima.ai.R
import com.hima.ai.core.designsystem.theme.HimaRadius
import com.hima.ai.core.designsystem.theme.HimaTextStyles
import com.hima.ai.core.designsystem.theme.LocalHimaColors
import com.hima.ai.core.designsystem.theme.OnHero

/**
 * The reserve overview's single visual statement. The ring represents the
 * real share of reports that have been resolved.
 */
@Composable
fun StatusIndicator(
    label: String,
    value: String,
    note: String,
    location: String,
    resolvedPercentage: Int?,
    resolvedPercentageLabel: String,
    resolvedLabel: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalHimaColors.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(238.dp)
            .clip(RoundedCornerShape(HimaRadius.hero))
            .background(
                Brush.linearGradient(
                    colors = listOf(colors.green, colors.greenDeep),
                ),
            ),
    ) {
        HeroDecoration(Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 17.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_hima_mark),
                    contentDescription = null,
                    tint = OnHero,
                    modifier = Modifier.size(39.dp),
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.09f))
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_field_pin),
                        contentDescription = null,
                        tint = OnHero,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = location,
                        style = HimaTextStyles.m.copy(fontSize = 12.5.sp, fontWeight = FontWeight.Medium),
                        color = OnHero.copy(alpha = 0.9f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                ResolvedReportsRing(
                    resolvedPercentage = resolvedPercentage,
                    percentageLabel = resolvedPercentageLabel,
                    label = resolvedLabel,
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = label,
                        style = HimaTextStyles.m.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium),
                        color = OnHero.copy(alpha = 0.74f),
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(top = 3.dp),
                    ) {
                        Text(
                            text = value,
                            style = HimaTextStyles.h1.copy(fontSize = 31.sp, fontWeight = FontWeight.Bold),
                            color = OnHero,
                            maxLines = 1,
                        )
                        Box(
                            Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(50))
                                .background(OnHero.copy(alpha = 0.82f)),
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        modifier = Modifier.padding(top = 10.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_feature_shield_check),
                            contentDescription = null,
                            tint = OnHero.copy(alpha = 0.78f),
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = note,
                            style = HimaTextStyles.m.copy(fontSize = 12.5.sp),
                            color = OnHero.copy(alpha = 0.84f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResolvedReportsRing(
    resolvedPercentage: Int?,
    percentageLabel: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    val resolvedFraction = resolvedPercentage?.coerceIn(0, 100)?.div(100f) ?: 0f
    val animatedFraction by animateFloatAsState(
        targetValue = resolvedFraction,
        animationSpec = tween(durationMillis = 700),
        label = "reserveHealthRing",
    )

    Box(
        modifier = modifier
            .size(108.dp)
            .semantics {
                progressBarRangeInfo = resolvedPercentage?.let {
                    ProgressBarRangeInfo(it.coerceIn(0, 100) / 100f, 0f..1f)
                } ?: ProgressBarRangeInfo.Indeterminate
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 9.dp.toPx()
            drawArc(
                color = Color.White.copy(alpha = 0.14f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = stroke),
            )
            drawArc(
                color = OnHero,
                startAngle = -90f,
                sweepAngle = 360f * animatedFraction,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = percentageLabel,
                style = HimaTextStyles.num.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                color = OnHero,
                textAlign = TextAlign.Center,
            )
            Text(
                text = label,
                style = HimaTextStyles.m.copy(fontSize = 10.5.sp, lineHeight = 14.sp),
                color = OnHero.copy(alpha = 0.72f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(start = 10.dp, top = 2.dp, end = 10.dp),
            )
        }
    }
}

@Composable
private fun HeroDecoration(modifier: Modifier = Modifier) {
    val layoutDirection = LocalLayoutDirection.current
    Canvas(modifier) {
        val ringSide = if (layoutDirection == LayoutDirection.Rtl) 0.8f else 0.2f
        val decorationCenter = center.copy(x = size.width * ringSide, y = size.height * 0.65f)
        listOf(68f, 88f, 108f).forEach { radius ->
            drawCircle(
                color = OnHero.copy(alpha = 0.06f),
                radius = radius.dp.toPx(),
                center = decorationCenter,
                style = Stroke(width = 1.dp.toPx()),
            )
        }
        repeat(4) { row ->
            repeat(4) { column ->
                drawCircle(
                    color = OnHero.copy(alpha = 0.08f),
                    radius = 2.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(
                        x = if (layoutDirection == LayoutDirection.Rtl) {
                            (22 + column * 16).dp.toPx()
                        } else {
                            size.width - (22 + column * 16).dp.toPx()
                        },
                        y = size.height - (22 + row * 16).dp.toPx(),
                    ),
                )
            }
        }
    }
}
