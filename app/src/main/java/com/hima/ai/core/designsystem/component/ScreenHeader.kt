package com.hima.ai.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hima.ai.R
import com.hima.ai.core.designsystem.theme.HimaRadius
import com.hima.ai.core.designsystem.theme.HimaTextStyles
import com.hima.ai.core.designsystem.theme.LocalHimaColors

/** Minimum comfortable touch target for field use (gloves, one-handed). */
val MinTouchTarget = 48.dp

/**
 * A borderless icon button sized for outdoor one-handed use. [filled] gives it
 * the subtle sunk background used in the Home header.
 */
@Composable
fun HimaIconButton(
    iconRes: Int,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
    tint: Color? = null,
    badged: Boolean = false,
) {
    val colors = LocalHimaColors.current
    Box(
        modifier = modifier
            .size(MinTouchTarget)
            .clip(RoundedCornerShape(HimaRadius.icon))
            .background(if (filled) colors.bg2 else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = tint ?: colors.ink,
            modifier = Modifier.size(21.dp),
        )
        if (badged) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 11.dp, end = 11.dp)
                    .size(7.dp)
                    .clip(RoundedCornerShape(50))
                    .background(colors.severityCritical),
            )
        }
    }
}

/**
 * The standard top bar: optional back button, centred title, optional trailing
 * action. Leading and trailing slots are equal width so the title stays
 * optically centred. Chevrons mirror automatically in RTL.
 */
@Composable
fun ScreenHeader(
    modifier: Modifier = Modifier,
    title: String? = null,
    onBackClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    val colors = LocalHimaColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBackClick != null) {
            HimaIconButton(
                iconRes = R.drawable.ic_back,
                contentDescription = stringResource(R.string.cd_back),
                onClick = onBackClick,
            )
        } else {
            Spacer(Modifier.size(MinTouchTarget))
        }

        if (title != null) {
            Text(
                text = title,
                style = HimaTextStyles.h2.copy(fontSize = 17.sp),
                color = colors.ink,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
        } else {
            Spacer(Modifier.weight(1f))
        }

        if (trailing != null) trailing() else Spacer(Modifier.size(MinTouchTarget))
    }
}
