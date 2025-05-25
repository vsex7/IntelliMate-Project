package com.intellimate.intellimate.data.local.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ai_interaction_analysis",
    foreignKeys = [
        ForeignKey(
            entity = ContextEntry::class,
            parentColumns = ["id"],
            childColumns = ["relatedContextEntryId"],
            onDelete = ForeignKey.SET_NULL // Or CASCADE, depending on desired behavior
        )
        // Conceptual: ForeignKey to a future AiSuggestionLog entity
        // ForeignKey(entity = AiSuggestionLog::class, ...)
    ],
    indices = [Index(value = ["relatedContextEntryId"]), Index(value = ["suggestionTimestamp"])] // Index for suggestionTimestamp
)
data class AiInteractionAnalysis(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Link to user's message that might have triggered this AI analysis/suggestion
    val relatedContextEntryId: Long? = null,

    // If suggestions themselves become entities, this would be a foreign key.
    // For now, using timestamp of suggestion generation for potential linking.
    val suggestionTimestamp: Long = System.currentTimeMillis(), // Timestamp of when the suggestion/analysis was generated
    val suggestionTextUsed: String?, // The actual suggestion text this analysis is for

    val timestamp: Long = System.currentTimeMillis(), // Timestamp of this analysis record

    // From DialogueAnalysis.EmotionData
    val primaryEmotion: String?,
    val emotionIntensity: Float?,

    // From DialogueAnalysis.IntentData
    val primaryIntent: String?,
    val intentConfidence: Float?,

    // From DialogueAnalysis.SemanticData
    val keywords: String?, // Comma-separated
    val semanticSummary: String?,

    // From SuggestionWithAnalysis
    val coachingTipProvided: String?,
    val explanationProvided: String?
)
