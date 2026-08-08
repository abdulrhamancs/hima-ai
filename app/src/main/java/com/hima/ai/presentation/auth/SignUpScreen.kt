package com.hima.ai.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hima.ai.R
import com.hima.ai.core.designsystem.component.ScreenHeader
import com.hima.ai.core.designsystem.component.HimaPrimaryButton
import com.hima.ai.core.designsystem.component.HimaTextField
import com.hima.ai.core.designsystem.component.HimaTextLink
import com.hima.ai.core.designsystem.theme.HimaTextStyles
import com.hima.ai.core.designsystem.theme.LocalHimaColors

/**
 * Sign-up screen. The approved design shows only the "Create new account"
 * button on Login, not a distinct sign-up mockup — this screen is my own
 * extrapolation using the same field/button language for consistency.
 */
@Composable
fun SignUpScreen(
    onAccountCreated: () -> Unit,
    onBackClick: () -> Unit,
    onSignInInsteadClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SignUpViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SignUpContent(
        uiState = uiState,
        onNameChange = viewModel::onNameChange,
        onIdentifierChange = viewModel::onIdentifierChange,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onSubmit = onAccountCreated,
        onBackClick = onBackClick,
        onSignInInsteadClick = onSignInInsteadClick,
        modifier = modifier,
    )
}

@Composable
private fun SignUpContent(
    uiState: SignUpUiState,
    onNameChange: (String) -> Unit,
    onIdentifierChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBackClick: () -> Unit,
    onSignInInsteadClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalHimaColors.current

    Column(modifier = modifier.fillMaxSize().background(colors.bg)) {
        Column(Modifier.padding(top = 50.dp, start = 20.dp, end = 20.dp)) {
            ScreenHeader(title = stringResource(R.string.signup_title), onBackClick = onBackClick)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            HimaTextField(
                value = uiState.name,
                onValueChange = onNameChange,
                hint = stringResource(R.string.signup_name_hint),
                modifier = Modifier.padding(top = 10.dp),
            )
            HimaTextField(
                value = uiState.identifier,
                onValueChange = onIdentifierChange,
                hint = stringResource(R.string.login_identifier_hint),
                leadingIconRes = R.drawable.ic_field_pin,
                keyboardType = KeyboardType.Email,
                modifier = Modifier.padding(top = 11.dp),
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
            HimaTextField(
                value = uiState.confirmPassword,
                onValueChange = onConfirmPasswordChange,
                hint = stringResource(R.string.signup_confirm_password_hint),
                leadingIconRes = R.drawable.ic_field_password,
                keyboardType = KeyboardType.Password,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.padding(top = 11.dp),
            )

            HimaPrimaryButton(
                text = stringResource(R.string.signup_submit),
                onClick = onSubmit,
                modifier = Modifier.padding(top = 22.dp),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.signup_have_account),
                    style = HimaTextStyles.m,
                    color = colors.sage,
                )
                HimaTextLink(text = stringResource(R.string.login_title), onClick = onSignInInsteadClick)
            }

            Text(
                text = stringResource(R.string.login_terms),
                style = HimaTextStyles.m.copy(lineHeight = 23.sp),
                color = colors.sage,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 30.dp),
            )
        }
    }
}
