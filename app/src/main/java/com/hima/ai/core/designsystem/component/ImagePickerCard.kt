package com.hima.ai.core.designsystem.component

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.hima.ai.R
import com.hima.ai.core.designsystem.theme.HimaRadius
import com.hima.ai.core.designsystem.theme.HimaTextStyles
import com.hima.ai.core.designsystem.theme.LocalHimaColors

/**
 * The two capture entry points, side by side on a sunk surface rather than in
 * outlined boxes. Both tiles are large targets for gloved, one-handed taps.
 */
@Composable
fun ImagePickerCard(
    onCaptureClick: () -> Unit,
    onGalleryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PickerTile(
            iconRes = R.drawable.ic_camera,
            label = stringResource(R.string.new_report_capture),
            onClick = onCaptureClick,
            modifier = Modifier.weight(1f),
        )
        PickerTile(
            iconRes = R.drawable.ic_gallery,
            label = stringResource(R.string.new_report_gallery),
            onClick = onGalleryClick,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PickerTile(
    iconRes: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalHimaColors.current
    Column(
        modifier = modifier
            .height(118.dp)
            .clip(RoundedCornerShape(HimaRadius.card))
            .background(colors.bg2)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = colors.green,
            modifier = Modifier.size(27.dp),
        )
        Text(
            text = label,
            style = HimaTextStyles.t.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium),
            color = colors.ink,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

/**
 * The chosen photo, with an inline affordance to swap it. [imageUri] is the
 * real capture or gallery pick; the gradient only exists to keep the change
 * affordance legible over an unpredictable photo.
 */
@Composable
fun SelectedImageCard(
    imageUri: Uri,
    onChangeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalHimaColors.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(190.dp)
            .clip(RoundedCornerShape(HimaRadius.hero)),
    ) {
        AsyncImage(
            model = imageUri,
            contentDescription = stringResource(R.string.cd_evidence_photo),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.62f to Color.Transparent,
                        1f to Color(0x9E12170F),
                    ),
                ),
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.92f))
                .clickable(onClick = onChangeClick)
                .padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_camera),
                contentDescription = null,
                tint = colors.ink,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = stringResource(R.string.new_report_retake),
                style = HimaTextStyles.m.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium),
                color = colors.ink,
            )
        }
    }
}
