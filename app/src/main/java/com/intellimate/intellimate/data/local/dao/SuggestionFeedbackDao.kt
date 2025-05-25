package com.intellimate.intellimate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.intellimate.intellimate.data.local.model.SuggestionFeedback
import kotlinx.coroutines.flow.Flow

@Dao
interface SuggestionFeedbackDao {

    @Insert
    suspend fun insert(feedback: SuggestionFeedback)

    @Query("SELECT * FROM suggestion_feedback ORDER BY timestamp DESC")
    fun getAllFeedback(): Flow<List<SuggestionFeedback>> // For potential future use to display feedback

    @Query("SELECT * FROM suggestion_feedback ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLatestFeedbackEntries(limit: Int): List<SuggestionFeedback> // More practical for a quick check
}
