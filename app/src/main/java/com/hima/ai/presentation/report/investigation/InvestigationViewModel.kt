package com.hima.ai.presentation.report.investigation

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hima.ai.R
import com.hima.ai.data.mock.PrototypeSession
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * One selectable answer and the follow-up it produces. [escalates] marks the
 * answers that indicate wider damage, which raises the report's risk score.
 */
data class InvestigationChoice(
    @StringRes val labelRes: Int,
    @StringRes val responseRes: Int,
    val escalates: Boolean,
)

data class InvestigationUiState(
    val choices: List<InvestigationChoice> = emptyList(),
    val selectedIndex: Int? = null,
    val isThinking: Boolean = false,
    @StringRes val responseRes: Int? = null,
    val reportUpdated: Boolean = false,
) {
    val hasAnswered: Boolean get() = selectedIndex != null
}

/**
 * Drives the follow-up exchange. Choosing an answer shows a brief thinking
 * state before the assistant replies, so the interaction reads as reasoning
 * rather than an instant canned response.
 */
@HiltViewModel
class InvestigationViewModel @Inject constructor(
    private val session: PrototypeSession,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        InvestigationUiState(
            choices = listOf(
                InvestigationChoice(R.string.investigation_opt1, R.string.investigation_a1, escalates = true),
                InvestigationChoice(R.string.investigation_opt2, R.string.investigation_a2, escalates = true),
                InvestigationChoice(R.string.investigation_opt3, R.string.investigation_a3, escalates = false),
                InvestigationChoice(R.string.investigation_opt4, R.string.investigation_a4, escalates = false),
            ),
        ),
    )
    val uiState: StateFlow<InvestigationUiState> = _uiState.asStateFlow()

    fun onChoiceSelected(index: Int) {
        val state = _uiState.value
        if (state.hasAnswered) return
        val choice = state.choices.getOrNull(index) ?: return

        _uiState.update { it.copy(selectedIndex = index, isThinking = true) }
        viewModelScope.launch {
            delay(THINKING_DELAY_MS)
            _uiState.update { it.copy(isThinking = false, responseRes = choice.responseRes) }
            if (choice.escalates) session.escalate()
        }
    }

    fun onApplyToReport() {
        _uiState.update { it.copy(reportUpdated = true) }
    }

    private companion object {
        const val THINKING_DELAY_MS = 1100L
    }
}
