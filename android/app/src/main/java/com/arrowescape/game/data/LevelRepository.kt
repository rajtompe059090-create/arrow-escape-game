package com.arrowescape.game.data

import com.arrowescape.game.engine.LevelGenerator
import com.arrowescape.game.model.Difficulty
import com.arrowescape.game.model.Level

object LevelRepository {

    /**
     * Dynamically generates a fresh, verified solvable puzzle for the given level ID.
     * Fully supports 1000+ levels and arbitrary future level numbers.
     */
    fun getLevel(id: Int): Level {
        return LevelGenerator.generateLevel(id)
    }

    /**
     * Returns the list of level IDs for a specific difficulty tier:
     * - EASY: 1..50
     * - NORMAL: 51..125
     * - MEDIUM: 126..250
     * - HARD: 251..400
     * - VERY_HARD: 401..550
     * - MASTER: 551..700
     * - GRANDMASTER: 701..850
     * - LEGENDARY: 851..1000
     */
    fun getLevelsForTier(difficulty: Difficulty): List<Int> {
        return when (difficulty) {
            Difficulty.EASY -> (1..50).toList()
            Difficulty.NORMAL -> (51..125).toList()
            Difficulty.MEDIUM -> (126..250).toList()
            Difficulty.HARD -> (251..400).toList()
            Difficulty.VERY_HARD -> (401..550).toList()
            Difficulty.MASTER -> (551..700).toList()
            Difficulty.GRANDMASTER -> (701..850).toList()
            Difficulty.LEGENDARY -> (851..1000).toList()
        }
    }

    fun getAllDifficulties(): List<Difficulty> {
        return Difficulty.values().toList()
    }
}
