package com.hima.ai.presentation.auth

/** UI state for the Login screen. Sign-in is a no-op stub until Firebase Authentication is wired in. */
data class LoginUiState(
    val identifier: String = "",
    val password: String = "",
)
