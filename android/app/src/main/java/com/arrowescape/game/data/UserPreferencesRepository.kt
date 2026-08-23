package com.arrowescape.game.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

data class UserPreferences(
    val unlockedLevel: Int,
    val completedLevels: Set<Int>,
    val earnedRupees: Int,
    val hintsRemaining: Int,
    val soundEnabled: Boolean
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val UNLOCKED_LEVEL = intPreferencesKey("unlocked_level")
        val COMPLETED_LEVELS = stringSetPreferencesKey("completed_levels")
        val EARNED_RUPEES = intPreferencesKey("earned_rupees")
        val HINTS_REMAINING = intPreferencesKey("hints_remaining")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .map { prefs ->
            val unlocked = prefs[Keys.UNLOCKED_LEVEL] ?: 1
            val completedSet = prefs[Keys.COMPLETED_LEVELS]?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
            val earned = prefs[Keys.EARNED_RUPEES] ?: 0
            val hints = prefs[Keys.HINTS_REMAINING] ?: 3
            val sound = prefs[Keys.SOUND_ENABLED] ?: true

            UserPreferences(
                unlockedLevel = unlocked,
                completedLevels = completedSet,
                earnedRupees = earned,
                hintsRemaining = hints,
                soundEnabled = sound
            )
        }

    suspend fun recordLevelCompleted(levelId: Int, rewardRupees: Int) {
        context.dataStore.edit { prefs ->
            val currentCompleted = prefs[Keys.COMPLETED_LEVELS] ?: emptySet()
            val isFirstTime = !currentCompleted.contains(levelId.toString())

            prefs[Keys.COMPLETED_LEVELS] = currentCompleted + levelId.toString()

            val currentUnlocked = prefs[Keys.UNLOCKED_LEVEL] ?: 1
            prefs[Keys.UNLOCKED_LEVEL] = maxOf(currentUnlocked, minOf(20, levelId + 1))

            if (isFirstTime) {
                val currentRupees = prefs[Keys.EARNED_RUPEES] ?: 0
                prefs[Keys.EARNED_RUPEES] = currentRupees + rewardRupees
            }
        }
    }

    suspend fun decrementHint() {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.HINTS_REMAINING] ?: 3
            prefs[Keys.HINTS_REMAINING] = maxOf(0, current - 1)
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SOUND_ENABLED] = enabled
        }
    }

    suspend fun resetAllProgress() {
        context.dataStore.edit { prefs ->
            prefs[Keys.UNLOCKED_LEVEL] = 1
            prefs[Keys.COMPLETED_LEVELS] = emptySet()
            prefs[Keys.EARNED_RUPEES] = 0
            prefs[Keys.HINTS_REMAINING] = 3
            prefs[Keys.SOUND_ENABLED] = true
        }
    }
}
