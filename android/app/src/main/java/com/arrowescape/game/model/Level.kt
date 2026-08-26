package com.arrowescape.game.model

enum class Difficulty(
    val displayName: String,
    val levelRange: String,
    val rewardRupees: Double
) {
    EASY("Easy", "Levels 1–100", 0.25),
    NORMAL("Normal", "Levels 101–200", 0.50),
    HARD("Hard", "Levels 201–300", 1.00),
    VERY_HARD("Very Hard", "Levels 301–400", 1.50),
    MASTER("Master", "Levels 401–600", 2.00),
    GRANDMASTER("Grandmaster", "Levels 601–800", 2.50),
    LEGENDARY("Legendary", "Levels 801+", 3.00);

    companion object {
        fun fromLevel(levelId: Int): Difficulty {
            return when {
                levelId <= 100 -> EASY
                levelId <= 200 -> NORMAL
                levelId <= 300 -> HARD
                levelId <= 400 -> VERY_HARD
                levelId <= 600 -> MASTER
                levelId <= 800 -> GRANDMASTER
                else -> LEGENDARY
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
    val rewardRupees: Double
)
