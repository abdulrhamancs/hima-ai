package com.hima.ai.presentation.badges

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hima.ai.domain.model.BadgeTier
import com.hima.ai.domain.model.ReportStatus
import com.hima.ai.domain.repository.AuthRepository
import com.hima.ai.domain.repository.ReportsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** One rung of the ladder the ranger has already reached, with when it happened. */
data class AchievedTier(val tier: BadgeTier, val achievedAt: Instant?)

data class BadgesUiState(
    val totalVerifiedReports: Int = 0,
    val reportsProcessed: Int = 0,
    val daysSinceJoining: Int = 0,
    val currentTier: BadgeTier = BadgeTier.ordered.first(),
    val nextTier: BadgeTier? = BadgeTier.ordered.getOrNull(1),
    /** Raw count toward [nextTier]'s own threshold — "18 of 30", not a per-tier span. */
    val progressCount: Int = 0,
    val progressFraction: Float = 0f,
    val environmentalPoints: Int = 0,
    val achievedTiers: List<AchievedTier> = emptyList(),
)

/** Derives every figure on Badges & Achievements from the same repositories Home and More already use. */
@HiltViewModel
class BadgesViewModel @Inject constructor(
    private val reportsRepository: ReportsRepository,
    authRepository: AuthRepository,
) : ViewModel() {

    val uiState: StateFlow<BadgesUiState> = combine(
        reportsRepository.reports,
        authRepository.currentSession,
    ) { reports, session ->
        val total = reports.size
        val processed = reports.count { it.status == ReportStatus.RESOLVED }
        val joinedAt = session?.user?.joinedAt
        val days = joinedAt?.let {
            Duration.between(it, Instant.now()).toDays().toInt().coerceAtLeast(0)
        } ?: 0

        val currentTier = BadgeTier.forReportCount(total)
        val nextTier = BadgeTier.next(currentTier)
        val progressFraction = nextTier?.let { (total.toFloat() / it.threshold).coerceIn(0f, 1f) } ?: 1f

        // The Nth verified report's own timestamp stands in for "when the
        // ranger crossed this tier's threshold" — real data rather than an
        // invented achievement date, since nothing else records that moment.
        val byCreationAscending = reports.sortedBy { it.createdAt ?: Instant.EPOCH }
        val achieved = BadgeTier.ordered
            .filter { total >= it.threshold }
            .map { tier -> AchievedTier(tier, byCreationAscending.getOrNull(tier.threshold - 1)?.createdAt) }

        BadgesUiState(
            totalVerifiedReports = total,
            reportsProcessed = processed,
            daysSinceJoining = days,
            currentTier = currentTier,
            nextTier = nextTier,
            progressCount = total,
            progressFraction = progressFraction,
            environmentalPoints = total * 10,
            achievedTiers = achieved,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BadgesUiState(),
    )

    init {
        viewModelScope.launch { reportsRepository.refresh() }
    }
}
