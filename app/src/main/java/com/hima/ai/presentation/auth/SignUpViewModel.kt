package com.hima.ai.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hima.ai.R
import com.hima.ai.core.common.ApiResult
import com.hima.ai.domain.model.UserRole
import com.hima.ai.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Mirrors the backend's own password rule, so a bad password is caught before a round trip. */
private val PasswordRequirement = Regex("^(?=.*[A-Z])(?=.*[0-9]).{8,}$")

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, validationErrorRes = null, errorMessage = null) }
    }

    fun onIdentifierChange(value: String) {
        _uiState.update { it.copy(identifier = value, validationErrorRes = null, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, validationErrorRes = null, errorMessage = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value, validationErrorRes = null, errorMessage = null) }
    }

    fun onRoleSelected(role: UserRole) {
        _uiState.update { it.copy(role = role) }
    }

    fun onSubmit() {
        val state = _uiState.value
        if (state.isSubmitting || !state.canSubmit) return

        if (state.password != state.confirmPassword) {
            _uiState.update { it.copy(validationErrorRes = R.string.auth_error_password_mismatch) }
            return
        }
        if (!PasswordRequirement.matches(state.password)) {
            _uiState.update { it.copy(validationErrorRes = R.string.auth_error_password_requirements) }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, validationErrorRes = null, errorMessage = null) }
        viewModelScope.launch {
            val result = authRepository.signUp(
                email = state.identifier.trim(),
                password = state.password,
                fullName = state.name.trim(),
                role = state.role,
            )
            when (result) {
                is ApiResult.Success -> _uiState.update { it.copy(isSubmitting = false, accountCreated = true) }
                is ApiResult.Failure ->
                    _uiState.update { it.copy(isSubmitting = false, errorMessage = result.error.message) }
            }
        }
    }

    fun onAccountCreatedHandled() {
        _uiState.update { it.copy(accountCreated = false) }
    }
}
