package com.hima.ai.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hima.ai.R
import com.hima.ai.core.designsystem.theme.HimaTextStyles
import com.hima.ai.core.designsystem.theme.LocalHimaColors
import com.hima.ai.core.util.toggleAppLanguage

/**
 * Globe + target-language pill that flips the whole app between Arabic (RTL)
 * and English (LTR). The label always shows the language you'd switch *to*.
 * Pass [onDark] over imagery. Switching recreates the activity, so callers
 * need no state of their own.
 */
@Composable
fun LanguageToggle(
    modifier: Modifier = Modifier,
    onDark: Boolean = false,
) {
    val colors = LocalHimaColors.current
    val content = if (onDark) Color.White else colors.ink
    val container = if (onDark) Color.White.copy(alpha = 0.16f) else colors.surface

    Row(
        modifier = modifier
            .defaultMinSize(minHeight = MinTouchTarget)
            .shadow(
                elevation = if (!onDark && !colors.isDark) 4.dp else 0.dp,
                shape = RoundedCornerShape(50),
                clip = false,
            )
            .clip(RoundedCornerShape(50))
            .background(container)
            .clickable { toggleAppLanguage() }
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_globe),
            contentDescription = stringResource(R.string.cd_language),
            tint = content,
            modifier = Modifier.size(17.dp),
        )
        Text(
            text = stringResource(R.string.lang_toggle_label),
            style = HimaTextStyles.m.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
            color = content,
        )
    }
}
