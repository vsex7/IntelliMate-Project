package com.intellimate.intellimate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.intellimate.intellimate.data.local.model.ContextEntry

@Dao
interface ContextEntryDao {

    @Insert
    suspend fun insert(entry: ContextEntry)

    @Query("SELECT * FROM context_entries ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLatestEntries(limit: Int): List<ContextEntry> // Keep for suspend needs

    @Query("SELECT * FROM context_entries ORDER BY timestamp DESC LIMIT :limit")
    fun getLatestEntriesFlow(limit: Int): Flow<List<ContextEntry>> // New Flow based method

    @Query("DELETE FROM context_entries")
    suspend fun clearAll()
}
