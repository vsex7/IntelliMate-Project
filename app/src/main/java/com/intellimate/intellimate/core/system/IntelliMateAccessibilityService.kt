package com.intellimate.intellimate.core.system

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.intellimate.intellimate.core.mda.MdaAdapterManager // Import Manager
import com.intellimate.intellimate.core.mda.impl.PlaceholderMdaAdapter // Import Placeholder Adapter
import com.intellimate.intellimate.core.mda.impl.SimulatedDatingAppAdapter
import com.intellimate.intellimate.data.local.AppDatabase
import com.intellimate.intellimate.data.repository.ContextRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class IntelliMateAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG_ACCESSIBILITY = "IntelliMateAcsService"
    }

    // private lateinit var mdaAdapter: MdaAdapter // Removed single adapter instance
    private lateinit var mdaAdapterManager: MdaAdapterManager // Added manager
    private lateinit var contextRepository: ContextRepository
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG_ACCESSIBILITY, "onServiceConnected: Service connected and explicitly configured.")

        try {
            val contextEntryDao = AppDatabase.getDatabase(applicationContext).contextEntryDao()
            contextRepository = ContextRepository(contextEntryDao)
            Log.i(TAG_ACCESSIBILITY, "onServiceConnected: ContextRepository initialized successfully.")
        } catch (e: Exception) {
            Log.e(TAG_ACCESSIBILITY, "onServiceConnected: Error initializing ContextRepository", e)
            return
        }

        mdaAdapterManager = MdaAdapterManager()
        Log.i(TAG_ACCESSIBILITY, "onServiceConnected: MdaAdapterManager initialized.")

        // Register adapters
        mdaAdapterManager.registerAdapter(SimulatedDatingAppAdapter(contextRepository, serviceScope))
        mdaAdapterManager.registerAdapter(PlaceholderMdaAdapter())
        Log.i(TAG_ACCESSIBILITY, "onServiceConnected: All MDA adapters registered. Count: ${mdaAdapterManager.getRegisteredPackageNames().size}")
        Log.d(TAG_ACCESSIBILITY, "Registered packages: ${mdaAdapterManager.getRegisteredPackageNames().joinToString()}")


        // Optionally, you can further configure serviceInfo here if needed,
        // but most of it should be in the XML config for clarity.
        // For example, to dynamically set packageNames if adapters are loaded dynamically:
        // val currentServiceInfo = this.serviceInfo
        // currentServiceInfo.packageNames = mdaAdapters.map { it.getPackageName() }.toTypedArray()
        // this.serviceInfo = currentServiceInfo
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) {
            Log.w(TAG_ACCESSIBILITY, "onAccessibilityEvent: Received a null event.")
            return
        }

        val eventPackageName = event.packageName?.toString()
        val eventType = AccessibilityEvent.eventTypeToString(event.eventType)

        Log.i(TAG_ACCESSIBILITY, "onAccessibilityEvent: Received event type '$eventType' from package '$eventPackageName'. ClassName: '${event.className}'.")

        if (eventPackageName == null) {
            Log.w(TAG_ACCESSIBILITY, "onAccessibilityEvent: Event package name is null. Cannot process further.")
            event.source?.recycle() // Recycle source if available and returning
            return
        }

        val sourceNode = event.source
        if (sourceNode == null) {
            Log.w(TAG_ACCESSIBILITY, "onAccessibilityEvent: Source node is null for event type '$eventType' from '$eventPackageName'.")
            return
        }

        val adapter = mdaAdapterManager.getAdapterForPackage(eventPackageName)
        if (adapter != null) {
            Log.d(TAG_ACCESSIBILITY, "onAccessibilityEvent: Adapter '${adapter::class.java.simpleName}' found for package '$eventPackageName'. Processing event...")
            try {
                val processedData = adapter.processAccessibilityEvent(event, sourceNode)
                if (processedData != null) {
                    Log.i(TAG_ACCESSIBILITY, "onAccessibilityEvent: Adapter processed data for '$eventPackageName': '${processedData.take(100)}...'")
                } else {
                    Log.d(TAG_ACCESSIBILITY, "onAccessibilityEvent: Adapter for '$eventPackageName' did not extract data from this event (type: $eventType).")
                }
            } catch (e: Exception) {
                Log.e(TAG_ACCESSIBILITY, "onAccessibilityEvent: Error processing event with adapter '${adapter::class.java.simpleName}' for package '$eventPackageName'.", e)
            }
        } else {
            Log.d(TAG_ACCESSIBILITY, "onAccessibilityEvent: No adapter registered for package '$eventPackageName'. Event type: $eventType.")
        }

        sourceNode.recycle()
    }

    override fun onInterrupt() {
        Log.w(TAG_ACCESSIBILITY, "onInterrupt: Service interrupted.")
        // This method is called when the system wants to interrupt the feedback your service is providing.
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel() // Cancel the scope when the service is destroyed
        Log.i(TAG_ACCESSIBILITY, "onDestroy: Service destroyed and scope cancelled.")
    }
}
