package com.intellimate.intellimate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.intellimate.intellimate.data.local.model.PotentialMatch
import kotlinx.coroutines.flow.Flow

@Dao
interface PotentialMatchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(match: PotentialMatch)

    @Query("SELECT * FROM potential_matches ORDER BY savedTimestamp DESC")
    fun getAll(): Flow<List<PotentialMatch>>

    @Query("DELETE FROM potential_matches WHERE profileId = :profileId")
    suspend fun deleteById(profileId: String)

    @Query("DELETE FROM potential_matches")
    suspend fun clearAll()
}
