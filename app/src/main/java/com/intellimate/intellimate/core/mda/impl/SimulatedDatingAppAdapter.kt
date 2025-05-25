package com.intellimate.intellimate.core.mda.impl

import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.intellimate.intellimate.core.mda.MdaAdapter
import com.intellimate.intellimate.data.repository.ContextRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SimulatedDatingAppAdapter(
    private val contextRepository: ContextRepository,
    private val adapterScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) : MdaAdapter {

    companion object {
        private const val TAG_SIM_ADAPTER = "IntelliMateSimAdapter" // Standardized TAG
    }
    private val TARGET_PACKAGE_NAME = "com.example.simulateddatingapp"

    override fun getPackageName(): String {
        Log.v(TAG_SIM_ADAPTER, "getPackageName() called, returning: $TARGET_PACKAGE_NAME")
        return TARGET_PACKAGE_NAME
    }

    override fun processAccessibilityEvent(event: AccessibilityEvent, sourceNode: AccessibilityNodeInfo?): String? {
        val eventType = AccessibilityEvent.eventTypeToString(event.eventType)
        val eventPackage = event.packageName?.toString() ?: "UnknownPackage"
        Log.i(TAG_SIM_ADAPTER, "processAccessibilityEvent: Received event type '$eventType' from package '$eventPackage'.")

        if (sourceNode == null) {
            Log.w(TAG_SIM_ADAPTER, "processAccessibilityEvent: Source node is null for event type '$eventType'.")
            return null
        }

        val collectedText = StringBuilder()
        Log.d(TAG_SIM_ADAPTER, "processAccessibilityEvent: Starting text collection from source node.")
        collectTextFromNode(sourceNode, collectedText, 0)
        val result = collectedText.toString().trim()

        return if (result.isNotEmpty()) {
            Log.i(TAG_SIM_ADAPTER, "processAccessibilityEvent: Successfully extracted text: '${result.take(100)}...'.")
            adapterScope.launch {
                try {
                    contextRepository.addEntry(eventPackage, result)
                    Log.i(TAG_SIM_ADAPTER, "processAccessibilityEvent: Context entry added to DB for '$eventPackage', msg='${result.take(50)}...'.")
                } catch (e: Exception) {
                    Log.e(TAG_SIM_ADAPTER, "processAccessibilityEvent: Error adding context entry to DB for '$eventPackage'.", e)
                }
            }
            result
        } else {
            Log.d(TAG_SIM_ADAPTER, "processAccessibilityEvent: No text extracted for event type '$eventType' from '$eventPackage'.")
            null
        }
    }

    private fun collectTextFromNode(node: AccessibilityNodeInfo?, output: StringBuilder, depth: Int) {
        if (node == null) {
            Log.v(TAG_SIM_ADAPTER, "collectTextFromNode: Node is null at depth $depth.")
            return
        }
        if (depth > MAX_RECURSION_DEPTH) {
            Log.v(TAG_SIM_ADAPTER, "collectTextFromNode: Max recursion depth $MAX_RECURSION_DEPTH reached for node: ${node.className}.")
            return
        }

        Log.v(TAG_SIM_ADAPTER, "collectTextFromNode: Depth $depth, Class: ${node.className}, Text: '${node.text}', Desc: '${node.contentDescription}'")

        val nodeText = node.text?.toString()?.trim()
        val nodeContentDescription = node.contentDescription?.toString()?.trim()

        if (!nodeText.isNullOrEmpty()) {
            if (output.isNotEmpty()) output.append(" | ")
            output.append(nodeText)
        } else if (!nodeContentDescription.isNullOrEmpty()) {
            // Optionally include content descriptions if they might contain relevant text
            // if (output.isNotEmpty()) output.append(" | ")
            // output.append("Desc: $nodeContentDescription")
        }

        for (i in 0 until node.childCount) {
            val childNode = node.getChild(i)
            if (childNode != null) {
                collectTextFromNode(childNode, output, depth + 1)
                // As per documentation, child nodes obtained from getChild should be recycled
                // if you are not keeping them. However, within a single processing pass, this might
                // be less critical than if storing them. For robust code, consider recycling.
                // childNode.recycle() // Be cautious with recycling if the parent node is still in use or iterating.
            }
        }
    }

    companion object {
        private const val MAX_RECURSION_DEPTH = 5 // To prevent excessively deep traversals
    }
}
