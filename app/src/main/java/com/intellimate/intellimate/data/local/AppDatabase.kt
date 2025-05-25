package com.intellimate.intellimate.data.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.intellimate.intellimate.data.local.dao.ContextEntryDao
import com.intellimate.intellimate.data.local.dao.SuggestionFeedbackDao
import com.intellimate.intellimate.data.local.dao.RagKnowledgeSnippetDao
import com.intellimate.intellimate.data.local.dao.AiInteractionAnalysisDao
import com.intellimate.intellimate.data.local.dao.PotentialMatchDao // Import new PotentialMatch DAO
import com.intellimate.intellimate.data.local.model.ContextEntry
import com.intellimate.intellimate.data.local.model.SuggestionFeedback
import com.intellimate.intellimate.data.local.model.RagKnowledgeSnippet
import com.intellimate.intellimate.data.local.model.AiInteractionAnalysis
import com.intellimate.intellimate.data.local.model.PotentialMatch // Import new PotentialMatch Entity

@Database(
    entities = [
        ContextEntry::class,
        SuggestionFeedback::class,
        RagKnowledgeSnippet::class,
        AiInteractionAnalysis::class,
        PotentialMatch::class // Add PotentialMatch
    ],
    version = 6, // Incremented version
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun contextEntryDao(): ContextEntryDao
    abstract fun suggestionFeedbackDao(): SuggestionFeedbackDao
    abstract fun ragKnowledgeSnippetDao(): RagKnowledgeSnippetDao
    abstract fun aiInteractionAnalysisDao(): AiInteractionAnalysisDao
    abstract fun potentialMatchDao(): PotentialMatchDao // Add abstract fun for new PotentialMatch DAO

    companion object {
        private const val TAG_APP_DB = "IntelliMateAppDB" // Standardized TAG

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                Log.d(TAG_APP_DB, "getDatabase: Returning existing instance.")
                return tempInstance
            }
            synchronized(this) {
                Log.d(TAG_APP_DB, "getDatabase: Existing instance not found, creating new one (synchronized).")
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "intellimate_database" // Database name
                )
                // Wipes and rebuilds instead of migrating if no Migration object.
                .fallbackToDestructiveMigration() // Ensures schema changes are handled by rebuilding
                .build()
                INSTANCE = instance
                Log.i(TAG_APP_DB, "getDatabase: New database instance created and assigned.")
                return instance
            }
        }
    }
}
