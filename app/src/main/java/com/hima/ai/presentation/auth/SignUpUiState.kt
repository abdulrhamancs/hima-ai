package com.hima.ai.presentation.auth

/** UI state for the Sign-up screen. Account creation is a no-op stub until Firebase Authentication is wired in. */
data class SignUpUiState(
    val name: String = "",
    val identifier: String = "",
    val password: String = "",
    val confirmPassword: String = "",
)
