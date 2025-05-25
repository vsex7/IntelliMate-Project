package com.intellimate.intellimate.core.model

enum class DatingStrategy(val displayName: String) {
    BALANCED("Balanced Approach"),
    ACCELERATED_CONNECTION("Accelerated Connection"),
    DEEP_CONNECTION("Deep Connection"),
    SOCIAL_EXPLORATION("Social Exploration");

    companion object {
        fun fromString(name: String?): DatingStrategy {
            return entries.find { it.name == name } ?: BALANCED
        }
    }
}
