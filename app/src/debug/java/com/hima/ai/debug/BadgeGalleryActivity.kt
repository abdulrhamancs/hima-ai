package com.hima.ai.debug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hima.ai.core.designsystem.component.MedalBadge
import com.hima.ai.core.designsystem.theme.HimaTextStyles
import com.hima.ai.core.designsystem.theme.HimaTheme
import com.hima.ai.core.designsystem.theme.LocalHimaColors
import com.hima.ai.domain.model.BadgeTier

/**
 * Debug-only gallery of every [MedalBadge] tier, for eyeballing the artwork
 * without signing in or walking the nav graph. Lives in `src/debug` so it is
 * never part of a release build.
 *
 * Launch with:
 * `adb shell am start -n com.hima.ai/com.hima.ai.debug.BadgeGalleryActivity --ez dark true`
 */
class BadgeGalleryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dark = intent.getBooleanExtra("dark", false)
        setContent {
            HimaTheme(darkTheme = dark) { BadgeGallery() }
        }
    }
}

@Composable
private fun BadgeGallery() {
    val colors = LocalHimaColors.current
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize().background(colors.bg),
        contentPadding = PaddingValues(top = 60.dp, start = 12.dp, end = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        items(BadgeTier.ordered) { tier ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                MedalBadge(tier = tier, size = 92.dp)
                Text(
                    text = stringResource(tier.nameRes),
                    style = HimaTextStyles.m,
                    color = colors.ink,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}
