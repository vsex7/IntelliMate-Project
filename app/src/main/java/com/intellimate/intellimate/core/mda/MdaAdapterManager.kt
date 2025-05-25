package com.intellimate.intellimate.core.mda

import android.util.Log

class MdaAdapterManager {

    companion object {
        private const val TAG_MDA_MANAGER = "IntelliMateMdaManager"
    }

    private val adapters = mutableListOf<MdaAdapter>()

    fun registerAdapter(adapter: MdaAdapter) {
        if (adapters.none { it.getPackageName() == adapter.getPackageName() }) {
            adapters.add(adapter)
            Log.i(TAG_MDA_MANAGER, "Adapter registered for package: ${adapter.getPackageName()} - ${adapter::class.java.simpleName}")
        } else {
            Log.w(TAG_MDA_MANAGER, "Adapter for package ${adapter.getPackageName()} already registered.")
        }
    }

    fun getAdapterForPackage(packageName: String): MdaAdapter? {
        val foundAdapter = adapters.find { it.getPackageName() == packageName }
        if (foundAdapter != null) {
            Log.d(TAG_MDA_MANAGER, "Adapter found for package '$packageName': ${foundAdapter::class.java.simpleName}")
        } else {
            Log.d(TAG_MDA_MANAGER, "No adapter found for package '$packageName'.")
        }
        return foundAdapter
    }

    fun getRegisteredPackageNames(): List<String> {
        return adapters.map { it.getPackageName() }
    }
}
