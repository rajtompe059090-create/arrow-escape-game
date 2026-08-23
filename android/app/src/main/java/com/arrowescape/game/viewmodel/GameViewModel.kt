package com.arrowescape.game.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrowescape.game.data.LevelRepository
import com.arrowescape.game.data.UserPreferencesRepository
import com.arrowescape.game.engine.PuzzleEngine
import com.arrowescape.game.model.Arrow
import com.arrowescape.game.model.Level
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
    val earnedRupees: Int = 0,
    val hintsRemaining: Int = 3,
    val soundEnabled: Boolean = true
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
                        earnedRupees = prefs.earnedRupees,
                        hintsRemaining = prefs.hintsRemaining,
                        soundEnabled = prefs.soundEnabled
                    )
                }
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
                isGameOver = false
            )
        }
    }

    fun onArrowTapped(arrow: Arrow) {
        val state = _uiState.value
        val level = state.currentLevel ?: return
        if (state.lives <= 0 || state.isLevelCompleted || state.escapingArrowIds.contains(arrow.id)) return

        val check = PuzzleEngine.isArrowPathClear(
            arrow = arrow,
            allArrows = state.remainingArrows,
            gridWidth = level.gridWidth,
            gridHeight = level.gridHeight
        )

        if (check.isClear) {
            _uiState.update {
                it.copy(
                    escapingArrowIds = it.escapingArrowIds + arrow.id,
                    movesCount = it.movesCount + 1,
                    hintedArrowId = if (it.hintedArrowId == arrow.id) null else it.hintedArrowId
                )
            }

            viewModelScope.launch {
                delay(380)
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
                    val reward = PuzzleEngine.calculateRewardRupees(level.id)
                    prefsRepo.recordLevelCompleted(level.id, reward)
                }
            }
        } else {
            val newLives = state.lives - 1
            _uiState.update {
                it.copy(
                    blockedArrowId = arrow.id,
                    lives = newLives,
                    movesCount = it.movesCount + 1
                )
            }

            viewModelScope.launch {
                delay(400)
                _uiState.update {
                    it.copy(
                        blockedArrowId = null,
                        isGameOver = newLives <= 0
                    )
                }
            }
        }
    }

    fun useHint() {
        val state = _uiState.value
        val level = state.currentLevel ?: return
        if (state.hintsRemaining <= 0 || state.lives <= 0) return

        val free = PuzzleEngine.findFreeArrow(state.remainingArrows, level.gridWidth, level.gridHeight)
        if (free != null) {
            _uiState.update { it.copy(hintedArrowId = free.id) }
            viewModelScope.launch {
                prefsRepo.decrementHint()
                delay(4000)
                _uiState.update { if (it.hintedArrowId == free.id) it.copy(hintedArrowId = null) else it }
            }
        }
    }

    fun restartCurrentLevel() {
        _uiState.value.currentLevel?.let { loadLevel(it.id) }
    }

    fun toggleSound() {
        viewModelScope.launch {
            prefsRepo.setSoundEnabled(!_uiState.value.soundEnabled)
        }
    }

    fun resetProgress() {
        viewModelScope.launch {
            prefsRepo.resetAllProgress()
        }
    }
}
