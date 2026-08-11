package com.hima.ai.core.designsystem.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hima.ai.R
import com.hima.ai.core.designsystem.theme.HimaRadius
import com.hima.ai.core.designsystem.theme.HimaTextStyles
import com.hima.ai.core.designsystem.theme.LocalHimaColors
import com.hima.ai.domain.model.BadgeTier

/** Where a tier stands relative to the signed-in ranger's own progress. */
enum class BadgeRowStatus { OBTAINED, CURRENT, LOCKED }

/** A small tinted capsule label — "Obtained" / "Current badge" / "Not achieved". */
@Composable
fun BadgeStatusPill(text: String, status: BadgeRowStatus, modifier: Modifier = Modifier) {
    val colors = LocalHimaColors.current
    val (bg, fg) = when (status) {
        BadgeRowStatus.OBTAINED -> colors.green.copy(alpha = if (colors.isDark) 0.20f else 0.12f) to colors.green
        BadgeRowStatus.CURRENT -> colors.green to colors.onGreen
        BadgeRowStatus.LOCKED -> colors.bg2 to colors.sage
    }
    Text(
        text = text,
        style = HimaTextStyles.m.copy(fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold),
        color = fg,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

/**
 * One row of the badge ladder — used both by the achieved-only timeline on
 * the overview screen and the full ordered list on the tier-list screen.
 *
 * [showTopConnector] draws a short vertical line above the medal, flush with
 * the previous row's medal below it, so consecutive rows read as a connected
 * path. It is a plain centred [Box], so it needs no RTL handling: a vertical
 * line under the leading-edge icon column stays under that column regardless
 * of reading direction.
 *
 * Locked rows dim via [Modifier.alpha] rather than desaturating colours, and
 * animate that dimming so a tier unlocking mid-session (crossing the report
 * threshold while this screen is open) settles in smoothly instead of
 * snapping.
 */
@Composable
fun BadgeTierRow(
    tier: BadgeTier,
    status: BadgeRowStatus,
    requirementText: String,
    modifier: Modifier = Modifier,
    showTopConnector: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val colors = LocalHimaColors.current
    val rowAlpha by animateFloatAsState(
        targetValue = if (status == BadgeRowStatus.LOCKED) 0.55f else 1f,
        animationSpec = tween(320, easing = FastOutSlowInEasing),
        label = "badgeRowAlpha",
    )
    val statusLabel = when (status) {
        BadgeRowStatus.OBTAINED -> stringResource(R.string.badges_obtained_tag)
        BadgeRowStatus.CURRENT -> stringResource(R.string.badges_current_tag)
        BadgeRowStatus.LOCKED -> stringResource(R.string.badges_not_achieved_tag)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(rowAlpha)
            .clip(RoundedCornerShape(HimaRadius.card))
            .background(
                if (status == BadgeRowStatus.CURRENT) {
                    colors.green.copy(alpha = if (colors.isDark) 0.14f else 0.07f)
                } else {
                    Color.Transparent
                },
            )
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 6.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .width(2.dp)
                    .height(if (showTopConnector) 18.dp else 0.dp)
                    .background(if (status == BadgeRowStatus.LOCKED) colors.divider else colors.green),
            )
            MedalBadge(tier = tier, size = 52.dp, glowing = status == BadgeRowStatus.CURRENT)
        }

        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(tier.nameRes),
                style = HimaTextStyles.t.copy(fontSize = 15.5.sp, fontWeight = FontWeight.Bold),
                color = colors.ink,
            )
            Text(
                text = requirementText,
                style = HimaTextStyles.m.copy(fontSize = 12.5.sp),
                color = colors.sage,
                modifier = Modifier.padding(top = 1.dp, bottom = 6.dp),
            )
            BadgeStatusPill(text = statusLabel, status = status)
        }

        when {
            status == BadgeRowStatus.LOCKED -> Icon(
                painter = painterResource(R.drawable.ic_lock),
                contentDescription = null,
                tint = colors.sage,
                modifier = Modifier.size(18.dp),
            )
            trailing != null -> trailing()
        }

        if (onClick != null) {
            Icon(
                painter = painterResource(R.drawable.ic_chevron),
                contentDescription = null,
                tint = colors.sage,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
