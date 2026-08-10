package com.hima.ai.presentation.more

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hima.ai.R
import com.hima.ai.core.designsystem.component.ScreenHeader
import com.hima.ai.core.designsystem.theme.HimaTextStyles
import com.hima.ai.core.designsystem.theme.LocalHimaColors

/** Temporary internal destination until a real support URL or store listing is provided. */
@Composable
fun ComingSoonScreen(
    @StringRes titleRes: Int,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalHimaColors.current
    Column(modifier.fillMaxSize().background(colors.bg)) {
        Spacer(Modifier.size(50.dp))
        ScreenHeader(
            title = stringResource(titleRes),
            onBackClick = onBackClick,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(82.dp)
                    .clip(CircleShape)
                    .background(colors.green.copy(alpha = if (colors.isDark) 0.18f else 0.10f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_spark),
                    contentDescription = null,
                    tint = colors.green,
                    modifier = Modifier.size(34.dp),
                )
            }
            Text(
                text = stringResource(R.string.more_coming_soon),
                style = HimaTextStyles.h2,
                color = colors.ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 18.dp),
            )
            Text(
                text = stringResource(R.string.more_coming_soon_description),
                style = HimaTextStyles.b,
                color = colors.sage,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}
