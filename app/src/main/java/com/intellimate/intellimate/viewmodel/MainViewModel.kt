package com.intellimate.intellimate.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import android.app.Application
import android.util.Log // Ensure Log is imported
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.intellimate.intellimate.core.ai.AiModelService
import com.intellimate.intellimate.core.ai.dto.DialogueAnalysis // Import DialogueAnalysis
import com.intellimate.intellimate.core.ai.impl.SimulatedAiModelService
import com.intellimate.intellimate.data.local.AppDatabase
import com.intellimate.intellimate.data.local.model.ContextEntry
import com.intellimate.intellimate.data.local.model.RagKnowledgeSnippet // Import RAG Snippet
import com.intellimate.intellimate.data.repository.ContextRepository
import com.intellimate.intellimate.data.repository.RagRepository // Import RAG Repository
import com.intellimate.intellimate.data.repository.StylePreferences
import com.intellimate.intellimate.data.repository.StylePreferencesRepository
import com.intellimate.intellimate.data.repository.SuggestionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG_MAIN_VM = "IntelliMateMainVM" // Standardized TAG
    }

    private val aiModelService: AiModelService
    private val contextRepository: ContextRepository
    private val stylePreferencesRepository: StylePreferencesRepository
    private val ragRepository: RagRepository
    private val feedbackRepository: FeedbackRepository
    private val aiAnalysisRepository: com.intellimate.intellimate.data.repository.AiAnalysisRepository
    private val potentialMatchRepository: com.intellimate.intellimate.data.repository.PotentialMatchRepository // Added

    private val _suggestion = MutableStateFlow<String>("Click the button to get a suggestion.")
    val suggestion: StateFlow<String> = _suggestion.asStateFlow()

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // private val _contextEntries = MutableStateFlow<List<ContextEntry>>(emptyList()) // Replaced by contextHistory
    // val contextEntries: StateFlow<List<ContextEntry>> = _contextEntries.asStateFlow() // Replaced

    private val _currentDialogueAnalysis = MutableStateFlow<DialogueAnalysis?>(null)
    val currentDialogueAnalysis: StateFlow<DialogueAnalysis?> = _currentDialogueAnalysis.asStateFlow()

    val stylePreferences: StateFlow<StylePreferences>
    val ragSnippets: StateFlow<List<RagKnowledgeSnippet>>

    val suggestionFeedbackHistory: StateFlow<List<com.intellimate.intellimate.data.local.model.SuggestionFeedback>>
    val contextHistory: StateFlow<List<ContextEntry>>
    val highPotentialMatches: StateFlow<List<com.intellimate.intellimate.data.local.model.PotentialMatch>> // Added

    init {
        Log.d(TAG_MAIN_VM, "Initializing MainViewModel...")
        val appDb = AppDatabase.getDatabase(application)

        stylePreferencesRepository = StylePreferencesRepository(application)
        stylePreferences = stylePreferencesRepository.stylePreferencesFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StylePreferences(
                formality = com.intellimate.intellimate.data.repository.StyleDefaults.FORMALITY,
                humorLevel = com.intellimate.intellimate.data.repository.StyleDefaults.HUMOR_LEVEL,
                enthusiasm = com.intellimate.intellimate.data.repository.StyleDefaults.ENTHUSIASM
            )
        )
        Log.i(TAG_MAIN_VM, "StylePreferencesRepository initialized.")

        ragRepository = RagRepository(appDb.ragKnowledgeSnippetDao())
        ragSnippets = ragRepository.getAllSnippetsFlow().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        Log.i(TAG_MAIN_VM, "RagRepository initialized.")

        aiModelService = SimulatedAiModelService(ragRepository)
        Log.i(TAG_MAIN_VM, "SimulatedAiModelService initialized.")

        contextRepository = ContextRepository(appDb.contextEntryDao())
        contextHistory = contextRepository.getLatestEntriesFlow(20).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        Log.i(TAG_MAIN_VM, "ContextRepository initialized.")

        feedbackRepository = FeedbackRepository(appDb.suggestionFeedbackDao())
        suggestionFeedbackHistory = feedbackRepository.getAllFeedbackFlow().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        Log.i(TAG_MAIN_VM, "FeedbackRepository initialized.")

        aiAnalysisRepository = com.intellimate.intellimate.data.repository.AiAnalysisRepository(appDb.aiInteractionAnalysisDao())
        Log.i(TAG_MAIN_VM, "AiAnalysisRepository initialized.")

        potentialMatchRepository = com.intellimate.intellimate.data.repository.PotentialMatchRepository(appDb.potentialMatchDao()) // Initialize
        highPotentialMatches = potentialMatchRepository.getAllMatchesFlow().stateIn( // Expose flow
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        Log.i(TAG_MAIN_VM, "PotentialMatchRepository initialized and highPotentialMatches flow exposed.")
    }

    fun fetchDemoSuggestion(sampleText: String) {
        Log.i(TAG_MAIN_VM, "fetchDemoSuggestion called with sampleText: '${sampleText.take(30)}...'")
        viewModelScope.launch {
            _isLoading.value = true
            _suggestion.value = "Loading..."
            _currentDialogueAnalysis.value = null
            try {
                val currentStyle = stylePreferences.value
                Log.d(TAG_MAIN_VM, "Using style for suggestion: Formality=${currentStyle.formality}, Humor=${currentStyle.humorLevel}, Enthusiasm=${currentStyle.enthusiasm}")

                val suggestionWithAnalysis = aiModelService.getSuggestion(
                    inputText = sampleText,
                    imageContext = null,
                    formality = currentStyle.formality,
                    humorLevel = currentStyle.humorLevel,
                    enthusiasm = currentStyle.enthusiasm
                )

                _suggestion.value = suggestionWithAnalysis.suggestion
                _currentDialogueAnalysis.value = suggestionWithAnalysis.analysis
                SuggestionRepository.postLastAiOutput(suggestionWithAnalysis)
                Log.i(TAG_MAIN_VM, "Successfully fetched suggestion: '${suggestionWithAnalysis.suggestion.take(50)}...'")
                Log.i(TAG_MAIN_VM, "With Analysis: Emotion='${suggestionWithAnalysis.analysis?.emotion?.primaryEmotion}', Intent='${suggestionWithAnalysis.analysis?.intent?.primaryIntent}', Tip: '${suggestionWithAnalysis.coachingTip}'")

                // Save the AI Interaction Analysis
                val aiInteraction = com.intellimate.intellimate.data.local.model.AiInteractionAnalysis(
                    suggestionTimestamp = System.currentTimeMillis(), // Or a more specific timestamp if available from AI service
                    suggestionTextUsed = suggestionWithAnalysis.suggestion,
                    primaryEmotion = suggestionWithAnalysis.analysis?.emotion?.primaryEmotion,
                    emotionIntensity = suggestionWithAnalysis.analysis?.emotion?.intensity,
                    primaryIntent = suggestionWithAnalysis.analysis?.intent?.primaryIntent,
                    intentConfidence = suggestionWithAnalysis.analysis?.intent?.confidence,
                    keywords = suggestionWithAnalysis.analysis?.semantics?.keywords?.joinToString(", "),
                    semanticSummary = suggestionWithAnalysis.analysis?.semantics?.summary,
                    coachingTipProvided = suggestionWithAnalysis.coachingTip,
                    explanationProvided = suggestionWithAnalysis.explanation
                    // relatedContextEntryId could be set if we had a direct link to user input that triggered this
                )
                aiAnalysisRepository.saveAnalysis(aiInteraction)
                Log.i(TAG_MAIN_VM, "Saved AiInteractionAnalysis for suggestion: '${suggestionWithAnalysis.suggestion.take(30)}...'")

            } catch (e: Exception) {
                _suggestion.value = "Error: ${e.message}"
                _currentDialogueAnalysis.value = null
                SuggestionRepository.postLastAiOutput(null)
                Log.e(TAG_MAIN_VM, "Error fetching demo suggestion for text: '$sampleText'", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deletePotentialMatch(profileId: String) {
        viewModelScope.launch {
            try {
                potentialMatchRepository.deleteMatchById(profileId)
                Log.i(TAG_MAIN_VM, "Potential match deleted: $profileId")
                // Flow will automatically update the UI
            } catch (e: Exception) {
                Log.e(TAG_MAIN_VM, "Error deleting potential match: $profileId", e)
            }
        }
    }

    fun clearAllPotentialMatches() {
        viewModelScope.launch {
            try {
                potentialMatchRepository.clearAllMatches()
                Log.i(TAG_MAIN_VM, "All potential matches cleared.")
                // Flow will automatically update the UI
            } catch (e: Exception) {
                Log.e(TAG_MAIN_VM, "Error clearing all potential matches.", e)
            }
        }
    }

    fun loadLatestContextEntries(limit: Int = 10) {
        Log.i(TAG_MAIN_VM, "loadLatestContextEntries called with limit: $limit. This method is deprecated, use contextHistory flow directly.")
    }

    fun clearAllContextEntries() {
        Log.i(TAG_MAIN_VM, "clearAllContextEntries called.")
        viewModelScope.launch {
            try {
                contextRepository.clearAllEntries()
                Log.i(TAG_MAIN_VM, "Successfully cleared all context entries.")
            } catch (e: Exception) {
                Log.e(TAG_MAIN_VM, "Error clearing context entries.", e)
            }
        }
    }

    fun addRagSnippet(text: String) {
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        Log.i(TAG_MAIN_VM, "FeedbackRepository initialized and suggestionFeedbackHistory flow exposed.")
    }

    fun fetchDemoSuggestion(sampleText: String) {
        Log.i(TAG_MAIN_VM, "fetchDemoSuggestion called with sampleText: '${sampleText.take(30)}...'")
        viewModelScope.launch {
            _isLoading.value = true
            _suggestion.value = "Loading..."
            _currentDialogueAnalysis.value = null
            try {
                val currentStyle = stylePreferences.value // Get current style prefs
                Log.d(TAG_MAIN_VM, "Using style for suggestion: Formality=${currentStyle.formality}, Humor=${currentStyle.humorLevel}, Enthusiasm=${currentStyle.enthusiasm}")

                val suggestionWithAnalysis = aiModelService.getSuggestion(
                    inputText = sampleText,
                    imageContext = null, // Not used in this simulation
                    formality = currentStyle.formality,
                    humorLevel = currentStyle.humorLevel,
                    enthusiasm = currentStyle.enthusiasm,
                    strategy = currentStyle.datingStrategy // Pass the strategy
                )

                _suggestion.value = suggestionWithAnalysis.suggestion
                _currentDialogueAnalysis.value = suggestionWithAnalysis.analysis
                SuggestionRepository.postLastAiOutput(suggestionWithAnalysis) // Post the whole object
                Log.i(TAG_MAIN_VM, "Successfully fetched suggestion: '${suggestionWithAnalysis.suggestion.take(50)}...'")
                Log.i(TAG_MAIN_VM, "With Analysis: Emotion='${suggestionWithAnalysis.analysis?.emotion?.primaryEmotion}', Intent='${suggestionWithAnalysis.analysis?.intent?.primaryIntent}', Tip: '${suggestionWithAnalysis.coachingTip}'")
            } catch (e: Exception) {
                _suggestion.value = "Error: ${e.message}"
                _currentDialogueAnalysis.value = null
                SuggestionRepository.postLastAiOutput(null) // Post null on error
                Log.e(TAG_MAIN_VM, "Error fetching demo suggestion for text: '$sampleText'", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addRagSnippet(text: String) {
        Log.i(TAG_MAIN_VM, "addRagSnippet called with text: '${text.take(50)}...'")
        viewModelScope.launch {
            try {
                ragRepository.addSnippet(text)
                Log.i(TAG_MAIN_VM, "RAG snippet added successfully.")
                // No need to manually refresh ragSnippets as it's a Flow collected by stateIn
            } catch (e: Exception) {
                Log.e(TAG_MAIN_VM, "Error adding RAG snippet.", e)
            }
        }
    }

    fun loadLatestContextEntries(limit: Int = 10) {
        Log.i(TAG_MAIN_VM, "loadLatestContextEntries called with limit: $limit. This method is deprecated, use contextHistory flow directly.")
        // This method could be kept for explicit refresh, but the flow should update automatically.
        // For explicit refresh, you might re-trigger the flow or use a different mechanism.
        // For now, we'll log its deprecation as contextHistory is a self-updating flow.
        // viewModelScope.launch {
        //     try {
        //         // _contextEntries.value = contextRepository.getLatestEntries(limit) // Old way
        //         Log.i(TAG_MAIN_VM, "Context history is now collected via a flow directly.")
        //     } catch (e: Exception) {
        //         Log.e(TAG_MAIN_VM, "Error explicitly loading context entries with limit: $limit", e)
        //     }
        // }
    }

    fun clearAllContextEntries() {
        Log.i(TAG_MAIN_VM, "clearAllContextEntries called.")
        viewModelScope.launch {
            try {
                contextRepository.clearAllEntries()
                Log.i(TAG_MAIN_VM, "Successfully cleared all context entries.")
                // contextHistory flow will update automatically
            } catch (e: Exception) {
                Log.e(TAG_MAIN_VM, "Error clearing context entries.", e)
            }
        }
    }
}
