package com.hima.ai.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hima.ai.core.common.ApiResult
import com.hima.ai.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onIdentifierChange(value: String) {
        _uiState.update { it.copy(identifier = value, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun onSubmit() {
        val state = _uiState.value
        val email = state.identifier.trim()
        // The submit button is disabled until both fields are non-empty, so
        // reaching here with either blank means a stray call; no-op rather
        // than show an error for something the user hasn't attempted yet.
        if (state.isSubmitting || email.isEmpty() || state.password.isEmpty()) return

        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = authRepository.login(email, state.password)) {
                is ApiResult.Success -> _uiState.update { it.copy(isSubmitting = false, signedIn = true) }
                is ApiResult.Failure ->
                    _uiState.update { it.copy(isSubmitting = false, errorMessage = result.error.message) }
            }
        }
    }

    /** Consumed once the screen has navigated, so returning to Login later starts clean. */
    fun onSignedInHandled() {
        _uiState.update { it.copy(signedIn = false) }
    }
}
