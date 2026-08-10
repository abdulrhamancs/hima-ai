package com.hima.ai.presentation.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hima.ai.core.util.ThemePreferences
import com.hima.ai.core.util.MorePreferences
import com.hima.ai.domain.model.UserRole
import com.hima.ai.domain.repository.AuthRepository
import com.hima.ai.domain.repository.ReportsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MoreUiState(
    val fullName: String = "",
    val email: String = "",
    val role: UserRole? = null,
    val reportCount: Int = 0,
    val notificationsEnabled: Boolean = true,
)

@HiltViewModel
class MoreViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val themePreferences: ThemePreferences,
    private val reportsRepository: ReportsRepository,
    private val morePreferences: MorePreferences,
) : ViewModel() {

    val darkModeOverride = themePreferences.darkModeOverride
    val uiState: StateFlow<MoreUiState> = combine(
        authRepository.currentSession,
        reportsRepository.reports,
        morePreferences.notificationsEnabled,
    ) { session, reports, notificationsEnabled ->
        MoreUiState(
            fullName = session?.user?.fullName.orEmpty(),
            email = session?.user?.email.orEmpty(),
            role = session?.user?.role,
            reportCount = reports.size,
            notificationsEnabled = notificationsEnabled,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MoreUiState(),
    )

    fun onDarkModeChanged(enabled: Boolean) {
        themePreferences.setDarkModeEnabled(enabled)
    }

    fun onNotificationsChanged(enabled: Boolean) {
        morePreferences.setNotificationsEnabled(enabled)
    }

    /**
     * Fire-and-forget: the repository clears the local session immediately
     * regardless of the network outcome, so the screen navigates to Login
     * without waiting on a round trip an unreachable server would stall.
     */
    fun onSignOut() {
        viewModelScope.launch { authRepository.logout() }
    }
}
