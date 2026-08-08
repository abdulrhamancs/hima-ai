package com.hima.ai.data.mock

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory state shared across the prototype flow, so answering a question in
 * the AI investigation can visibly change the final report. Survives navigation
 * but not process death — deliberate for a prototype; real persistence arrives
 * with Firestore.
 */
@Singleton
class PrototypeSession @Inject constructor() {

    private val _riskScore = MutableStateFlow(MockData.RISK_SCORE)
    val riskScore: StateFlow<String> = _riskScore.asStateFlow()

    private val _escalated = MutableStateFlow(false)
    val escalated: StateFlow<Boolean> = _escalated.asStateFlow()

    /** Called when investigation answers indicate wider or repeated damage. */
    fun escalate() {
        _riskScore.value = MockData.RISK_SCORE_ESCALATED
        _escalated.value = true
    }

    /** Resets between prototype runs so a fresh report starts from the base score. */
    fun reset() {
        _riskScore.value = MockData.RISK_SCORE
        _escalated.value = false
    }
}
