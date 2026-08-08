package com.hima.ai.presentation.splash

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hima.ai.R
import com.hima.ai.core.designsystem.component.HimaSecondaryButton
import com.hima.ai.core.designsystem.component.LanguageToggle
import com.hima.ai.core.designsystem.component.LoopingVideoBackground
import com.hima.ai.core.designsystem.theme.HimaTextStyles
import com.hima.ai.core.designsystem.theme.IBMPlexSansArabic
import com.hima.ai.core.designsystem.theme.Inter

/**
 * Splash — the app's entry lockup: shield, bilingual wordmark, tagline, and a
 * single "Get started" action, over a silent looping reserve video (slightly
 * blurred, with a dark veil for text contrast) matching the approved design.
 */
@Composable
fun SplashScreen(onGetStarted: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize()) {
        LoopingVideoBackground(
            videoRes = R.raw.splash_loop,
            modifier = Modifier
                .fillMaxSize()
                .blur(10.dp),
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color(0x6B1C221C),
                        0.32f to Color(0x0D161B16),
                        0.66f to Color(0x80161C16),
                        1f to Color(0xE6121712),
                    ),
                ),
        )
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(top = 104.dp, start = 28.dp, end = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_hima_mark),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(60.dp),
                )
                Text(
                    text = stringResource(R.string.wordmark_ar),
                    style = HimaTextStyles.h1.copy(
                        fontFamily = IBMPlexSansArabic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 34.sp,
                    ),
                    color = Color.White,
                    modifier = Modifier.padding(top = 20.dp),
                )
                Text(
                    text = stringResource(R.string.wordmark_en),
                    style = HimaTextStyles.h2.copy(fontFamily = Inter, fontSize = 21.sp),
                    color = Color.White.copy(alpha = 0.95f),
                    modifier = Modifier.padding(top = 2.dp),
                )
                Text(
                    text = stringResource(R.string.splash_tagline),
                    style = HimaTextStyles.b.copy(fontSize = 15.sp, lineHeight = 26.sp),
                    color = Color.White.copy(alpha = 0.86f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 14.dp)
                        .width(240.dp),
                )
                Spacer(Modifier.weight(1f))
                HimaSecondaryButton(
                    text = stringResource(R.string.splash_cta),
                    onClick = onGetStarted,
                    containerColor = Color.White,
                    contentColor = Color(0xFF2C2F2B),
                    elevated = true,
                )
                Row(
                    modifier = Modifier
                        .padding(top = 20.dp, bottom = 26.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    SplashDot(on = true)
                    Spacer(Modifier.width(7.dp))
                    SplashDot(on = false)
                    Spacer(Modifier.width(7.dp))
                    SplashDot(on = false)
                }
            }
            SplashFeaturePanel(Modifier.fillMaxWidth())
        }

        // Language is reachable before sign-in, so a ranger can switch on the
        // very first screen rather than hunting for it after logging in.
        LanguageToggle(
            onDark = true,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 54.dp, end = 20.dp),
        )
    }
}

@Composable
private fun SplashDot(on: Boolean) {
    Box(
        Modifier
            .height(6.dp)
            .width(if (on) 20.dp else 6.dp)
            .clip(CircleShape)
            .background(if (on) Color.White else Color.White.copy(alpha = 0.36f)),
    )
}
