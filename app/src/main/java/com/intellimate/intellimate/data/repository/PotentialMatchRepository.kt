package com.intellimate.intellimate.data.repository

import android.util.Log
import com.intellimate.intellimate.data.local.dao.PotentialMatchDao
import com.intellimate.intellimate.data.local.model.PotentialMatch
import kotlinx.coroutines.flow.Flow

class PotentialMatchRepository(private val potentialMatchDao: PotentialMatchDao) {

    companion object {
        private const val TAG_POTENTIAL_MATCH_REPO = "IntelliMatePotMatchRepo"
    }

    suspend fun saveMatch(match: PotentialMatch) {
        Log.i(TAG_POTENTIAL_MATCH_REPO, "saveMatch called for profileId: ${match.profileId}, name: ${match.userGivenName}")
        try {
            potentialMatchDao.insert(match)
            Log.d(TAG_POTENTIAL_MATCH_REPO, "PotentialMatch inserted successfully: ${match.profileId}")
        } catch (e: Exception) {
            Log.e(TAG_POTENTIAL_MATCH_REPO, "Error inserting PotentialMatch: ${match.profileId}", e)
        }
    }

    fun getAllMatchesFlow(): Flow<List<PotentialMatch>> {
        Log.d(TAG_POTENTIAL_MATCH_REPO, "getAllMatchesFlow called.")
        return potentialMatchDao.getAll()
    }

    suspend fun deleteMatchById(profileId: String) {
        Log.i(TAG_POTENTIAL_MATCH_REPO, "deleteMatchById called for profileId: $profileId")
        try {
            potentialMatchDao.deleteById(profileId)
            Log.d(TAG_POTENTIAL_MATCH_REPO, "PotentialMatch deleted successfully: $profileId")
        } catch (e: Exception) {
            Log.e(TAG_POTENTIAL_MATCH_REPO, "Error deleting PotentialMatch: $profileId", e)
        }
    }

    suspend fun clearAllMatches() {
        Log.i(TAG_POTENTIAL_MATCH_REPO, "clearAllMatches called.")
        try {
            potentialMatchDao.clearAll()
            Log.d(TAG_POTENTIAL_MATCH_REPO, "All PotentialMatches cleared successfully.")
        } catch (e: Exception) {
            Log.e(TAG_POTENTIAL_MATCH_REPO, "Error clearing all PotentialMatches.", e)
        }
    }
}
