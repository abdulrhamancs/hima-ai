package com.hima.ai.presentation.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hima.ai.R
import com.hima.ai.core.designsystem.theme.HimaTextStyles

private data class SplashFeature(val iconRes: Int, val titleRes: Int, val descRes: Int)

private val Features = listOf(
    SplashFeature(R.drawable.ic_feature_leaf, R.string.splash_feature1_title, R.string.splash_feature1_desc),
    SplashFeature(R.drawable.ic_feature_shield_check, R.string.splash_feature2_title, R.string.splash_feature2_desc),
    SplashFeature(R.drawable.ic_feature_chart, R.string.splash_feature3_title, R.string.splash_feature3_desc),
)

/** The 3-column feature highlight strip pinned to the bottom of Splash. */
@Composable
fun SplashFeaturePanel(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color(0xCC121712))
            .padding(top = 22.dp, bottom = 28.dp, start = 8.dp, end = 8.dp),
    ) {
        Features.forEachIndexed { index, feature ->
            if (index > 0) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(Color.White.copy(alpha = 0.14f)),
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    painter = painterResource(feature.iconRes),
                    contentDescription = null,
                    tint = Color(0xFFA9C2AE),
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    text = stringResource(feature.titleRes),
                    style = HimaTextStyles.m.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp),
                )
                Text(
                    text = stringResource(feature.descRes),
                    style = HimaTextStyles.m.copy(fontSize = 11.5.sp, lineHeight = 16.sp),
                    color = Color(0xFF9AA398),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 5.dp),
                )
            }
        }
    }
}
