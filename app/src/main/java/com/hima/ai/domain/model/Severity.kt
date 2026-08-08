package com.hima.ai.domain.model

import androidx.annotation.StringRes
import com.hima.ai.R

/**
 * Environmental incident severity — a 4-level ramp, deliberately off the brand
 * green action colour. Rendered as a labelled `SeverityBadge`; colours live in
 * `core/designsystem`.
 */
enum class Severity(@StringRes val labelRes: Int) {
    LOW(R.string.severity_low),
    MEDIUM(R.string.severity_medium),
    HIGH(R.string.severity_high),
    CRITICAL(R.string.severity_critical),
}
