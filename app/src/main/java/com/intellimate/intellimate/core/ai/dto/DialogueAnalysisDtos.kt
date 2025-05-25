package com.intellimate.intellimate.core.ai.dto

// Using a single file for all related DTOs for this subtask's scope

data class EmotionData(val primaryEmotion: String, val intensity: Float = 0.8f)

data class IntentData(val primaryIntent: String, val confidence: Float = 0.9f)

data class SemanticData(val keywords: List<String>, val summary: String? = null)

data class DialogueAnalysis(
    val emotion: EmotionData?,
    val intent: IntentData?,
    val semantics: SemanticData?,
    val opennessScore: Int? = null,
    val socialMediaInsight: String? = null,
    val mockUserVoiceTone: String? = null, // Added for Voice/Video Assist
    val mockPartnerVoiceTone: String? = null // Added for Voice/Video Assist
)

data class SuggestionWithAnalysis(
    val suggestion: String,
    val analysis: DialogueAnalysis?,
    val coachingTip: String? = null,
    val explanation: String? = null,
    val predictedNextTopics: List<String>? = null,
    val activeStrategyDisplayName: String? = null // Added for strategy display
)
