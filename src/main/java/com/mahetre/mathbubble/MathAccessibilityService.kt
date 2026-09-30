package com.mahetre.mathbubble

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class MathAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) { /* Base para detección y toque automático. */ }
    override fun onInterrupt() {}
}
