package com.intellimate.intellimate.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.intellimate.intellimate.core.ai.AiModelService
import com.intellimate.intellimate.core.ai.dto.PracticeMessage
import com.intellimate.intellimate.core.ai.impl.SimulatedAiModelService
import com.intellimate.intellimate.core.model.PracticePersona
import com.intellimate.intellimate.data.local.AppDatabase
import com.intellimate.intellimate.data.repository.RagRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PracticeViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG_PRACTICE_VM = "IntelliMatePracticeVM"
    }

    // Initialize RagRepository (needed by SimulatedAiModelService)
    // In a DI setup, this would be injected.
    private val appDb = AppDatabase.getDatabase(application)
    private val ragRepository = RagRepository(appDb.ragKnowledgeSnippetDao())
    private val aiModelService: AiModelService = SimulatedAiModelService(ragRepository) // Pass RagRepository

    private val _selectedPersona = MutableStateFlow(PracticePersona.GENERAL_CHAT)
    val selectedPersona: StateFlow<PracticePersona> = _selectedPersona.asStateFlow()

    private val _conversationTranscript = MutableStateFlow<List<PracticeMessage>>(emptyList())
    val conversationTranscript: StateFlow<List<PracticeMessage>> = _conversationTranscript.asStateFlow()

    private val _isSessionActive = MutableStateFlow(false)
    val isSessionActive: StateFlow<Boolean> = _isSessionActive.asStateFlow()

    private val _personaIsThinking = MutableStateFlow(false)
    val personaIsThinking: StateFlow<Boolean> = _personaIsThinking.asStateFlow()

    fun selectPersona(persona: PracticePersona) {
        _selectedPersona.value = persona
        Log.i(TAG_PRACTICE_VM, "Persona selected: ${persona.toDisplayName()}")
        // Optionally, restart session or inform user to restart
        if (_isSessionActive.value) {
            startOrRestartSession() // Restart session if active and persona changes
        }
    }

    fun startOrRestartSession() {
        Log.i(TAG_PRACTICE_VM, "Starting/Restarting practice session with persona: ${selectedPersona.value.toDisplayName()}")
        _conversationTranscript.value = emptyList()
        _isSessionActive.value = true
        _personaIsThinking.value = true
        viewModelScope.launch {
            try {
                val openingLine = aiModelService.getPracticeOpeningLine(selectedPersona.value)
                _conversationTranscript.value = listOf(
                    PracticeMessage(sender = selectedPersona.value.toDisplayName(), text = openingLine)
                )
                Log.d(TAG_PRACTICE_VM, "Session started. Persona opening: '$openingLine'")
            } catch (e: Exception) {
                Log.e(TAG_PRACTICE_VM, "Error getting opening line", e)
                _conversationTranscript.value = listOf(
                    PracticeMessage(sender = "System", text = "Error starting session: ${e.message}", isFeedback = true)
                )
                _isSessionActive.value = false // End session on error
            } finally {
                _personaIsThinking.value = false
            }
        }
    }

    fun sendUserReply(replyText: String) {
        if (!_isSessionActive.value || replyText.isBlank()) {
            Log.w(TAG_PRACTICE_VM, "sendUserReply: Session not active or reply is blank. Ignoring.")
            return
        }
        Log.i(TAG_PRACTICE_VM, "User reply: '$replyText'")
        val userMessage = PracticeMessage(sender = "User", text = replyText)
        _conversationTranscript.value += userMessage // Add user message immediately
        _personaIsThinking.value = true

        viewModelScope.launch {
            try {
                // Pass a snapshot of the current transcript for context
                val currentHistory = _conversationTranscript.value
                val practiceTurn = aiModelService.getPracticeResponse(replyText, selectedPersona.value, currentHistory)

                val newMessages = mutableListOf<PracticeMessage>()
                newMessages.add(PracticeMessage(sender = selectedPersona.value.toDisplayName(), text = practiceTurn.personaResponse))
                practiceTurn.feedbackOnUserInput?.let { feedback ->
                    newMessages.add(PracticeMessage(sender = "Coach", text = feedback, isFeedback = true))
                }
                _conversationTranscript.value += newMessages
                Log.d(TAG_PRACTICE_VM, "Persona response: '${practiceTurn.personaResponse}', Feedback: '${practiceTurn.feedbackOnUserInput}'")

            } catch (e: Exception) {
                Log.e(TAG_PRACTICE_VM, "Error getting practice response", e)
                _conversationTranscript.value += PracticeMessage(sender = "System", text = "Error getting response: ${e.message}", isFeedback = true)
            } finally {
                _personaIsThinking.value = false
            }
        }
    }
}
