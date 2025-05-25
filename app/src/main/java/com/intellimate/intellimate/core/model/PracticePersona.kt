package com.intellimate.intellimate.core.model

enum class PracticePersona {
    GENERAL_CHAT,
    FLIRTY_ACCELERATED,
    SERIOUS_DEEP;

    fun toDisplayName(): String {
        return when (this) {
            GENERAL_CHAT -> "General Chat"
            FLIRTY_ACCELERATED -> "Flirty & Fast-Paced"
            SERIOUS_DEEP -> "Serious & Deep Convo"
        }
    }

    companion object {
        fun fromDisplayName(displayName: String): PracticePersona {
            return entries.find { it.toDisplayName() == displayName } ?: GENERAL_CHAT
        }
    }
}
