package com.intellimate.intellimate.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.intellimate.intellimate.core.ai.dto.SuggestionWithAnalysis // Import DTO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A singleton repository to hold and broadcast the latest AI output (suggestion and analysis).
 */
object SuggestionRepository {

    private val _latestAiOutputFlow = MutableStateFlow<SuggestionWithAnalysis?>(null)
    val latestAiOutputFlow: StateFlow<SuggestionWithAnalysis?> = _latestAiOutputFlow.asStateFlow()

    fun postLastAiOutput(output: SuggestionWithAnalysis?) {
        _latestAiOutputFlow.value = output
    }
}
