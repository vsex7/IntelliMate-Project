package com.intellimate.intellimate.data.repository

import android.util.Log
import com.intellimate.intellimate.data.local.dao.SuggestionFeedbackDao
import com.intellimate.intellimate.data.local.model.SuggestionFeedback

class FeedbackRepository(private val feedbackDao: SuggestionFeedbackDao) {

    companion object {
        private const val TAG_FEEDBACK_REPO = "IntelliMateFbkRepo"
    }

    suspend fun addFeedback(suggestionText: String, feedbackType: String) {
        Log.i(TAG_FEEDBACK_REPO, "addFeedback called. Suggestion: '${suggestionText.take(50)}...', Type: $feedbackType")
        try {
            val feedbackEntry = SuggestionFeedback(
                suggestionText = suggestionText,
                feedbackType = feedbackType,
                timestamp = System.currentTimeMillis()
            )
            feedbackDao.insert(feedbackEntry)
            Log.d(TAG_FEEDBACK_REPO, "Feedback entry inserted successfully. Timestamp: ${feedbackEntry.timestamp}")
        } catch (e: Exception) {
            Log.e(TAG_FEEDBACK_REPO, "Error inserting feedback for suggestion: '${suggestionText.take(50)}...'", e)
        }
import kotlinx.coroutines.flow.Flow // Ensure Flow is imported

class FeedbackRepository(private val feedbackDao: SuggestionFeedbackDao) {

    companion object {
        private const val TAG_FEEDBACK_REPO = "IntelliMateFbkRepo"
    }

    suspend fun addFeedback(suggestionText: String, feedbackType: String) {
        Log.i(TAG_FEEDBACK_REPO, "addFeedback called. Suggestion: '${suggestionText.take(50)}...', Type: $feedbackType")
        try {
            val feedbackEntry = SuggestionFeedback(
                suggestionText = suggestionText,
                feedbackType = feedbackType,
                timestamp = System.currentTimeMillis()
            )
            feedbackDao.insert(feedbackEntry)
            Log.d(TAG_FEEDBACK_REPO, "Feedback entry inserted successfully. Timestamp: ${feedbackEntry.timestamp}")
        } catch (e: Exception) {
            Log.e(TAG_FEEDBACK_REPO, "Error inserting feedback for suggestion: '${suggestionText.take(50)}...'", e)
        }
    }

    fun getAllFeedbackFlow(): Flow<List<SuggestionFeedback>> { // Uncommented and verified
        Log.d(TAG_FEEDBACK_REPO, "getAllFeedbackFlow called.")
        return feedbackDao.getAllFeedback()
    }
}
