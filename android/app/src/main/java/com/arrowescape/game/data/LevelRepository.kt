package com.arrowescape.game.data

import com.arrowescape.game.engine.LevelGenerator
import com.arrowescape.game.model.Difficulty
import com.arrowescape.game.model.Level

object LevelRepository {

    /**
     * Dynamically generates a fresh, verified solvable puzzle for the given level ID.
     */
    fun getLevel(id: Int): Level {
        return LevelGenerator.generateLevel(id)
    }

    /**
     * Returns the list of level IDs for a specific difficulty tier:
     * - EASY: 1..50
     * - NORMAL: 51..100
     * - HARD: 101..150
     * - VERY_HARD: 151..200
     * - EXTREME: 201..250
     */
    fun getLevelsForTier(difficulty: Difficulty): List<Int> {
        return when (difficulty) {
            Difficulty.EASY -> (1..50).toList()
            Difficulty.NORMAL -> (51..100).toList()
            Difficulty.HARD -> (101..150).toList()
            Difficulty.VERY_HARD -> (151..200).toList()
            Difficulty.EXTREME -> (201..250).toList()
        }
    }

    fun getAllDifficulties(): List<Difficulty> {
        return Difficulty.values().toList()
    }
}
