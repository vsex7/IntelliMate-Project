package com.intellimate.intellimate.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// Define DataStore instance at the top level
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "style_settings")

// Define default values
import com.intellimate.intellimate.core.model.DatingStrategy // Import DatingStrategy

// Define DataStore instance at the top level
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "style_settings")

// Define default values
object StyleDefaults {
// Define DataStore instance at the top level
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "style_settings")

// Define default values
object StyleDefaults {
    const val FORMALITY = "Medium"
    const val HUMOR_LEVEL = 0.3f
    const val ENTHUSIASM = "Neutral"
    val DATING_STRATEGY = DatingStrategy.BALANCED
    const val VOICE_ASSIST_MODE_ENABLED = false // Default for new preference
}

data class StylePreferences(
    val formality: String,
    val humorLevel: Float,
    val enthusiasm: String,
    val datingStrategy: DatingStrategy,
    val voiceAssistModeEnabled: Boolean // Added voice assist mode
)

class StylePreferencesRepository(private val context: Context) {

    companion object {
        private const val TAG_STYLE_REPO = "IntelliMateStyleRepo"
        private val KEY_FORMALITY = stringPreferencesKey("style_formality")
        private val KEY_HUMOR_LEVEL = floatPreferencesKey("style_humor_level")
        private val KEY_ENTHUSIASM = stringPreferencesKey("style_enthusiasm")
        private val KEY_DATING_STRATEGY = stringPreferencesKey("dating_strategy")
        private val KEY_VOICE_ASSIST_MODE_ENABLED = booleanPreferencesKey("voice_assist_mode_enabled") // New key
    }

    val stylePreferencesFlow: Flow<StylePreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG_STYLE_REPO, "Error reading style preferences.", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val formality = preferences[KEY_FORMALITY] ?: StyleDefaults.FORMALITY
            val humorLevel = preferences[KEY_HUMOR_LEVEL] ?: StyleDefaults.HUMOR_LEVEL
            val enthusiasm = preferences[KEY_ENTHUSIASM] ?: StyleDefaults.ENTHUSIASM
            val datingStrategy = DatingStrategy.fromString(preferences[KEY_DATING_STRATEGY])
            val voiceAssistEnabled = preferences[KEY_VOICE_ASSIST_MODE_ENABLED] ?: StyleDefaults.VOICE_ASSIST_MODE_ENABLED
            StylePreferences(formality, humorLevel, enthusiasm, datingStrategy, voiceAssistEnabled)
        }

    suspend fun updateFormality(formality: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_FORMALITY] = formality
        }
        Log.d(TAG_STYLE_REPO, "Formality updated to: $formality")
    }

    suspend fun updateHumorLevel(humorLevel: Float) {
        context.dataStore.edit { preferences ->
            preferences[KEY_HUMOR_LEVEL] = humorLevel
        }
        Log.d(TAG_STYLE_REPO, "Humor level updated to: $humorLevel")
    }

    suspend fun updateEnthusiasm(enthusiasm: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ENTHUSIASM] = enthusiasm
        }
        Log.d(TAG_STYLE_REPO, "Enthusiasm updated to: $enthusiasm")
    }

    suspend fun updateDatingStrategy(strategy: DatingStrategy) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DATING_STRATEGY] = strategy.name // Store enum name as string
        }
        Log.d(TAG_STYLE_REPO, "Dating strategy updated to: ${strategy.name}")
    }

    suspend fun updateVoiceAssistMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_VOICE_ASSIST_MODE_ENABLED] = enabled
        }
        Log.d(TAG_STYLE_REPO, "Voice Assist Mode updated to: $enabled")
    }
}
