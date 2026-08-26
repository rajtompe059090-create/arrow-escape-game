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
     * - EASY: 1..100
     * - NORMAL: 101..200
     * - HARD: 201..300
     * - VERY_HARD: 301..400
     * - MASTER: 401..600
     * - GRANDMASTER: 601..800
     * - LEGENDARY: 801..1000
     */
    fun getLevelsForTier(difficulty: Difficulty): List<Int> {
        return when (difficulty) {
            Difficulty.EASY -> (1..100).toList()
            Difficulty.NORMAL -> (101..200).toList()
            Difficulty.HARD -> (201..300).toList()
            Difficulty.VERY_HARD -> (301..400).toList()
            Difficulty.MASTER -> (401..600).toList()
            Difficulty.GRANDMASTER -> (601..800).toList()
            Difficulty.LEGENDARY -> (801..1000).toList()
        }
    }

    fun getAllDifficulties(): List<Difficulty> {
        return Difficulty.values().toList()
    }
}
