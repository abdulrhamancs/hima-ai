package com.hima.ai.presentation.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hima.ai.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class MoreViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    /**
     * Fire-and-forget: the repository clears the local session immediately
     * regardless of the network outcome, so the screen navigates to Login
     * without waiting on a round trip an unreachable server would stall.
     */
    fun onSignOut() {
        viewModelScope.launch { authRepository.logout() }
    }
}
