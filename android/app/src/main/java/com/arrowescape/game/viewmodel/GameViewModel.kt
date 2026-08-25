package com.arrowescape.game.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrowescape.game.data.LevelRepository
import com.arrowescape.game.data.UserPreferencesRepository
import com.arrowescape.game.engine.PuzzleEngine
import com.arrowescape.game.model.Arrow
import com.arrowescape.game.model.EarningTransaction
import com.arrowescape.game.model.Level
import com.arrowescape.game.sound.SoundManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GameUiState(
    val currentLevel: Level? = null,
    val remainingArrows: List<Arrow> = emptyList(),
    val escapingArrowIds: Set<String> = emptySet(),
    val blockedArrowId: String? = null,
    val hintedArrowId: String? = null,
    val lives: Int = 3,
    val maxLives: Int = 3,
    val movesCount: Int = 0,
    val isLevelCompleted: Boolean = false,
    val isGameOver: Boolean = false,
    val unlockedLevel: Int = 1,
    val completedLevels: Set<Int> = emptySet(),
    val walletBalance: Double = 0.0,
    val totalEarnings: Double = 0.0,
    val hintsRemaining: Int = 3,
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = false,
    val hapticsEnabled: Boolean = true,
    val dailyStreak: Int = 0,
    val lastDailyRewardTimestamp: Long = 0L,
    val username: String = "PlayerOne",
    val earningHistory: List<EarningTransaction> = emptyList(),
    val lastCompletedReward: Double = 0.0,
    val isLastLevelAlreadyClaimed: Boolean = false
)

