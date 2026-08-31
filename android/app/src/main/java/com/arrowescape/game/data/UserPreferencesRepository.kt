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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

data class WithdrawalResult(
    val success: Boolean,
    val message: String,
    val withdrawalId: String? = null,
    val amount: Double = 0.0,
    val maskedUpi: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "SUBMITTED"
)

data class UserPreferences(
    val unlockedLevel: Int,
    val completedLevels: Set<Int>,
    val levelStars: Map<Int, Int>,
    val walletBalance: Double,
    val totalEarnings: Double,
    val hintsRemaining: Int,
    val soundEnabled: Boolean,
    val musicEnabled: Boolean,
    val hapticsEnabled: Boolean,
    val dailyStreak: Int,
    val lastDailyRewardTimestamp: Long,
    val displayName: String,
    val username: String,
    val uid: String,
    val upiId: String,
    val referralCode: String,
    val isRegistered: Boolean,
    val earningHistory: List<EarningTransaction>
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val UNLOCKED_LEVEL = intPreferencesKey("unlocked_level")
        val COMPLETED_LEVELS = stringSetPreferencesKey("completed_levels")
        val LEVEL_STARS_JSON = stringPreferencesKey("level_stars_json")
        val WALLET_BALANCE = doublePreferencesKey("wallet_balance")
        val TOTAL_EARNINGS = doublePreferencesKey("total_earnings")
        val HINTS_REMAINING = intPreferencesKey("hints_remaining")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val DAILY_STREAK = intPreferencesKey("daily_streak")
        val LAST_DAILY_TIMESTAMP = longPreferencesKey("last_daily_timestamp")
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val USERNAME = stringPreferencesKey("username")
        val UID = stringPreferencesKey("uid")
        val UPI_ID = stringPreferencesKey("upi_id")
        val REFERRAL_CODE = stringPreferencesKey("referral_code")
        val IS_REGISTERED = booleanPreferencesKey("is_registered")
        val PASSWORD_SALT = stringPreferencesKey("password_salt")
        val PASSWORD_HASH = stringPreferencesKey("password_hash")
        val TRANSACTIONS_JSON = stringPreferencesKey("transactions_json")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .map { prefs ->
            val unlocked = prefs[Keys.UNLOCKED_LEVEL] ?: 1
            val completedSet = prefs[Keys.COMPLETED_LEVELS]?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
            val starsJson = prefs[Keys.LEVEL_STARS_JSON] ?: "{}"
            val starsMap = parseLevelStars(starsJson)
            val wallet = prefs[Keys.WALLET_BALANCE] ?: 0.0
            val total = prefs[Keys.TOTAL_EARNINGS] ?: 0.0
            val hints = prefs[Keys.HINTS_REMAINING] ?: 3
            val sound = prefs[Keys.SOUND_ENABLED] ?: true
            val music = prefs[Keys.MUSIC_ENABLED] ?: false
            val haptics = prefs[Keys.HAPTICS_ENABLED] ?: true
            val streak = prefs[Keys.DAILY_STREAK] ?: 0
            val lastDaily = prefs[Keys.LAST_DAILY_TIMESTAMP] ?: 0L
            val dispName = prefs[Keys.DISPLAY_NAME] ?: "Player One"
            val uname = prefs[Keys.USERNAME] ?: "player_0590"
            val userUid = prefs[Keys.UID] ?: "AE-0590-7812"
            val upi = prefs[Keys.UPI_ID] ?: ""
            val refCode = prefs[Keys.REFERRAL_CODE] ?: "ARROW590"
            val registered = prefs[Keys.IS_REGISTERED] ?: false
            val txJson = prefs[Keys.TRANSACTIONS_JSON] ?: "[]"
            val history = parseTransactions(txJson)

            UserPreferences(
                unlockedLevel = unlocked,
                completedLevels = completedSet,
                levelStars = starsMap,
                walletBalance = wallet,
                totalEarnings = total,
                hintsRemaining = hints,
                soundEnabled = sound,
                musicEnabled = music,
                hapticsEnabled = haptics,
                dailyStreak = streak,
                lastDailyRewardTimestamp = lastDaily,
                displayName = dispName,
                username = uname,
                uid = userUid,
                upiId = upi,
                referralCode = refCode,
                isRegistered = registered,
                earningHistory = history
            )
        }

    suspend fun recordLevelCompleted(levelId: Int, rewardRupees: Double, earnedStars: Int = 3): Boolean {
        var isFirstTime = false
        context.dataStore.edit { prefs ->
            val currentCompleted = prefs[Keys.COMPLETED_LEVELS] ?: emptySet()
            isFirstTime = !currentCompleted.contains(levelId.toString())

            prefs[Keys.COMPLETED_LEVELS] = currentCompleted + levelId.toString()

            val currentUnlocked = prefs[Keys.UNLOCKED_LEVEL] ?: 1
            prefs[Keys.UNLOCKED_LEVEL] = maxOf(currentUnlocked, levelId + 1)

            // Persist star rating (never downgrade)
            val starsJson = prefs[Keys.LEVEL_STARS_JSON] ?: "{}"
            val starsMap = parseLevelStars(starsJson).toMutableMap()
            val prevStars = starsMap[levelId] ?: 0
            val newStars = maxOf(prevStars, earnedStars.coerceIn(1, 3))
            starsMap[levelId] = newStars
            prefs[Keys.LEVEL_STARS_JSON] = serializeLevelStars(starsMap)

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

    fun generateWithdrawalId(): String {
        val dateStr = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        val randomPart = UUID.randomUUID().toString().replace("-", "").take(8).uppercase(Locale.US)
        return "WD-$dateStr-$randomPart"
    }

    fun maskUpiId(upiId: String): String {
        val trimmed = upiId.trim()
        val atIndex = trimmed.indexOf('@')
        if (atIndex <= 0) return trimmed
        val handle = trimmed.substring(0, atIndex)
        val domain = trimmed.substring(atIndex)
        val visibleChars = if (handle.length <= 2) handle.take(1) else handle.take(2)
        return "$visibleChars***$domain"
    }

    suspend fun requestWithdrawal(
        amount: Double,
        upiId: String,
        providedWithdrawalId: String? = null
    ): WithdrawalResult {
        var result = WithdrawalResult(
            success = false,
            message = "Withdrawal could not be processed.",
            amount = amount,
            status = "FAILED"
        )

        val cleanUpi = upiId.trim()
        val maskedUpi = maskUpiId(cleanUpi)

        context.dataStore.edit { prefs ->
            val currentWallet = prefs[Keys.WALLET_BALANCE] ?: 0.0
            val history = parseTransactions(prefs[Keys.TRANSACTIONS_JSON] ?: "[]").toMutableList()

            // Idempotency check: if withdrawal ID already submitted, return existing record
            if (providedWithdrawalId != null) {
                val existing = history.firstOrNull { it.withdrawalId == providedWithdrawalId || it.id == providedWithdrawalId }
                if (existing != null) {
                    result = WithdrawalResult(
                        success = true,
                        message = "Existing withdrawal request found: ${existing.status}",
                        withdrawalId = existing.withdrawalId ?: existing.id,
                        amount = existing.amount,
                        maskedUpi = existing.maskedUpiId ?: maskedUpi,
                        timestamp = existing.timestamp,
                        status = existing.status
                    )
                    return@edit
                }
            }

            if (amount < 50.0) {
                result = WithdrawalResult(
                    success = false,
                    message = "Minimum withdrawal amount is ₹50.00",
                    amount = amount,
                    status = "FAILED"
                )
                return@edit
            }

            if (currentWallet < amount) {
                result = WithdrawalResult(
                    success = false,
                    message = "Insufficient wallet balance (₹${"%.2f".format(currentWallet)} available)",
                    amount = amount,
                    status = "FAILED"
                )
                return@edit
            }

            // Deduct balance and create new withdrawal record
            val finalWithdrawalId = providedWithdrawalId ?: generateWithdrawalId()
            prefs[Keys.WALLET_BALANCE] = currentWallet - amount

            val now = System.currentTimeMillis()
            val newTx = EarningTransaction(
                id = "tx_${finalWithdrawalId}",
                title = "UPI Withdrawal ($maskedUpi)",
                amount = amount,
                timestamp = now,
                type = TransactionType.WITHDRAWAL,
                status = "SUBMITTED",
                withdrawalId = finalWithdrawalId,
                maskedUpiId = maskedUpi
            )

            history.add(0, newTx)
            prefs[Keys.TRANSACTIONS_JSON] = serializeTransactions(history)

            result = WithdrawalResult(
                success = true,
                message = "Withdrawal request submitted successfully",
                withdrawalId = finalWithdrawalId,
                amount = amount,
                maskedUpi = maskedUpi,
                timestamp = now,
                status = "SUBMITTED"
            )
        }

        return result
    }

    suspend fun updateWithdrawalStatus(
        withdrawalId: String,
        newStatus: String
    ): Boolean {
        var transitioned = false
        var notificationPayload: WithdrawalNotificationPayload? = null

        context.dataStore.edit { prefs ->
            val history = parseTransactions(prefs[Keys.TRANSACTIONS_JSON] ?: "[]").toMutableList()
            val index = history.indexOfFirst { it.withdrawalId == withdrawalId || it.id == withdrawalId }
            if (index >= 0) {
                val currentTx = history[index]
                // Idempotency: A withdrawal can only transition to SUCCESSFUL once
                if (currentTx.status == "SUCCESSFUL" && (newStatus == "SUCCESSFUL" || newStatus == "SUCCESS")) {
                    return@edit
                }

                val updatedTx = currentTx.copy(status = newStatus)
                history[index] = updatedTx
                prefs[Keys.TRANSACTIONS_JSON] = serializeTransactions(history)
                transitioned = true

                if (newStatus == "SUCCESSFUL" || newStatus == "SUCCESS") {
                    val uid = prefs[Keys.UID] ?: "AE-0590-7812"
                    val username = prefs[Keys.USERNAME] ?: "player_0590"
                    notificationPayload = WithdrawalNotificationPayload(
                        withdrawalId = currentTx.withdrawalId ?: currentTx.id,
                        userId = uid,
                        username = username,
                        amount = currentTx.amount,
                        maskedUpiId = currentTx.maskedUpiId ?: maskUpiId(currentTx.title),
                        timestamp = currentTx.timestamp,
                        status = "SUCCESSFUL"
                    )
                }
            }
        }

        // Send admin notification if genuinely transitioned to SUCCESSFUL
        notificationPayload?.let { payload ->
            DefaultWithdrawalNotificationService.notifyWithdrawalSuccessful(payload)
        }

        return transitioned
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

    suspend fun updateProfile(displayName: String, username: String, upiId: String, isRegistered: Boolean = false) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DISPLAY_NAME] = displayName
            prefs[Keys.USERNAME] = username
            prefs[Keys.UPI_ID] = upiId
            if (isRegistered) {
                prefs[Keys.IS_REGISTERED] = true
            }
        }
    }

    suspend fun setUpiId(upiId: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.UPI_ID] = upiId
        }
    }

    suspend fun resetAllProgress() {
        context.dataStore.edit { prefs ->
            prefs[Keys.UNLOCKED_LEVEL] = 1
            prefs[Keys.COMPLETED_LEVELS] = emptySet()
            prefs[Keys.LEVEL_STARS_JSON] = "{}"
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

    private fun parseLevelStars(jsonStr: String): Map<Int, Int> {
        val map = mutableMapOf<Int, Int>()
        try {
            val jsonObject = JSONObject(jsonStr)
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val levelId = key.toIntOrNull()
                if (levelId != null) {
                    map[levelId] = jsonObject.optInt(key, 0)
                }
            }
        } catch (_: Exception) {}
        return map
    }

    private fun serializeLevelStars(map: Map<Int, Int>): String {
        val jsonObject = JSONObject()
        for ((levelId, stars) in map) {
            jsonObject.put(levelId.toString(), stars)
        }
        return jsonObject.toString()
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
                        status = obj.optString("status", "SUCCESS"),
                        withdrawalId = if (obj.has("withdrawalId") && !obj.isNull("withdrawalId")) obj.getString("withdrawalId") else null,
                        maskedUpiId = if (obj.has("maskedUpiId") && !obj.isNull("maskedUpiId")) obj.getString("maskedUpiId") else null
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
            tx.withdrawalId?.let { obj.put("withdrawalId", it) }
            tx.maskedUpiId?.let { obj.put("maskedUpiId", it) }
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }
}
