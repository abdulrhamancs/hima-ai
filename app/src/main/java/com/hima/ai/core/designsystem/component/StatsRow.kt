package com.hima.ai.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    modifier: Modifier = Modifier,
    emphasis: Color? = null,
) {
    val colors = LocalHimaColors.current
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = HimaTextStyles.num.copy(fontSize = 21.sp, fontWeight = FontWeight.SemiBold),
            color = emphasis ?: colors.ink,
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
    modifier: Modifier = Modifier,
    emphasisIndex: Int = -1,
    emphasisColor: Color? = null,
) {
    val colors = LocalHimaColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(HimaRadius.card))
            .background(colors.bg2)
            .padding(vertical = 16.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEachIndexed { index, (value, label) ->
            if (index > 0) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .padding(vertical = 2.dp)
                        .width(1.dp)
                        .background(colors.beige.copy(alpha = 0.45f)),
                )
            }
            StatItem(
                value = value,
                label = label,
                emphasis = if (index == emphasisIndex) emphasisColor else null,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
