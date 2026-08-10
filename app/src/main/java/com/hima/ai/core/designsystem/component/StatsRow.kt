package com.hima.ai.core.designsystem.component

import androidx.annotation.DrawableRes
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hima.ai.core.designsystem.theme.HimaRadius
import com.hima.ai.core.designsystem.theme.HimaTextStyles
import com.hima.ai.core.designsystem.theme.LocalHimaColors

/** One figure plus its caption. [emphasis] tints the number (used for critical alerts). */
@Composable
fun StatItem(
    value: String,
    label: String,
    @DrawableRes iconRes: Int?,
    modifier: Modifier = Modifier,
    emphasis: Color? = null,
) {
    val colors = LocalHimaColors.current
    Column(
        modifier = modifier
            .padding(horizontal = 3.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(emphasis?.copy(alpha = if (colors.isDark) 0.16f else 0.08f) ?: Color.Transparent)
            .padding(horizontal = 2.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        iconRes?.let {
            Icon(
                painter = painterResource(it),
                contentDescription = null,
                tint = emphasis ?: colors.green,
                modifier = Modifier.size(17.dp),
            )
        }
        Text(
            text = value,
            style = HimaTextStyles.num.copy(fontSize = 21.sp, fontWeight = FontWeight.SemiBold),
            color = emphasis ?: colors.ink,
            modifier = Modifier.padding(top = if (iconRes == null) 0.dp else 4.dp),
        )
        Text(
            text = label,
            style = HimaTextStyles.m.copy(fontSize = 11.5.sp, lineHeight = 16.sp),
            color = colors.sage,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 5.dp),
        )
    }
}

/**
 * The four Home counters on a single warm surface — one container instead of
 * four separate boxes, which keeps the section compact and quiet.
 */
@Composable
fun StatsRow(
    items: List<Pair<String, String>>,
    iconResIds: List<Int> = emptyList(),
    modifier: Modifier = Modifier,
    emphasisIndex: Int = -1,
    emphasisColor: Color? = null,
) {
    val colors = LocalHimaColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .shadow(
                elevation = if (colors.isDark) 1.dp else 6.dp,
                shape = RoundedCornerShape(HimaRadius.card),
                clip = false,
            )
            .clip(RoundedCornerShape(HimaRadius.card))
            .background(colors.surface)
            .padding(vertical = 8.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEachIndexed { index, (value, label) ->
            if (index > 0) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .padding(vertical = 2.dp)
                        .width(1.dp)
                        .background(colors.divider),
                )
            }
            StatItem(
                value = value,
                label = label,
                iconRes = iconResIds.getOrNull(index),
                emphasis = if (index == emphasisIndex) emphasisColor else null,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
