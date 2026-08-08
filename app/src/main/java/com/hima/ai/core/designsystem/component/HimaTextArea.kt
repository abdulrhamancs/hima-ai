package com.hima.ai.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hima.ai.core.designsystem.theme.HimaShapes
import com.hima.ai.core.designsystem.theme.HimaTextStyles
import com.hima.ai.core.designsystem.theme.Inter
import com.hima.ai.core.designsystem.theme.LocalHimaColors

/**
 * Multi-line "sunk" input for free-text notes, with the character counter
 * placed inside the field rather than floating beneath it.
 */
@Composable
fun HimaTextArea(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    maxChars: Int = 200,
) {
    val colors = LocalHimaColors.current
    val textStyle = HimaTextStyles.b.copy(fontSize = 15.sp, color = colors.ink)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 104.dp)
            .clip(HimaShapes.small)
            .background(colors.bg2)
            .padding(horizontal = 16.dp, vertical = 15.dp),
    ) {
        BasicTextField(
            value = value,
            onValueChange = { if (it.length <= maxChars) onValueChange(it) },
            textStyle = textStyle,
            cursorBrush = SolidColor(colors.green),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(text = hint, style = textStyle.copy(color = colors.sage))
                }
                innerTextField()
            },
        )
        Text(
            text = "${value.length}/$maxChars",
            style = HimaTextStyles.num.copy(fontFamily = Inter, fontSize = 12.sp),
            color = colors.sage,
            modifier = Modifier.align(Alignment.BottomEnd),
        )
    }
}
