package com.intellimate.intellimate.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "suggestion_feedback")
data class SuggestionFeedback(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val suggestionText: String,
    val feedbackType: String, // "good" or "bad"
    val timestamp: Long
)
