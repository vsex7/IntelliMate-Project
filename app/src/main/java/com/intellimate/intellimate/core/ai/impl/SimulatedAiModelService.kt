package com.intellimate.intellimate.core.ai.impl

import android.util.Log
import com.intellimate.intellimate.core.ai.AiModelService
import com.intellimate.intellimate.core.ai.dto.DialogueAnalysis
import com.intellimate.intellimate.core.ai.dto.EmotionData
import com.intellimate.intellimate.core.ai.dto.IntentData
import com.intellimate.intellimate.core.ai.dto.SemanticData
import com.intellimate.intellimate.core.ai.dto.SuggestionWithAnalysis
import kotlinx.coroutines.delay
import kotlin.random.Random

// Import your ApiClient and DTOs if you were to make real calls here
// import com.intellimate.intellimate.data.remote.ApiClient
// import com.intellimate.intellimate.data.remote.dto.LlmRequest
// import com.intellimate.intellimate.data.remote.dto.VlmRequest
import com.intellimate.intellimate.data.repository.RagRepository // Import RagRepository
import kotlinx.coroutines.flow.firstOrNull

class SimulatedAiModelService(
    private val ragRepository: RagRepository? = null // Make it optional for now if MainViewModel isn't updated yet
) : AiModelService {

    companion object {
        private const val TAG_AI_SIM = "IntelliMateAiSimSvc" // Standardized TAG
    }
    // Generic fallback suggestions
    private val genericFallbackSuggestions = listOf(
        "That's interesting, tell me more.",
        "What are your thoughts on that?",
        "Cool!",
        "I see. Anything else on your mind?",
        "Gotcha."
    )
    // Specific keywords to check for demonstration
    private val interestKeywords = mapOf(
        "music" to "What kind of music do you enjoy?",
        "travel" to "Any favorite travel destinations?",
        "food" to "Tried any good restaurants lately?",
        "movie" to "Seen any good movies recently?",
        "book" to "Reading any interesting books?"
    )


import com.intellimate.intellimate.core.model.DatingStrategy // Import DatingStrategy

class SimulatedAiModelService(
    private val ragRepository: RagRepository? = null
) : AiModelService {

    companion object {
        private const val TAG_AI_SIM = "IntelliMateAiSimSvc"
    }
    // Generic fallback suggestions (as before)
    private val genericFallbackSuggestions = listOf(
        "That's interesting, tell me more.",
        "What are your thoughts on that?",
        "Cool!",
        "I see. Anything else on your mind?",
        "Gotcha."
    )
    // Specific keywords to check for demonstration (as before)
    private val interestKeywords = mapOf(
        "music" to "What kind of music do you enjoy?",
        "travel" to "Any favorite travel destinations?",
        "food" to "Tried any good restaurants lately?",
        "movie" to "Seen any good movies recently?",
        "book" to "Reading any interesting books?"
    )

    override suspend fun getSuggestion(
        inputText: String,
        imageContext: ByteArray?,
        formality: String,
        humorLevel: Float,
        enthusiasm: String,
        strategy: DatingStrategy // New strategy parameter
    ): SuggestionWithAnalysis {
        Log.i(TAG_AI_SIM, "getSuggestion called. Input: '${inputText.take(50)}'. Style: Formality=$formality, Humor=$humorLevel, Enthusiasm=$enthusiasm, Strategy=${strategy.name}")

        // --- Conceptual Real API Integration Point (as before) ---
        // In a real implementation, this service would:
        // 1. Potentially use ApiClient.createLlmService() and/or ApiClient.createVlmService().
        //    (Though usually, you'd inject these services rather than creating them directly here).
        //    Example: private val llmService = ApiClient.createLlmService()
        //             private val vlmService = ApiClient.createVlmService()

        // 2. Prepare the request based on inputText and imageContext.
        //    if (imageContext != null) {
        //        // This is a VLM call
        //        // val vlmRequest = VlmRequest(image_url = "base64_encoded_image_or_url", prompt = inputText)
        //        // val vlmResponse = vlmService.analyzeImage(vlmRequest, "YOUR_VLM_API_KEY")
        //        // return vlmResponse.description.generated_text
        //        Log.d(TAG_AI_SIM, "Conceptual VLM call would happen here with image size: ${imageContext.size} bytes.")
        //    } else {
        //        // This is an LLM call
        //        // val llmRequest = LlmRequest(prompt = inputText)
        //        // val llmResponse = llmService.generateText(llmRequest, "YOUR_LLM_API_KEY")
        //        // return llmResponse.choices.firstOrNull()?.text ?: "Error: No response from LLM"
        //        Log.d(TAG_AI_SIM, "Conceptual LLM call would happen here.")
        //    }
        // For now, we simulate this.
        // --- End Conceptual Real API Integration Point ---

        if (imageContext != null) {
            Log.d(TAG_AI_SIM, "Image context provided, size: ${imageContext.size} bytes. (Note: Not processed in this simulation).")
        } else {
            Log.d(TAG_AI_SIM, "No image context was provided.")
        }

        val delayMillis = Random.nextLong(200, 600)
        Log.d(TAG_AI_SIM, "Simulating AI processing latency for $delayMillis ms.")
        delay(delayMillis)

        // Generate Mock Base Analysis
        var mockOpennessScore: Int? = null
        if (strategy == DatingStrategy.ACCELERATED_CONNECTION) {
            mockOpennessScore = Random.nextInt(30, 96) // Score between 30 and 95
            Log.i(TAG_AI_SIM, "Accelerated Connection: Generated Mock Openness Score: $mockOpennessScore")
        }

        val mockEmotion = EmotionData(
            primaryEmotion = listOf("happy", "curious", "neutral", "thoughtful").random(),
            intensity = Random.nextFloat() * 0.6f + 0.4f
        )
        val mockIntent = IntentData(
            primaryIntent = listOf("casual_chat", "info_seeking", "making_plans", "sharing_opinion").random(),
            confidence = Random.nextFloat() * 0.5f + 0.5f
        )
        val mockSemantics = SemanticData(
            keywords = inputText.lowercase().split(" ").take(4).filter { it.length > 3 && it.isNotBlank() }.distinct(),
            summary = "User mentioned: ${inputText.take(30)}..."
        )
        // Include opennessScore in mockAnalysis
        val mockAnalysis = DialogueAnalysis(
            emotion = mockEmotion,
            intent = mockIntent,
            semantics = mockSemantics,
            opennessScore = mockOpennessScore // This will be null if not ACCELERATED_CONNECTION
        )
        Log.i(TAG_AI_SIM, "Generated Mock Analysis: Emotion='${mockAnalysis.emotion?.primaryEmotion}', Intent='${mockAnalysis.intent?.primaryIntent}', Keywords='${mockAnalysis.semantics?.keywords?.joinToString()}', OpennessScore=${mockAnalysis.opennessScore}")

        var suggestionText = ""
        var reasonForSuggestion = "Fallback generic suggestion"
        var styleInfluenceLog = "Style influences: "
        var ragInfluenceLog = "RAG influence: None."
        var mockCoachingTip: String? = null
        var mockExplanation: String? = null

        // RAG influence (as before)
        if (ragRepository != null) {
            val latestSnippets = ragRepository.getLatestSnippets(2)
            val relevantSnippet = latestSnippets.firstOrNull()
            if (relevantSnippet != null) {
                val snippetText = relevantSnippet.text.take(30)
                suggestionText = "Remembering you mentioned '$snippetText', how about you say: 'Let's discuss that more!' or perhaps ask about their thoughts on similar topics?"
                reasonForSuggestion = "Used RAG snippet: '${snippetText}...'"
                ragInfluenceLog = "RAG influence: Used snippet ID ${relevantSnippet.id} - '${snippetText}...'"
            }
        }

        // Emotion/Intent/Keyword based suggestions if RAG didn't fire (as before)
        if (suggestionText.isBlank()) {
            when (mockAnalysis.emotion?.primaryEmotion) {
                "happy" -> if (mockAnalysis.emotion.intensity > 0.7) {
                    suggestionText = "They seem happy! You could say: 'Glad to hear you're feeling good! What's got you in high spirits?'"
                    reasonForSuggestion = "Detected strong happy emotion."
                }
                "sad", "frustrated" -> if (mockAnalysis.emotion.intensity > 0.6) {
                    suggestionText = "It sounds like they might be feeling down. Maybe offer some support, like: 'I'm here if you want to talk about it.'"
                    reasonForSuggestion = "Detected sad/frustrated emotion."
                }
            }
        }
        if (suggestionText.isBlank()) {
            when (mockAnalysis.intent?.primaryIntent) {
                "making_plans" -> if (mockAnalysis.intent.confidence > 0.7) {
                    suggestionText = "Since they seem interested in making plans, how about suggesting: 'Would you be free to [activity] next [day]?'"
                    reasonForSuggestion = "Detected intent: making_plans."
                }
                "info_seeking" -> if (mockAnalysis.intent.confidence > 0.65) {
                    suggestionText = "They seem to be looking for info. Maybe ask: 'Is there anything specific I can help you find out about that?'"
                    reasonForSuggestion = "Detected intent: info_seeking."
                }
            }
        }
        if (suggestionText.isBlank()) {
            mockAnalysis.semantics?.keywords?.forEach { keyword ->
                if (interestKeywords.containsKey(keyword)) {
                    suggestionText = "They mentioned '$keyword'! A good follow-up would be: '${interestKeywords[keyword]}'"
                    reasonForSuggestion = "Detected keyword: $keyword."
                    return@forEach
                }
            }
        }
        if (suggestionText.isBlank()) {
            val fallback = genericFallbackSuggestions.random()
            val firstKeyword = mockAnalysis.semantics?.keywords?.firstOrNull()
            suggestionText = if (firstKeyword != null) {
                "Regarding '$firstKeyword', you could say: '$fallback'"
            } else {
                "You could respond with: '$fallback'"
            }
            reasonForSuggestion = "Fallback, using keyword: '$firstKeyword'."
        }

        // Apply Style Parameters (as before)
        if (formality == "High") {
            suggestionText = "Considering a formal approach: $suggestionText"
            styleInfluenceLog += "High formality applied. "
        } else if (formality == "Low") {
            suggestionText = "$suggestionText Wanna keep it chill?"
            styleInfluenceLog += "Low formality applied. "
        }

        if (humorLevel > 0.7f) {
            suggestionText += " ...and that's why the programmer quit his job, he didn't get arrays! ;)"
            styleInfluenceLog += "High humor added. "
        } else if (humorLevel > 0.3f) {
            suggestionText += " Heh, nice."
            styleInfluenceLog += "Medium humor added. "
        }

        when (enthusiasm) {
            "Enthusiastic" -> {
                suggestionText += " Sounds super exciting!!!"
                styleInfluenceLog += "Enthusiastic style added. "
            }
            "Calm" -> {
                suggestionText = suggestionText.replace("!", ".") + " (Just keeping it mellow)."
                styleInfluenceLog += "Calm style applied. "
            }
        }

        Log.i(TAG_AI_SIM, "Base suggestion logic: $reasonForSuggestion")
        Log.i(TAG_AI_SIM, ragInfluenceLog)
        Log.i(TAG_AI_SIM, styleInfluenceLog)

        var finalSuggestionText = suggestionText
        var strategyInfluenceNote = "Strategy: ${strategy.displayName}."

        when (strategy) {
            DatingStrategy.ACCELERATED_CONNECTION -> {
                val score = mockAnalysis.opennessScore // Already generated and part of mockAnalysis
                if (score != null) {
                    if (score > 75) {
                        finalSuggestionText = "With their high openness ($score/100), be bold: 'This is great, let's meet up! How about [day]?'"
                        strategyInfluenceNote += " High openness ($score) prompted very direct suggestion."
                    } else if (score < 50) {
                        finalSuggestionText = "Openness is moderate ($score/100). A casual invite could work: 'Enjoying this chat! Fancy a coffee sometime soon?'"
                        strategyInfluenceNote += " Moderate openness ($score) prompted casual direct suggestion."
                    } else { // Medium score
                        finalSuggestionText = "Openness ($score/100) looks good. To speed things up: $finalSuggestionText How about suggesting a quick call or meeting?"
                        strategyInfluenceNote += " Good openness ($score) supported direct approach."
                    }
                } else { // Fallback if score somehow wasn't generated (should not happen for ACCELERATED_CONNECTION)
                    finalSuggestionText = "To speed things up: $finalSuggestionText How about suggesting a quick call or meeting up soon?"
                    strategyInfluenceNote += " Made suggestion more direct and action-oriented (no openness score available)."
                }
                mockCoachingTip = "Tip (Accelerated): Openness score: ${score ?: "N/A"}. Be bold if high, or proceed with a casual invite if moderate."
            }
            DatingStrategy.DEEP_CONNECTION -> {
                val deepTopic = mockAnalysis.semantics?.keywords?.firstOrNull() ?: "values or future goals"
                finalSuggestionText = "For a deeper connection: $finalSuggestionText What are their thoughts on $deepTopic?"
                strategyInfluenceNote += " Added a prompt for deeper conversation around '$deepTopic'."
                mockCoachingTip = "Tip (Deep): Look for opportunities to discuss values, dreams, or significant experiences."
            }
            DatingStrategy.SOCIAL_EXPLORATION -> {
                finalSuggestionText = "For social exploration: $finalSuggestionText Is this something they enjoy with friends or a way to meet new people?"
                strategyInfluenceNote += " Framed suggestion towards social aspects."
                mockCoachingTip = "Tip (Social): Keep it light, explore shared activities, and be open to group interactions."
            }
            DatingStrategy.BALANCED -> {
                strategyInfluenceNote += " Maintained balanced approach."
                // Base coaching tip might be fine, or slightly adjust if needed
                mockCoachingTip = mockCoachingTip ?: "Tip (Balanced): Maintain a good conversational flow, balancing listening and sharing."
            }
        }
        Log.i(TAG_AI_SIM, strategyInfluenceNote)


        // Generate Coaching Tip based on final analysis and strategy (if not already set by strategy)
        if (mockCoachingTip == null) { // Fallback if strategy didn't set a specific tip
            mockCoachingTip = when (mockAnalysis.emotion?.primaryEmotion) {
                "happy" -> "Positive vibe! Now's a great time to share more or ask something fun."
                "curious" -> "They seem curious. Encourage their questions or ask one back!"
                "sad", "frustrated" -> "Sense some negativity. Tread carefully, offer support if appropriate."
                else -> "Keep the conversation balanced and engaging."
            }
            if (mockAnalysis.intent?.primaryIntent == "making_plans" && mockAnalysis.intent.confidence > 0.6) {
                mockCoachingTip = "Looks like there's interest in meeting up. Consider suggesting a concrete idea if the moment feels right."
            } else if (mockAnalysis.intent?.primaryIntent == "info_seeking" && mockAnalysis.intent.confidence > 0.6) {
                mockCoachingTip = "They're asking questions. Be open and answer thoughtfully to build rapport."
            }
        }
        Log.i(TAG_AI_SIM, "Final Coaching Tip: '$mockCoachingTip'")

        // Generate XAI Explanation
        mockExplanation = "Why this suggestion? $reasonForSuggestion"
        if (styleInfluenceLog.replace("Style influences: ", "").isNotBlank()) {
            mockExplanation += " Style applied: ${styleInfluenceLog.replace("Style influences: ", "").trim()}"
        }
        if (ragInfluenceLog.replace("RAG influence: ", "").isNotBlank() && ragInfluenceLog != "RAG influence: None.") {
            mockExplanation += " Knowledge used: ${ragInfluenceLog.replace("RAG influence: ", "").trim()}"
        }
        Log.i(TAG_AI_SIM, "Generated XAI Explanation: '$mockExplanation'")

        // Generate Mock Predicted Next Topics
        val allPossibleNextTopics = listOf(
            "Ask about their weekend plans",
            "Share a recent personal achievement",
            "Discuss a common interest (e.g., ${mockAnalysis.semantics?.keywords?.firstOrNull() ?: "hobbies"})",
            "Talk about a new movie or show",
            "Ask a light-hearted hypothetical question"
        )
        val mockPredictedNextTopics = allPossibleNextTopics.shuffled().take(Random.nextInt(1, 3)) // Take 1 or 2 random topics
        Log.i(TAG_AI_SIM, "Generated Predicted Next Topics: $mockPredictedNextTopics")


        Log.i(TAG_AI_SIM, "Final context-aware & styled suggestion: '${suggestionText.take(150)}...'")
        return SuggestionWithAnalysis(
            suggestion = suggestionText,
            analysis = mockAnalysis,
            coachingTip = mockCoachingTip,
            explanation = mockExplanation,
            predictedNextTopics = mockPredictedNextTopics
        )
    }

    // --- Practice Mode Method Implementations ---

    override suspend fun getPracticeOpeningLine(persona: com.intellimate.intellimate.core.model.PracticePersona): String {
        Log.i(TAG_AI_SIM, "getPracticeOpeningLine called for persona: ${persona.name}")
        delay(Random.nextLong(100, 300)) // Simulate a quick response
        return when (persona) {
            com.intellimate.intellimate.core.model.PracticePersona.GENERAL_CHAT -> "Hey there! What's on your mind today?"
            com.intellimate.intellimate.core.model.PracticePersona.FLIRTY_ACCELERATED -> "Well hello there... 😉 Ready to dive into some fun chat?"
            com.intellimate.intellimate.core.model.PracticePersona.SERIOUS_DEEP -> "Hello. I'm interested in having a meaningful conversation. What's something you're passionate about?"
        }
    }

    override suspend fun getPracticeResponse(
        userInput: String,
        persona: com.intellimate.intellimate.core.model.PracticePersona,
        conversationHistory: List<com.intellimate.intellimate.core.ai.dto.PracticeMessage>
    ): com.intellimate.intellimate.core.ai.dto.PracticeTurn {
        Log.i(TAG_AI_SIM, "getPracticeResponse called for persona: ${persona.name}, userInput: '${userInput.take(50)}...'")
        delay(Random.nextLong(300, 700)) // Simulate AI thinking

        val personaResponse: String
        var feedback: String? = null

        when (persona) {
            com.intellimate.intellimate.core.model.PracticePersona.GENERAL_CHAT -> {
                personaResponse = "That's an interesting point about '${userInput.take(20)}...'. I wonder, what's your take on [related topic like 'current events' or 'hobbies']?"
                if (userInput.length < 10) feedback = "A bit short, but okay! Try elaborating more next time."
                else if (userInput.contains("?")) feedback = "Good use of a question to keep it interactive!"
                else feedback = "Solid reply!"
            }
            com.intellimate.intellimate.core.model.PracticePersona.FLIRTY_ACCELERATED -> {
                personaResponse = "Ooh, '${userInput.take(20)}...' you say? 😉 That's bold. I like it. What's your wildest dream date?"
                if (userInput.length < 15 && !userInput.contains("😉")) feedback = "Short and sweet! Maybe add a playful emoji next time? 😉"
                else if (userInput.contains("date") || userInput.contains("meet")) feedback = "Cutting to the chase, are we? Confident!"
                else feedback = "Intriguing... keep that energy up!"
            }
            com.intellimate.intellimate.core.model.PracticePersona.SERIOUS_DEEP -> {
                personaResponse = "I appreciate you sharing that about '${userInput.take(20)}...'. It makes me think about [deeper concept like 'the nature of connection' or 'personal growth']. How do you see it?"
                if (userInput.length < 20) feedback = "Thanks for sharing. To go deeper, perhaps expand on your thoughts a bit more?"
                else if (userInput.contains("feel") || userInput.contains("think") || userInput.contains("believe")) feedback = "Great job sharing your perspective thoughtfully!"
                else feedback = "That's a good starting point for a deeper discussion."
            }
        }
        Log.d(TAG_AI_SIM, "Persona response: '$personaResponse', Feedback: '$feedback'")
        return com.intellimate.intellimate.core.ai.dto.PracticeTurn(personaResponse, feedback)
    }
}