class GameViewModel(
    private val prefsRepo: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            prefsRepo.userPreferencesFlow.collect { prefs ->
                _uiState.update {
                    it.copy(
                        unlockedLevel = prefs.unlockedLevel,
                        completedLevels = prefs.completedLevels,
                        walletBalance = prefs.walletBalance,
                        totalEarnings = prefs.totalEarnings,
                        hintsRemaining = prefs.hintsRemaining,
                        soundEnabled = prefs.soundEnabled,
                        musicEnabled = prefs.musicEnabled,
                        hapticsEnabled = prefs.hapticsEnabled,
                        dailyStreak = prefs.dailyStreak,
                        lastDailyRewardTimestamp = prefs.lastDailyRewardTimestamp,
                        username = prefs.username,
                        earningHistory = prefs.earningHistory
                    )
                }
                // Sync settings with SoundManager
                SoundManager.isSoundEnabled = prefs.soundEnabled
                SoundManager.isHapticsEnabled = prefs.hapticsEnabled
                SoundManager.setMusic(prefs.musicEnabled)
            }
        }
    }

    fun loadLevel(levelId: Int) {
        val level = LevelRepository.getLevel(levelId) ?: return

        _uiState.update {
            it.copy(
                currentLevel = level,
                remainingArrows = level.arrows,
                escapingArrowIds = emptySet(),
                blockedArrowId = null,
                hintedArrowId = null,
                lives = 3,
                movesCount = 0,
                isLevelCompleted = false,
                isGameOver = false,
                lastCompletedReward = 0.0,
                isLastLevelAlreadyClaimed = false
            )
        }
    }

    fun onArrowTapped(arrow: Arrow) {
        val state = _uiState.value
        val level = state.currentLevel ?: return

        if (state.lives <= 0 || state.isLevelCompleted || state.escapingArrowIds.contains(arrow.id)) {
            return
        }

        val check = PuzzleEngine.isArrowPathClear(
            arrow = arrow,
            allArrows = state.remainingArrows,
            gridWidth = level.gridWidth,
            gridHeight = level.gridHeight
        )

        if (check.isClear) {
            SoundManager.playEscape()
            // Start escape animation
            _uiState.update {
                it.copy(
                    escapingArrowIds = it.escapingArrowIds + arrow.id,
                    movesCount = it.movesCount + 1,
                    hintedArrowId = if (it.hintedArrowId == arrow.id) null else it.hintedArrowId
                )
            }

            viewModelScope.launch {
                delay(400)

                val updatedArrows = _uiState.value.remainingArrows.filter { it.id != arrow.id }
                val isCompleted = updatedArrows.isEmpty()

                _uiState.update {
                    it.copy(
                        remainingArrows = updatedArrows,
                        escapingArrowIds = it.escapingArrowIds - arrow.id,
                        isLevelCompleted = isCompleted
                    )
                }

                if (isCompleted) {
                    SoundManager.playLevelComplete()
                    val reward = PuzzleEngine.calculateRewardRupees(level.id).toDouble()
                    val isFirstTime = prefsRepo.recordLevelCompleted(level.id, reward)

                    _uiState.update {
                        it.copy(
                            lastCompletedReward = if (isFirstTime) reward else 0.0,
                            isLastLevelAlreadyClaimed = !isFirstTime
                        )
                    }
                }
            }
        } else {
            // Arrow is blocked
            SoundManager.playBlocked()
            val newLives = state.lives - 1

            _uiState.update {
                it.copy(
                    blockedArrowId = arrow.id,
                    lives = newLives,
                    movesCount = it.movesCount + 1
                )
            }

            viewModelScope.launch {
                delay(350)
                _uiState.update {
                    it.copy(
                        blockedArrowId = null,
                        isGameOver = newLives <= 0
                    )
                }
                if (newLives <= 0) {
                    SoundManager.playGameOver()
                }
            }
        }
    }

    fun useHint() {
        val state = _uiState.value
        val level = state.currentLevel ?: return
        if (state.remainingArrows.isEmpty() || state.isLevelCompleted) return

        val solvableArrow = PuzzleEngine.findFreeArrow(
            arrows = state.remainingArrows,
            gridWidth = level.gridWidth,
            gridHeight = level.gridHeight
        )

        if (solvableArrow != null) {
            SoundManager.playHint()
            _uiState.update { it.copy(hintedArrowId = solvableArrow.id) }
            viewModelScope.launch {
                prefsRepo.decrementHint()
            }
        }
    }

    fun grantRewardedHint() {
        viewModelScope.launch {
            prefsRepo.addHint()
            useHint()
        }
    }

    fun restartCurrentLevel() {
        val currentLvlId = _uiState.value.currentLevel?.id ?: 1
        loadLevel(currentLvlId)
    }

    fun claimDailyReward(multiplier: Int = 1) {
        val state = _uiState.value
        val now = System.currentTimeMillis()
        val twentyFourHoursMs = 24 * 60 * 60 * 1000L
        val fortyEightHoursMs = 48 * 60 * 60 * 1000L

        val lastTs = state.lastDailyRewardTimestamp
        val isWithin24h = (lastTs > 0 && now - lastTs < twentyFourHoursMs)
        if (isWithin24h) return // Already claimed today

        val isStreakBroken = (lastTs > 0 && now - lastTs > fortyEightHoursMs)
        val nextStreak = if (isStreakBroken) 1 else (state.dailyStreak % 7) + 1

        val dailyAmounts = intArrayOf(1, 2, 3, 4, 5, 7, 10)
        val baseAmount = dailyAmounts[(nextStreak - 1).coerceIn(0, 6)].toDouble()
        val finalAmount = baseAmount * multiplier

        SoundManager.playLevelComplete()
        viewModelScope.launch {
            prefsRepo.claimDailyReward(finalAmount, nextStreak)
        }
    }

    fun requestWithdrawal(amount: Double, upiId: String, onResult: (Boolean, String) -> Unit) {
        if (amount < 50.0) {
            onResult(false, "Minimum withdrawal is ₹50.00")
            return
        }
        if (_uiState.value.walletBalance < amount) {
            onResult(false, "Insufficient wallet balance")
            return
        }
        val upiPattern = "^[a-zA-Z0-9.\\-_]{2,256}@[a-zA-Z]{2,64}$".toRegex()
        if (!upiPattern.matches(upiId.trim())) {
            onResult(false, "Please enter a valid UPI ID (e.g. name@upi)")
            return
        }

        viewModelScope.launch {
            val success = prefsRepo.requestWithdrawal(amount, upiId.trim())
            if (success) {
                SoundManager.playLevelComplete()
                onResult(true, "Withdrawal request of ₹${"%.2f".format(amount)} submitted successfully! Status: Processing.")
            } else {
                onResult(false, "Failed to process withdrawal. Please try again.")
            }
        }
    }

    fun toggleSound() {
        viewModelScope.launch {
            val next = !_uiState.value.soundEnabled
            prefsRepo.setSoundEnabled(next)
            SoundManager.playTap()
        }
    }

    fun toggleMusic() {
        viewModelScope.launch {
            val next = !_uiState.value.musicEnabled
            prefsRepo.setMusicEnabled(next)
            SoundManager.playTap()
        }
    }

    fun toggleHaptics() {
        viewModelScope.launch {
            val next = !_uiState.value.hapticsEnabled
            prefsRepo.setHapticsEnabled(next)
            SoundManager.playTap()
        }
    }

    fun setUsername(name: String) {
        viewModelScope.launch {
            prefsRepo.setUsername(name)
        }
    }

    fun resetAllProgress() {
        viewModelScope.launch {
            prefsRepo.resetAllProgress()
            loadLevel(1)
        }
    }
}
