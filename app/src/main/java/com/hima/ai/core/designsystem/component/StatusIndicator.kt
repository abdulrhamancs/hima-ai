package com.hima.ai.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hima.ai.R
import com.hima.ai.core.designsystem.theme.HimaRadius
import com.hima.ai.core.designsystem.theme.HimaTextStyles
import com.hima.ai.core.designsystem.theme.IBMPlexSansArabic
import com.hima.ai.domain.model.SceneKind

/**
 * The reserve's headline health state over a valley illustration. One
 * statement, not a dashboard — a ranger should read it in a glance from arm's
 * length in sunlight.
 */
@Composable
fun StatusIndicator(
    label: String,
    value: String,
    note: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(HimaRadius.hero)),
    ) {
        SceneArt(kind = SceneKind.VALLEY, modifier = Modifier.fillMaxSize())
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xDB18261D), Color(0x6B18261D)),
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_feature_leaf),
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(17.dp),
                )
                Text(
                    text = label,
                    style = HimaTextStyles.m.copy(fontSize = 13.sp),
                    color = Color.White.copy(alpha = 0.85f),
                )
            }
            Text(
                text = value,
                style = HimaTextStyles.h1.copy(
                    fontFamily = IBMPlexSansArabic,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                ),
                color = Color.White,
                modifier = Modifier.padding(top = 6.dp),
            )
            Text(
                text = note,
                style = HimaTextStyles.m.copy(fontSize = 12.5.sp),
                color = Color.White.copy(alpha = 0.78f),
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}
