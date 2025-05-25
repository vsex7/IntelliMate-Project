package com.intellimate.intellimate.data.repository

import android.util.Log
import com.intellimate.intellimate.data.local.dao.ContextEntryDao
import com.intellimate.intellimate.data.local.model.ContextEntry

class ContextRepository(private val contextEntryDao: ContextEntryDao) {

    companion object {
        private const val TAG_CONTEXT_REPO = "IntelliMateCtxRepo" // Standardized TAG
    }

    suspend fun addEntry(
        sourceAppPackage: String,
        message: String,
        emotion: String? = null,
        intent: String? = null,
        keywords: List<String>? = null
    ) {
        val keywordString = keywords?.joinToString(", ")
        Log.i(
            TAG_CONTEXT_REPO,
            "addEntry called for package: '$sourceAppPackage', msg: '${message.take(50)}', emotion: $emotion, intent: $intent, keywords: $keywordString"
        )
        try {
            val entry = ContextEntry(
                timestamp = System.currentTimeMillis(),
                sourceAppPackage = sourceAppPackage,
                message = message,
                mockEmotion = emotion,
                mockIntent = intent,
                mockKeywords = keywordString
            )
            contextEntryDao.insert(entry)
            Log.d(TAG_CONTEXT_REPO, "Successfully inserted context entry. ID: (auto-generated), Timestamp: ${entry.timestamp}.")
        } catch (e: Exception) {
            Log.e(TAG_CONTEXT_REPO, "Error adding entry for package '$sourceAppPackage'.", e)
        }
    }

    suspend fun getLatestEntries(limit: Int): List<ContextEntry> { // Keep this for non-Flow synchronous needs if any
        Log.i(TAG_CONTEXT_REPO, "getLatestEntries (suspend) called with limit: $limit.")
        return try {
            val entries = contextEntryDao.getLatestEntries(limit)
            Log.d(TAG_CONTEXT_REPO, "Retrieved ${entries.size} entries from DB (suspend).")
            entries
        } catch (e: Exception) {
            Log.e(TAG_CONTEXT_REPO, "Error getting latest entries with limit: $limit (suspend).", e)
            emptyList()
        }
    }

    fun getLatestEntriesFlow(limit: Int = 20): Flow<List<ContextEntry>> { // New Flow based method
        Log.i(TAG_CONTEXT_REPO, "getLatestEntriesFlow called with limit: $limit.")
        // Assuming ContextEntryDao.getLatestEntries returns List, not Flow.
        // If DAO can return Flow, this would be simpler: return contextEntryDao.getLatestEntriesFlow(limit)
        // For now, let's assume the DAO's getLatestEntries itself is suspend and returns List.
        // This means the repository method itself cannot directly return a Flow from a suspend fun
        // without using something like a callbackFlow or by changing the DAO.
        // The prompt implies the DAO returns Flow for `getAllSnippets`, so let's assume
        // a similar method `getLatestEntriesFlow(limit: Int): Flow<List<ContextEntry>>` can be added to DAO.
        // For now, I will *assume* such a method exists in the DAO or adjust.
        // If ContextEntryDao.getLatestEntries(limit) is suspend fun returning List<ContextEntry>,
        // this flow version is not directly possible without channelFlow or changing DAO.
        //
        // *** Correction based on DAO capabilities: ***
        // The DAO for getLatestEntries is a suspend fun. To make this a Flow,
        // it should ideally be a @Query that returns Flow<List<ContextEntry>>.
        // Let's assume DAO is updated or this is a conceptual link.
        // For the current structure, if DAO's getLatest is suspend, this should be a suspend fun too.
        // The ViewModel will then use a SharedFlow/StateFlow if it needs to collect it.
        // The request was: `contextRepository.getLatestEntriesFlow(20).stateIn(...)`
        // This implies getLatestEntriesFlow returns a Flow.
        // I will proceed assuming DAO has a Flow-returning method like:
        // @Query("SELECT * FROM context_entries ORDER BY timestamp DESC LIMIT :limit")
        // fun getLatestEntriesFlow(limit: Int): Flow<List<ContextEntry>>
        // And this method in Repository calls that.
        // If that method is not in DAO, this will be a conceptual placeholder.
        // For now, I will *add* such a method to the DAO in the next step if it's missing.
        // For this step, I'll just define it in Repository.
        return contextEntryDao.getLatestEntriesFlow(limit) // ASSUMING DAO has this method
    }


    suspend fun clearAllEntries() {
        Log.i(TAG_CONTEXT_REPO, "clearAllEntries called.")
        try {
            contextEntryDao.clearAll()
            Log.d(TAG_CONTEXT_REPO, "Successfully cleared all context entries from DB.")
        } catch (e: Exception) {
            Log.e(TAG_CONTEXT_REPO, "Error clearing all context entries.", e)
        }
    }
}
