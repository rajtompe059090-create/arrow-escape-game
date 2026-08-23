package com.arrowescape.game.model

enum class Difficulty {
    Easy,
    Normal,
    Hard,
    Expert
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
