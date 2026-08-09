package com.hima.ai.presentation.more

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hima.ai.R
import com.hima.ai.core.designsystem.component.HimaBottomNavigation
import com.hima.ai.core.designsystem.component.HimaDivider
import com.hima.ai.core.designsystem.component.HimaSecondaryButton
import com.hima.ai.core.designsystem.component.HimaTab
import com.hima.ai.core.designsystem.component.KeyValueRow
import com.hima.ai.core.designsystem.component.LanguageToggle
import com.hima.ai.core.designsystem.component.ScreenHeader
import com.hima.ai.core.designsystem.component.SectionHeader
import com.hima.ai.core.designsystem.theme.LocalHimaColors

/**
 * More — language and sign-out, built entirely from existing
 * components/tokens. This is the destination the bottom nav's "More" tab
 * (and Home's hamburger icon) previously pointed at nothing.
 */
@Composable
fun MoreScreen(
    onSignOutClick: () -> Unit,
    onHomeClick: () -> Unit,
    onMapClick: () -> Unit,
    onNewReportClick: () -> Unit,
    onReportsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MoreViewModel = hiltViewModel(),
) {
    val colors = LocalHimaColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        Spacer(Modifier.height(50.dp))
        ScreenHeader(
            title = stringResource(R.string.more_title),
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            SectionHeader(title = stringResource(R.string.more_section_language))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Start,
            ) {
                LanguageToggle()
            }

            SectionHeader(title = stringResource(R.string.more_section_about))
            KeyValueRow(
                label = stringResource(R.string.more_version),
                value = "0.1.0",
            )
            HimaDivider()

            HimaSecondaryButton(
                text = stringResource(R.string.more_sign_out),
                onClick = {
                    viewModel.onSignOut()
                    onSignOutClick()
                },
                leadingIconRes = R.drawable.ic_logout,
                modifier = Modifier.padding(top = 30.dp),
            )
            Spacer(Modifier.height(30.dp))
        }

        HimaBottomNavigation(
            selected = HimaTab.MORE,
            onHomeClick = onHomeClick,
            onMapClick = onMapClick,
            onNewReportClick = onNewReportClick,
            onReportsClick = onReportsClick,
            onMoreClick = {},
        )
    }
}
