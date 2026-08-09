package com.hima.ai.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hima.ai.R
import com.hima.ai.core.designsystem.component.HimaPrimaryButton
import com.hima.ai.core.designsystem.component.HimaSecondaryButton
import com.hima.ai.core.designsystem.component.HimaTextField
import com.hima.ai.core.designsystem.component.HimaTextLink
import com.hima.ai.core.designsystem.component.LanguageToggle
import com.hima.ai.core.designsystem.theme.HimaTextStyles
import com.hima.ai.core.designsystem.theme.IBMPlexSansArabic
import com.hima.ai.core.designsystem.theme.Inter
import com.hima.ai.core.designsystem.theme.LocalHimaColors

/**
 * Login screen — matches the approved "White-first system" prototype: shield
 * + bilingual wordmark, identifier field, password field, forgot-password
 * link, a primary Sign-in action, and a secondary Create-account action.
 */
@Composable
fun LoginScreen(
    onSignInSuccess: () -> Unit,
    onCreateAccountClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.signedIn) {
        if (uiState.signedIn) {
            viewModel.onSignedInHandled()
            onSignInSuccess()
        }
    }

    LoginContent(
        uiState = uiState,
        onIdentifierChange = viewModel::onIdentifierChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSignIn = viewModel::onSubmit,
        onCreateAccountClick = onCreateAccountClick,
        modifier = modifier,
    )
}

@Composable
private fun LoginContent(
    uiState: LoginUiState,
    onIdentifierChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignIn: () -> Unit,
    onCreateAccountClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalHimaColors.current
    var showForgotPasswordNotice by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(50.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            LanguageToggle()
        }
        Spacer(Modifier.height(18.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_hima_mark),
                contentDescription = null,
                tint = colors.green,
                modifier = Modifier.size(48.dp),
            )
            Text(
                text = stringResource(R.string.wordmark_ar),
                style = HimaTextStyles.h1.copy(fontFamily = IBMPlexSansArabic, fontSize = 30.sp),
                color = colors.green,
                modifier = Modifier.padding(top = 14.dp),
            )
            Text(
                text = stringResource(R.string.wordmark_en),
                style = HimaTextStyles.h2.copy(fontFamily = Inter, fontSize = 19.sp),
                color = colors.green.copy(alpha = 0.9f),
            )
            Text(
                text = stringResource(R.string.login_title),
                style = HimaTextStyles.h2,
                color = colors.ink,
                modifier = Modifier.padding(top = 26.dp),
            )
        }

        Column(Modifier.padding(top = 20.dp)) {
            HimaTextField(
                value = uiState.identifier,
                onValueChange = onIdentifierChange,
                hint = stringResource(R.string.login_identifier_hint),
                leadingIconRes = R.drawable.ic_field_pin,
                keyboardType = KeyboardType.Email,
            )
            HimaTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                hint = stringResource(R.string.login_password_hint),
                leadingIconRes = R.drawable.ic_field_password,
                keyboardType = KeyboardType.Password,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.padding(top = 11.dp),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            HimaTextLink(
                text = stringResource(R.string.login_forgot_password),
                onClick = { showForgotPasswordNotice = true },
            )
            AnimatedVisibility(visible = showForgotPasswordNotice, enter = fadeIn(), exit = fadeOut()) {
                Text(
                    text = stringResource(R.string.login_forgot_password_notice),
                    style = HimaTextStyles.m,
                    color = colors.sage,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp),
                )
            }
        }

        AnimatedVisibility(visible = uiState.errorMessage != null, enter = fadeIn(), exit = fadeOut()) {
            Text(
                text = uiState.errorMessage.orEmpty(),
                style = HimaTextStyles.m,
                color = colors.severityCritical,
                modifier = Modifier.padding(top = 10.dp),
            )
        }

        HimaPrimaryButton(
            text = stringResource(R.string.login_submit),
            onClick = onSignIn,
            enabled = uiState.canSubmit,
            loading = uiState.isSubmitting,
            modifier = Modifier.padding(top = 8.dp),
        )
        HimaSecondaryButton(
            text = stringResource(R.string.login_create_account),
            onClick = onCreateAccountClick,
            modifier = Modifier.padding(top = 11.dp),
        )

        Text(
            text = stringResource(R.string.login_terms),
            style = HimaTextStyles.m.copy(lineHeight = 23.sp),
            color = colors.sage,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 26.dp, bottom = 30.dp),
        )
    }
}
