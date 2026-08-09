package com.hima.ai.presentation.auth

import androidx.annotation.StringRes
import com.hima.ai.domain.model.UserRole

/** UI state for the Sign-up screen. */
data class SignUpUiState(
    val name: String = "",
    val identifier: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val role: UserRole = UserRole.FIELD_AGENT,
    val isSubmitting: Boolean = false,
    /** Client-side checks (password mismatch/requirements) — resolved locally, no network round trip. */
    @StringRes val validationErrorRes: Int? = null,
    /** What the server rejected the request for. */
    val errorMessage: String? = null,
    /** True for exactly one frame after account creation, so the screen can navigate once. */
    val accountCreated: Boolean = false,
) {
    val canSubmit: Boolean
        get() = name.isNotBlank() && identifier.isNotBlank() && password.isNotBlank() &&
            confirmPassword.isNotBlank() && !isSubmitting
}
