package com.hima.ai.presentation.more

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hima.ai.BuildConfig
import com.hima.ai.R
import com.hima.ai.core.designsystem.component.HimaBottomNavigation
import com.hima.ai.core.designsystem.component.HimaDivider
import com.hima.ai.core.designsystem.component.HimaTab
import com.hima.ai.core.designsystem.component.LanguageToggle
import com.hima.ai.core.designsystem.component.ScreenHeader
import com.hima.ai.core.designsystem.theme.HimaRadius
import com.hima.ai.core.designsystem.theme.HimaTextStyles
import com.hima.ai.core.designsystem.theme.LocalHimaColors
import com.hima.ai.domain.model.UserRole

/** Account, preferences, support and app information backed by real local/session state. */
@Composable
fun MoreScreen(
    onSignOutClick: () -> Unit,
    onHomeClick: () -> Unit,
    onMapClick: () -> Unit,
    onNewReportClick: () -> Unit,
    onReportsClick: () -> Unit,
    onContactClick: () -> Unit,
    onFaqClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onTermsClick: () -> Unit,
    onRateAppClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MoreViewModel = hiltViewModel(),
) {
    val colors = LocalHimaColors.current
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val darkModeOverride by viewModel.darkModeOverride.collectAsStateWithLifecycle()
    val darkModeEnabled = darkModeOverride ?: androidx.compose.foundation.isSystemInDarkTheme()
    var showSignOutConfirmation by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize().background(colors.bg),
    ) {
        Spacer(Modifier.height(50.dp))
        ScreenHeader(
            title = stringResource(R.string.more_title),
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                SectionLabel(stringResource(R.string.more_section_account))
                ProfileCard(uiState)
            }

            item {
                SettingsSection(stringResource(R.string.more_section_preferences)) {
                    SettingsRow(
                        iconRes = R.drawable.ic_globe,
                        title = stringResource(R.string.more_section_language),
                        trailing = { LanguageToggle() },
                    )
                    HimaDivider()
                    SettingsRow(
                        iconRes = R.drawable.ic_feature_leaf,
                        title = stringResource(R.string.more_dark_mode),
                        subtitle = stringResource(R.string.more_dark_mode_description),
                        toggleValue = darkModeEnabled,
                        onToggle = viewModel::onDarkModeChanged,
                        trailing = { SettingsSwitch(darkModeEnabled) },
                    )
                    HimaDivider()
                    SettingsRow(
                        iconRes = R.drawable.ic_bell,
                        title = stringResource(R.string.more_notifications),
                        subtitle = stringResource(R.string.more_notifications_description),
                        toggleValue = uiState.notificationsEnabled,
                        onToggle = viewModel::onNotificationsChanged,
                        trailing = { SettingsSwitch(uiState.notificationsEnabled) },
                    )
                }
            }

            item {
                SettingsSection(stringResource(R.string.more_section_support)) {
                    SettingsRow(R.drawable.ic_tab_you, stringResource(R.string.more_contact), onClick = onContactClick)
                    HimaDivider()
                    SettingsRow(R.drawable.ic_bot, stringResource(R.string.more_faq), onClick = onFaqClick)
                    HimaDivider()
                    SettingsRow(
                        R.drawable.ic_feature_shield_check,
                        stringResource(R.string.more_privacy),
                        onClick = onPrivacyClick,
                    )
                    HimaDivider()
                    SettingsRow(R.drawable.ic_tab_reports, stringResource(R.string.more_terms), onClick = onTermsClick)
                }
            }

            item {
                SettingsSection(stringResource(R.string.more_section_about)) {
                    SettingsRow(
                        iconRes = R.drawable.ic_hima_mark,
                        title = stringResource(R.string.more_version),
                        trailing = {
                            Text(BuildConfig.VERSION_NAME, style = HimaTextStyles.num.copy(fontSize = 13.sp), color = colors.sage)
                        },
                    )
                    HimaDivider()
                    SettingsRow(
                        iconRes = R.drawable.ic_share,
                        title = stringResource(R.string.more_share_app),
                        onClick = {
                            shareHimaApp(
                                context = context,
                                text = context.getString(R.string.more_share_text),
                                chooserTitle = context.getString(R.string.more_share_chooser),
                            )
                        },
                    )
                    HimaDivider()
                    SettingsRow(R.drawable.ic_spark, stringResource(R.string.more_rate_app), onClick = onRateAppClick)
                }
            }

            item {
                SignOutRow(onClick = { showSignOutConfirmation = true })
            }
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

    if (showSignOutConfirmation) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirmation = false },
            containerColor = colors.surface,
            title = {
                Text(stringResource(R.string.more_sign_out_confirm_title), style = HimaTextStyles.h2, color = colors.ink)
            },
            text = {
                Text(stringResource(R.string.more_sign_out_confirm_message), style = HimaTextStyles.b, color = colors.ink2)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutConfirmation = false
                        viewModel.onSignOut()
                        onSignOutClick()
                    },
                ) {
                    Text(stringResource(R.string.more_sign_out), color = colors.severityCritical)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirmation = false }) {
                    Text(stringResource(R.string.common_cancel), color = colors.ink2)
                }
            },
        )
    }
}

