package com.hima.ai.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.hima.ai.core.designsystem.theme.LocalHimaColors

/** Height of [HimaBottomNavigation], for screens that float content above it. */
val BottomNavHeight = 84.dp

/** Which primary destination is currently showing. */
enum class HimaTab { HOME, MAP, REPORTS, MORE }

/**
 * Five-slot bottom navigation with the New report action raised in the centre.
 * Item order follows layout direction automatically, so it mirrors in Arabic.
 */
@Composable
fun HimaBottomNavigation(
    selected: HimaTab,
    onHomeClick: () -> Unit,
    onMapClick: () -> Unit,
    onNewReportClick: () -> Unit,
    onReportsClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalHimaColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(BottomNavHeight)
            .background(colors.bg)
            .padding(top = 9.dp, start = 6.dp, end = 6.dp),
        verticalAlignment = Alignment.Top,
    ) {
        TabItem(
            iconRes = R.drawable.ic_tab_home,
            label = stringResource(R.string.tab_home),
            selected = selected == HimaTab.HOME,
            onClick = onHomeClick,
            modifier = Modifier.weight(1f),
        )
        TabItem(
            iconRes = R.drawable.ic_tab_map,
            label = stringResource(R.string.tab_map),
            selected = selected == HimaTab.MAP,
            onClick = onMapClick,
            modifier = Modifier.weight(1f),
        )
        NewReportAction(onClick = onNewReportClick, modifier = Modifier.weight(1f))
        TabItem(
            iconRes = R.drawable.ic_tab_reports,
            label = stringResource(R.string.tab_reports),
            selected = selected == HimaTab.REPORTS,
            onClick = onReportsClick,
            modifier = Modifier.weight(1f),
        )
        TabItem(
            iconRes = R.drawable.ic_tab_more,
            label = stringResource(R.string.tab_more),
            selected = selected == HimaTab.MORE,
            onClick = onMoreClick,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun TabItem(
    iconRes: Int,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalHimaColors.current
    val tint = if (selected) colors.ink else colors.sage
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(top = 6.dp, bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = label,
            style = HimaTextStyles.m.copy(
                fontSize = 10.5.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            ),
            color = tint,
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun NewReportAction(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalHimaColors.current
    val interaction = remember { MutableInteractionSource() }
    Column(
        modifier = modifier
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                // Raised so the primary action breaks the bar's top edge.
                .offset(y = (-13).dp)
                .size(52.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(colors.green),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_plus),
                contentDescription = stringResource(R.string.cd_new_report),
                tint = Color.White,
                modifier = Modifier.size(24.dp),
            )
        }
        Text(
            text = stringResource(R.string.tab_new_report),
            style = HimaTextStyles.m.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Medium),
            color = colors.ink2,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 5.dp),
        )
    }
}
