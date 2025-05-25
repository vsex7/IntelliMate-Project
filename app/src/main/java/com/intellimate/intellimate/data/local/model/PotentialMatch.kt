package com.intellimate.intellimate.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "potential_matches")
data class PotentialMatch(
    @PrimaryKey val profileId: String, // e.g., sourceAppPackage + "_" + userGivenName.hashCode() + System.currentTimeMillis()
    val userGivenName: String,
    val sourceAppPackage: String,
    val strategyWhenSaved: String,
    val potentialScore: Int?, // e.g., opennessScore or other future heuristic
    val notes: String?,
    val savedTimestamp: Long
)
