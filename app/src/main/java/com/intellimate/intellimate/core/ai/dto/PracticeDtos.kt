package com.intellimate.intellimate.core.ai.dto

data class PracticeMessage(
    val sender: String, // "User" or "PersonaName"
    val text: String,
    val isFeedback: Boolean = false // True if this message is AI feedback on user's input
)

data class PracticeTurn(
    val personaResponse: String,
    val feedbackOnUserInput: String? // e.g., "Good follow-up!", "A bit direct for this persona."
)
