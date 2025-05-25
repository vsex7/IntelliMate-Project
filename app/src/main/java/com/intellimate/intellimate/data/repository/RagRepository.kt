package com.intellimate.intellimate.data.repository

import android.util.Log
import com.intellimate.intellimate.data.local.dao.RagKnowledgeSnippetDao
import com.intellimate.intellimate.data.local.model.RagKnowledgeSnippet
import kotlinx.coroutines.flow.Flow

class RagRepository(private val ragDao: RagKnowledgeSnippetDao) {

    companion object {
        private const val TAG_RAG_REPO = "IntelliMateRagRepo"
    }

    suspend fun addSnippet(text: String) {
        Log.i(TAG_RAG_REPO, "addSnippet called with text: '${text.take(50)}...'")
        try {
            val snippet = RagKnowledgeSnippet(
                text = text,
                timestamp = System.currentTimeMillis()
            )
            ragDao.insert(snippet)
            Log.d(TAG_RAG_REPO, "Snippet inserted successfully. Timestamp: ${snippet.timestamp}")
        } catch (e: Exception) {
            Log.e(TAG_RAG_REPO, "Error inserting RAG snippet: '${text.take(50)}...'", e)
        }
    }

    fun getAllSnippetsFlow(): Flow<List<RagKnowledgeSnippet>> {
        Log.d(TAG_RAG_REPO, "getAllSnippetsFlow called.")
        return ragDao.getAllSnippets()
    }

    suspend fun getLatestSnippets(limit: Int = 3): List<RagKnowledgeSnippet> { // Default limit to a few for AI service
        Log.d(TAG_RAG_REPO, "getLatestSnippets called with limit: $limit.")
        return try {
            ragDao.getLatestSnippets(limit)
        } catch (e: Exception) {
            Log.e(TAG_RAG_REPO, "Error getting latest RAG snippets with limit: $limit.", e)
            emptyList()
        }
    }

    suspend fun clearAllSnippets() {
        Log.i(TAG_RAG_REPO, "clearAllSnippets called.")
        try {
            ragDao.clearAll()
            Log.d(TAG_RAG_REPO, "Successfully cleared all RAG snippets.")
        } catch (e: Exception) {
            Log.e(TAG_RAG_REPO, "Error clearing RAG snippets.", e)
        }
    }
}
