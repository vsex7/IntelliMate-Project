package com.intellimate.intellimate.core.ai

/**
 * Interface for AI model services (VLM/LLM).
 */
import com.intellimate.intellimate.core.ai.dto.SuggestionWithAnalysis

interface AiModelService {
    /**
     * Gets a suggestion from the AI model based on input text and optional image context.
     *
     * @param inputText The primary text input for generating the suggestion.
     * @param imageContext Optional image data that might provide context.
     *                     (Placeholder type; actual implementation might use Bitmap, Uri, etc.)
     * @param formality Current formality preference (e.g., "Low", "Medium", "High").
     * @param humorLevel Current humor level preference (e.g., 0.0f to 1.0f).
     * @param enthusiasm Current enthusiasm preference (e.g., "Calm", "Neutral", "Enthusiastic").
     * @param strategy Current dating strategy preference.
     * @return A SuggestionWithAnalysis object containing the AI-generated suggestion and its analysis.
     */
    suspend fun getSuggestion(
        inputText: String,
        imageContext: ByteArray? = null,
        formality: String,
        humorLevel: Float,
        enthusiasm: String,
        strategy: com.intellimate.intellimate.core.model.DatingStrategy,
        voiceAssistModeEnabled: Boolean // New parameter for voice assist mode
    ): SuggestionWithAnalysis

    // --- Practice Mode Methods ---
    /**
     * Gets an opening line for a practice session with a specific persona.
     * @param persona The selected practice persona.
     * @return A string containing the opening line from the persona.
     */
    suspend fun getPracticeOpeningLine(persona: com.intellimate.intellimate.core.model.PracticePersona): String

    /**
     * Gets a response from the practice persona based on user input and conversation history.
     * @param userInput The user's latest reply.
     * @param persona The selected practice persona.
     * @param conversationHistory The current transcript of the practice conversation.
     * @return A PracticeTurn object containing the persona's response and feedback on the user's input.
     */
    suspend fun getPracticeResponse(
        userInput: String,
        persona: com.intellimate.intellimate.core.model.PracticePersona,
        conversationHistory: List<com.intellimate.intellimate.core.ai.dto.PracticeMessage>
    ): com.intellimate.intellimate.core.ai.dto.PracticeTurn
}
