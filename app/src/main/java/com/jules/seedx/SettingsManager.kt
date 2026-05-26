package com.jules.seedx

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {
    companion object {
        val SEED_KEY = stringPreferencesKey("seed")
        val RANGE_KEY = stringPreferencesKey("range")
        val CENTER_X_KEY = stringPreferencesKey("center_x")
        val CENTER_Z_KEY = stringPreferencesKey("center_z")
        val MAX_DIST_KEY = stringPreferencesKey("max_dist")
        val MIN_COUNT_KEY = stringPreferencesKey("min_count")
        val STRUCTURE_TYPE_KEY = stringPreferencesKey("structure_type")
    }

    val seed: Flow<String> = context.dataStore.data.map { it[SEED_KEY] ?: "" }
    val range: Flow<String> = context.dataStore.data.map { it[RANGE_KEY] ?: "10000" }
    val centerX: Flow<String> = context.dataStore.data.map { it[CENTER_X_KEY] ?: "0" }
    val centerZ: Flow<String> = context.dataStore.data.map { it[CENTER_Z_KEY] ?: "0" }
    val maxDist: Flow<String> = context.dataStore.data.map { it[MAX_DIST_KEY] ?: "64" }
    val minCount: Flow<String> = context.dataStore.data.map { it[MIN_COUNT_KEY] ?: "4" }
    val structureType: Flow<String> = context.dataStore.data.map { it[STRUCTURE_TYPE_KEY] ?: StructureType.SWAMP_HUT.name }

    suspend fun saveSettings(
        seed: String,
        range: String,
        centerX: String,
        centerZ: String,
        maxDist: String,
        minCount: String,
        type: String
    ) {
        context.dataStore.edit {
            it[SEED_KEY] = seed
            it[RANGE_KEY] = range
            it[CENTER_X_KEY] = centerX
            it[CENTER_Z_KEY] = centerZ
            it[MAX_DIST_KEY] = maxDist
            it[MIN_COUNT_KEY] = minCount
            it[STRUCTURE_TYPE_KEY] = type
        }
    }
}
