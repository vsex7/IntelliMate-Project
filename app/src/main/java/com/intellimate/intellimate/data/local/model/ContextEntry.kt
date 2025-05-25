package com.intellimate.intellimate.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "context_entries")
data class ContextEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val sourceAppPackage: String,
    val message: String,
    val mockEmotion: String? = null,
    val mockIntent: String? = null,
    val mockKeywords: String? = null // Stored as a comma-separated string
)