@Composable
private fun ProfileCard(uiState: MoreUiState) {
    val colors = LocalHimaColors.current
    val displayName = uiState.fullName.ifBlank { stringResource(R.string.more_profile_default_name) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .shadow(
                elevation = if (colors.isDark) 0.dp else 5.dp,
                shape = RoundedCornerShape(HimaRadius.card),
                ambientColor = colors.green.copy(alpha = 0.12f),
                spotColor = colors.green.copy(alpha = 0.12f),
            )
            .clip(RoundedCornerShape(HimaRadius.card))
            .background(colors.surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier.size(58.dp).clip(CircleShape).background(colors.green.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = displayName.firstOrNull()?.toString().orEmpty(),
                style = HimaTextStyles.h1,
                color = colors.green,
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = displayName,
                style = HimaTextStyles.t.copy(fontWeight = FontWeight.SemiBold),
                color = colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (uiState.email.isNotBlank()) {
                Text(
                    text = uiState.email,
                    style = HimaTextStyles.m,
                    color = colors.sage,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            uiState.role?.let {
                Text(
                    text = stringResource(
                        if (it == UserRole.FIELD_AGENT) R.string.signup_role_field_agent else R.string.signup_role_authority,
                    ),
                    style = HimaTextStyles.m.copy(fontWeight = FontWeight.Medium),
                    color = colors.green,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
        Column(
            modifier = Modifier.clip(RoundedCornerShape(14.dp)).background(colors.bg2).padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(uiState.reportCount.toString(), style = HimaTextStyles.num.copy(fontSize = 19.sp), color = colors.ink)
            Text(stringResource(R.string.more_profile_reports), style = HimaTextStyles.m.copy(fontSize = 10.sp), color = colors.sage)
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    val colors = LocalHimaColors.current
    Text(text = text, style = HimaTextStyles.h2.copy(fontSize = 16.sp), color = colors.ink)
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    val colors = LocalHimaColors.current
    Column {
        SectionLabel(title)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .shadow(
                    elevation = if (colors.isDark) 0.dp else 4.dp,
                    shape = RoundedCornerShape(HimaRadius.card),
                    ambientColor = colors.green.copy(alpha = 0.10f),
                    spotColor = colors.green.copy(alpha = 0.10f),
                )
                .clip(RoundedCornerShape(HimaRadius.card))
                .background(colors.surface),
            content = content,
        )
    }
}

@Composable
private fun SettingsRow(
    iconRes: Int,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    toggleValue: Boolean? = null,
    onToggle: ((Boolean) -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = LocalHimaColors.current
    val interactionModifier = when {
        toggleValue != null && onToggle != null -> Modifier.toggleable(
            value = toggleValue,
            role = Role.Switch,
            onValueChange = onToggle,
        )
        onClick != null -> Modifier.clickable(onClick = onClick)
        else -> Modifier
    }
    Row(
        modifier = Modifier.fillMaxWidth().then(interactionModifier).padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(colors.bg2),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(iconRes), contentDescription = null, tint = colors.green, modifier = Modifier.size(21.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = HimaTextStyles.t.copy(fontSize = 15.sp), color = colors.ink)
            subtitle?.let {
                Text(it, style = HimaTextStyles.m, color = colors.sage, modifier = Modifier.padding(top = 2.dp))
            }
        }
        when {
            trailing != null -> trailing()
            onClick != null -> Icon(
                painter = painterResource(R.drawable.ic_chevron),
                contentDescription = null,
                tint = colors.sage,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun SettingsSwitch(checked: Boolean) {
    val colors = LocalHimaColors.current
    Switch(
        checked = checked,
        onCheckedChange = null,
        colors = SwitchDefaults.colors(
            checkedThumbColor = colors.onGreen,
            checkedTrackColor = colors.green,
            checkedBorderColor = colors.green,
            uncheckedThumbColor = colors.sage,
            uncheckedTrackColor = colors.bg2,
            uncheckedBorderColor = colors.divider,
        ),
    )
}

@Composable
private fun SignOutRow(onClick: () -> Unit) {
    val colors = LocalHimaColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(HimaRadius.card))
            .background(colors.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_logout),
            contentDescription = null,
            tint = colors.severityCritical,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = stringResource(R.string.more_sign_out),
            style = HimaTextStyles.t.copy(fontWeight = FontWeight.SemiBold),
            color = colors.severityCritical,
        )
    }
}

private fun shareHimaApp(context: android.content.Context, text: String, chooserTitle: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, chooserTitle))
}
