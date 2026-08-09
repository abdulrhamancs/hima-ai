package com.hima.ai.presentation.auth

/** UI state for the Login screen. */
data class LoginUiState(
    val identifier: String = "",
    val password: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    /** True for exactly one frame after a successful sign-in, so the screen can navigate once. */
    val signedIn: Boolean = false,
) {
    val canSubmit: Boolean get() = identifier.isNotBlank() && password.isNotBlank() && !isSubmitting
}
