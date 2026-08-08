package com.hima.ai.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hima.ai.core.designsystem.theme.HimaShapes
import com.hima.ai.core.designsystem.theme.HimaTextStyles
import com.hima.ai.core.designsystem.theme.LocalHimaColors

/** Shared label + optional leading icon layout for the button family. */
@Composable
private fun ButtonContent(
    text: String,
    leadingIconRes: Int?,
    style: TextStyle,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        if (leadingIconRes != null) {
            Icon(
                painter = painterResource(leadingIconRes),
                contentDescription = null,
                modifier = Modifier.size(19.dp),
            )
        }
        Text(text = text, style = style, maxLines = 1)
    }
}

/**
 * The single primary action per screen — full-width, 56dp, brand green.
 * Shows the progress ring (not new text) while [loading].
 */
@Composable
fun HimaPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIconRes: Int? = null,
) {
    val colors = LocalHimaColors.current
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = HimaShapes.medium, // 18dp
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.green,
            contentColor = Color.White,
            disabledContainerColor = colors.beige,
            disabledContentColor = Color.White,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
    ) {
        if (loading) {
            LoadingIndicator(diameter = 22.dp, strokeWidth = 2.5.dp)
        } else {
            ButtonContent(text, leadingIconRes, HimaTextStyles.button)
        }
    }
}

/**
 * The secondary action — warm-beige fill, ink text, no shadow. Colours are
 * overridable so the Splash CTA can go white over imagery.
 */
@Composable
fun HimaSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIconRes: Int? = null,
    containerColor: Color = LocalHimaColors.current.warm,
    contentColor: Color = LocalHimaColors.current.ink,
    elevated: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = HimaShapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = if (elevated) 10.dp else 0.dp),
    ) {
        ButtonContent(
            text,
            leadingIconRes,
            HimaTextStyles.button.copy(fontWeight = FontWeight.Medium),
        )
    }
}

/** A quiet, borderless text link (e.g. "Forgot password?"). */
@Composable
fun HimaTextLink(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalHimaColors.current
    TextButton(onClick = onClick, modifier = modifier) {
        Text(
            text = text,
            style = HimaTextStyles.m.copy(fontWeight = FontWeight.Medium, fontSize = 14.sp),
            color = colors.ink2,
        )
    }
}
