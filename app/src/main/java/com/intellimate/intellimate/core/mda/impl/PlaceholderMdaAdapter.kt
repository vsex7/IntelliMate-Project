package com.intellimate.intellimate.core.mda.impl

import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.intellimate.intellimate.core.mda.MdaAdapter

class PlaceholderMdaAdapter : MdaAdapter {

    companion object {
        private const val TAG_PLACEHOLDER_ADAPTER = "IntelliMatePlaceholderAdapter"
        private const val TARGET_PACKAGE_NAME = "com.example.anotherdatingapp"
    }

    override fun getPackageName(): String {
        return TARGET_PACKAGE_NAME
    }

    override fun processAccessibilityEvent(event: AccessibilityEvent, sourceNode: AccessibilityNodeInfo?): String? {
        val eventType = AccessibilityEvent.eventTypeToString(event.eventType)
        Log.i(TAG_PLACEHOLDER_ADAPTER, "Event received for $TARGET_PACKAGE_NAME. Type: $eventType, ClassName: ${event.className}")
        // This adapter does no actual processing, just logs.
        return null
    }
}
