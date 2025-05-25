package com.intellimate.intellimate.core.mda

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Interface for Mobile Dating App (MDA) Adapters.
 * Each adapter is responsible for handling app-specific logic
 * for extracting information from accessibility events.
 */
interface MdaAdapter {
    /**
     * Returns the package name of the dating app this adapter supports.
     */
    fun getPackageName(): String

    /**
     * Processes an accessibility event from the supported application.
     *
     * @param event The AccessibilityEvent that occurred.
     * @param sourceNode The source node of the event, if available. Can be null.
     * @return A String containing extracted relevant text or data,
     *         or null if no relevant data could be extracted from this event.
     */
    fun processAccessibilityEvent(event: AccessibilityEvent, sourceNode: AccessibilityNodeInfo?): String?
}
