package com.arrowescape.game.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.arrowescape.game.model.EarningTransaction
import com.arrowescape.game.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

data class UserPreferences(
    val unlockedLevel: Int,
    val completedLevels: Set<Int>,
    val walletBalance: Double,
    val totalEarnings: Double,
    val hintsRemaining: Int,
    val soundEnabled: Boolean,
    val musicEnabled: Boolean,
    val hapticsEnabled: Boolean,
    val dailyStreak: Int,
    val lastDailyRewardTimestamp: Long,
    val username: String,
    val earningHistory: List<EarningTransaction>
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val UNLOCKED_LEVEL = intPreferencesKey("unlocked_level")
        val COMPLETED_LEVELS = stringSetPreferencesKey("completed_levels")
        val WALLET_BALANCE = doublePreferencesKey("wallet_balance")
        val TOTAL_EARNINGS = doublePreferencesKey("total_earnings")
        val HINTS_REMAINING = intPreferencesKey("hints_remaining")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val DAILY_STREAK = intPreferencesKey("daily_streak")
        val LAST_DAILY_TIMESTAMP = longPreferencesKey("last_daily_timestamp")
        val USERNAME = stringPreferencesKey("username")
        val TRANSACTIONS_JSON = stringPreferencesKey("transactions_json")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .map { prefs ->
            val unlocked = prefs[Keys.UNLOCKED_LEVEL] ?: 1
            val completedSet = prefs[Keys.COMPLETED_LEVELS]?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
            val wallet = prefs[Keys.WALLET_BALANCE] ?: 0.0
            val total = prefs[Keys.TOTAL_EARNINGS] ?: 0.0
            val hints = prefs[Keys.HINTS_REMAINING] ?: 3
            val sound = prefs[Keys.SOUND_ENABLED] ?: true
            val music = prefs[Keys.MUSIC_ENABLED] ?: false
            val haptics = prefs[Keys.HAPTICS_ENABLED] ?: true
            val streak = prefs[Keys.DAILY_STREAK] ?: 0
            val lastDaily = prefs[Keys.LAST_DAILY_TIMESTAMP] ?: 0L
            val uname = prefs[Keys.USERNAME] ?: "PlayerOne"
            val txJson = prefs[Keys.TRANSACTIONS_JSON] ?: "[]"
            val history = parseTransactions(txJson)

            UserPreferences(
                unlockedLevel = unlocked,
                completedLevels = completedSet,
                walletBalance = wallet,
                totalEarnings = total,
                hintsRemaining = hints,
                soundEnabled = sound,
                musicEnabled = music,
                hapticsEnabled = haptics,
                dailyStreak = streak,
                lastDailyRewardTimestamp = lastDaily,
                username = uname,
                earningHistory = history
            )
        }

    suspend fun recordLevelCompleted(levelId: Int, rewardRupees: Double): Boolean {
        var isFirstTime = false
        context.dataStore.edit { prefs ->
            val currentCompleted = prefs[Keys.COMPLETED_LEVELS] ?: emptySet()
            isFirstTime = !currentCompleted.contains(levelId.toString())

            prefs[Keys.COMPLETED_LEVELS] = currentCompleted + levelId.toString()

            val currentUnlocked = prefs[Keys.UNLOCKED_LEVEL] ?: 1
            prefs[Keys.UNLOCKED_LEVEL] = maxOf(currentUnlocked, levelId + 1)

            if (isFirstTime) {
                val currentWallet = prefs[Keys.WALLET_BALANCE] ?: 0.0
                val currentTotal = prefs[Keys.TOTAL_EARNINGS] ?: 0.0

                prefs[Keys.WALLET_BALANCE] = currentWallet + rewardRupees
                prefs[Keys.TOTAL_EARNINGS] = currentTotal + rewardRupees

                val history = parseTransactions(prefs[Keys.TRANSACTIONS_JSON] ?: "[]").toMutableList()
                history.add(
                    0,
                    EarningTransaction(
                        id = "tx_lvl_${levelId}_${System.currentTimeMillis()}",
                        title = "Level $levelId Solved",
                        amount = rewardRupees,
                        timestamp = System.currentTimeMillis(),
                        type = TransactionType.LEVEL_REWARD,
                        levelId = levelId,
                        status = "SUCCESS"
                    )
                )
                prefs[Keys.TRANSACTIONS_JSON] = serializeTransactions(history)
            }
        }
        return isFirstTime
    }

    suspend fun claimDailyReward(rewardAmount: Double, newStreak: Int) {
        context.dataStore.edit { prefs ->
            val currentWallet = prefs[Keys.WALLET_BALANCE] ?: 0.0
            val currentTotal = prefs[Keys.TOTAL_EARNINGS] ?: 0.0

            prefs[Keys.WALLET_BALANCE] = currentWallet + rewardAmount
            prefs[Keys.TOTAL_EARNINGS] = currentTotal + rewardAmount
            prefs[Keys.DAILY_STREAK] = newStreak
            prefs[Keys.LAST_DAILY_TIMESTAMP] = System.currentTimeMillis()

            val history = parseTransactions(prefs[Keys.TRANSACTIONS_JSON] ?: "[]").toMutableList()
            history.add(
                0,
                EarningTransaction(
                    id = "tx_daily_${System.currentTimeMillis()}",
                    title = "Day $newStreak Daily Bonus",
                    amount = rewardAmount,
                    timestamp = System.currentTimeMillis(),
                    type = TransactionType.DAILY_REWARD,
                    status = "SUCCESS"
                )
            )
            prefs[Keys.TRANSACTIONS_JSON] = serializeTransactions(history)
        }
    }

    suspend fun requestWithdrawal(amount: Double, upiId: String): Boolean {
        var success = false
        context.dataStore.edit { prefs ->
            val currentWallet = prefs[Keys.WALLET_BALANCE] ?: 0.0
            if (currentWallet >= amount && amount >= 50.0) {
                prefs[Keys.WALLET_BALANCE] = currentWallet - amount

                val history = parseTransactions(prefs[Keys.TRANSACTIONS_JSON] ?: "[]").toMutableList()
                history.add(
                    0,
                    EarningTransaction(
                        id = "tx_wdr_${UUID.randomUUID().toString().take(8)}",
                        title = "UPI Withdrawal ($upiId)",
                        amount = amount,
                        timestamp = System.currentTimeMillis(),
                        type = TransactionType.WITHDRAWAL,
                        status = "PROCESSING"
                    )
                )
                prefs[Keys.TRANSACTIONS_JSON] = serializeTransactions(history)
                success = true
            }
        }
        return success
    }

    suspend fun addHint() {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.HINTS_REMAINING] ?: 3
            prefs[Keys.HINTS_REMAINING] = current + 1
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

    suspend fun setMusicEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.MUSIC_ENABLED] = enabled
        }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.HAPTICS_ENABLED] = enabled
        }
    }

    suspend fun setUsername(name: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.USERNAME] = name
        }
    }

    suspend fun resetAllProgress() {
        context.dataStore.edit { prefs ->
            prefs[Keys.UNLOCKED_LEVEL] = 1
            prefs[Keys.COMPLETED_LEVELS] = emptySet()
            prefs[Keys.WALLET_BALANCE] = 0.0
            prefs[Keys.TOTAL_EARNINGS] = 0.0
            prefs[Keys.HINTS_REMAINING] = 3
            prefs[Keys.SOUND_ENABLED] = true
            prefs[Keys.MUSIC_ENABLED] = false
            prefs[Keys.HAPTICS_ENABLED] = true
            prefs[Keys.DAILY_STREAK] = 0
            prefs[Keys.LAST_DAILY_TIMESTAMP] = 0L
            prefs[Keys.TRANSACTIONS_JSON] = "[]"
        }
    }

    private fun parseTransactions(jsonStr: String): List<EarningTransaction> {
        val list = mutableListOf<EarningTransaction>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    EarningTransaction(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        amount = obj.getDouble("amount"),
                        timestamp = obj.getLong("timestamp"),
                        type = TransactionType.valueOf(obj.getString("type")),
                        levelId = if (obj.has("levelId") && !obj.isNull("levelId")) obj.getInt("levelId") else null,
                        status = obj.optString("status", "SUCCESS")
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    private fun serializeTransactions(list: List<EarningTransaction>): String {
        val jsonArray = JSONArray()
        for (tx in list.take(50)) { // keep last 50 transactions
            val obj = JSONObject()
            obj.put("id", tx.id)
            obj.put("title", tx.title)
            obj.put("amount", tx.amount)
            obj.put("timestamp", tx.timestamp)
            obj.put("type", tx.type.name)
            tx.levelId?.let { obj.put("levelId", it) }
            obj.put("status", tx.status)
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }
}
