package com.intellimate.intellimate.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rag_knowledge_snippets")
data class RagKnowledgeSnippet(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val timestamp: Long
)
