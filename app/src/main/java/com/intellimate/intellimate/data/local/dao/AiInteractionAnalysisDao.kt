package com.intellimate.intellimate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.intellimate.intellimate.data.local.model.AiInteractionAnalysis
import kotlinx.coroutines.flow.Flow

@Dao
interface AiInteractionAnalysisDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(analysis: AiInteractionAnalysis): Long // Return Long for the inserted ID

    @Query("SELECT * FROM ai_interaction_analysis WHERE id = :id")
    suspend fun getById(id: Long): AiInteractionAnalysis?

    @Query("SELECT * FROM ai_interaction_analysis ORDER BY timestamp DESC LIMIT :limit")
    fun getLatestAnalysesFlow(limit: Int): Flow<List<AiInteractionAnalysis>>

    // Example: Query to get analyses related to a specific suggestion timestamp
    @Query("SELECT * FROM ai_interaction_analysis WHERE suggestionTimestamp = :suggestionTimestamp ORDER BY timestamp DESC")
    fun getAnalysesForSuggestionTimestamp(suggestionTimestamp: Long): Flow<List<AiInteractionAnalysis>>
}
