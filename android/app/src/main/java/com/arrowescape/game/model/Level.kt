package com.arrowescape.game.model

enum class Difficulty(
    val displayName: String,
    val levelRange: String,
    val rewardRupees: Double
) {
    EASY("Easy", "Levels 1–50", 1.00),
    NORMAL("Normal", "Levels 51–125", 2.00),
    MEDIUM("Medium", "Levels 126–250", 3.00),
    HARD("Hard", "Levels 251–400", 5.00),
    VERY_HARD("Very Hard", "Levels 401–550", 10.00),
    MASTER("Master", "Levels 551–700", 15.00),
    GRANDMASTER("Grandmaster", "Levels 701–850", 20.00),
    LEGENDARY("Legendary", "Levels 851+", 25.00);

    companion object {
        fun fromLevel(levelId: Int): Difficulty {
            return when {
                levelId <= 50 -> EASY
                levelId <= 125 -> NORMAL
                levelId <= 250 -> MEDIUM
                levelId <= 400 -> HARD
                levelId <= 550 -> VERY_HARD
                levelId <= 700 -> MASTER
                levelId <= 850 -> GRANDMASTER
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
