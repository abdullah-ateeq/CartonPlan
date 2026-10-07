package com.example.cartonplan.ui.components

import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

object HapticFeedbackHelper {
    fun performLightHaptic(haptic: HapticFeedback, enabled: Boolean) {
        if (enabled) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }
}
