package com.hima.ai.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hima.ai.core.designsystem.theme.HimaTextStyles
import com.hima.ai.core.designsystem.theme.LocalHimaColors

/** Section title with an optional trailing text action (e.g. "View all"). */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    val colors = LocalHimaColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 26.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            style = HimaTextStyles.h2.copy(fontSize = 17.sp),
            color = colors.ink,
        )
        if (actionLabel != null && onActionClick != null) {
            Text(
                text = actionLabel,
                style = HimaTextStyles.m.copy(fontSize = 13.5.sp, fontWeight = FontWeight.Medium),
                color = colors.sage,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onActionClick)
                    .padding(horizontal = 6.dp, vertical = 8.dp),
            )
        }
    }
}

/** A hairline separator. Used instead of borders to divide label/value rows. */
@Composable
fun HimaDivider(modifier: Modifier = Modifier) {
    val colors = LocalHimaColors.current
    Box(
        modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(colors.bg2),
    )
}

/**
 * A label/value pair on plain white. Values may be plain text or a composable
 * (e.g. a [SeverityBadge]) via [valueContent].
 */
@Composable
fun KeyValueRow(
    label: String,
    modifier: Modifier = Modifier,
    value: String? = null,
    valueContent: @Composable (() -> Unit)? = null,
) {
    val colors = LocalHimaColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = HimaTextStyles.b.copy(fontSize = 14.5.sp),
            color = colors.sage,
        )
        when {
            valueContent != null -> valueContent()
            value != null -> Text(
                text = value,
                style = HimaTextStyles.t.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                color = colors.ink,
            )
        }
    }
}
