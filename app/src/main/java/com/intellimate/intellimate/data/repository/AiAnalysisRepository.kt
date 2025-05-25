package com.intellimate.intellimate.data.repository

import android.util.Log
import com.intellimate.intellimate.data.local.dao.AiInteractionAnalysisDao
import com.intellimate.intellimate.data.local.model.AiInteractionAnalysis

class AiAnalysisRepository(private val aiAnalysisDao: AiInteractionAnalysisDao) {

    companion object {
        private const val TAG_AI_ANALYSIS_REPO = "IntelliMateAiRepo"
    }

    suspend fun saveAnalysis(analysis: AiInteractionAnalysis): Long {
        Log.i(TAG_AI_ANALYSIS_REPO, "saveAnalysis called for suggestion timestamp: ${analysis.suggestionTimestamp}")
        return try {
            val insertedId = aiAnalysisDao.insert(analysis)
            Log.d(TAG_AI_ANALYSIS_REPO, "Successfully inserted AI interaction analysis. Inserted ID: $insertedId")
            insertedId
        } catch (e: Exception) {
            Log.e(TAG_AI_ANALYSIS_REPO, "Error saving AI interaction analysis for suggestion timestamp: ${analysis.suggestionTimestamp}", e)
            -1L // Return -1 or throw exception to indicate failure
        }
    }

    // Optional: Methods to retrieve analysis data, though not explicitly used by UI in this task
    // fun getLatestAnalysesFlow(limit: Int) = aiAnalysisDao.getLatestAnalysesFlow(limit)
    // suspend fun getAnalysisById(id: Long) = aiAnalysisDao.getById(id)
}
