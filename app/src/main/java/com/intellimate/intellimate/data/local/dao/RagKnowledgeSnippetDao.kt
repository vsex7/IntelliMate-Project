package com.intellimate.intellimate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.intellimate.intellimate.data.local.model.RagKnowledgeSnippet
import kotlinx.coroutines.flow.Flow

@Dao
interface RagKnowledgeSnippetDao {

    @Insert
    suspend fun insert(snippet: RagKnowledgeSnippet)

    @Query("SELECT * FROM rag_knowledge_snippets ORDER BY timestamp DESC")
    fun getAllSnippets(): Flow<List<RagKnowledgeSnippet>>

    @Query("SELECT * FROM rag_knowledge_snippets ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLatestSnippets(limit: Int): List<RagKnowledgeSnippet> // For quick access if needed

    @Query("DELETE FROM rag_knowledge_snippets")
    suspend fun clearAll() // Optional: for clearing the knowledge base
}
