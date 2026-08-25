package com.arrowescape.game.model

enum class Difficulty(
    val displayName: String,
    val levelRange: String,
    val rewardRupees: Int
) {
    EASY("Easy", "Levels 1–50", 2),
    NORMAL("Normal", "Levels 51–100", 3),
    HARD("Hard", "Levels 101–150", 5),
    VERY_HARD("Very Hard", "Levels 151–200", 10),
    EXTREME("Extreme", "Levels 201+", 15);

    companion object {
        fun fromLevel(levelId: Int): Difficulty {
            return when {
                levelId <= 50 -> EASY
                levelId <= 100 -> NORMAL
                levelId <= 150 -> HARD
                levelId <= 200 -> VERY_HARD
                else -> EXTREME
            }
        }
    }
}

data class Level(
    val id: Int,
    val name: String,
    val gridWidth: Int,
    val gridHeight: Int,
    val difficulty: Difficulty,
    val arrows: List<Arrow>,
    val rewardRupees: Int
)

