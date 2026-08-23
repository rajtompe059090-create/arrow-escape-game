package com.arrowescape.game.model

data class Arrow(
    val id: String,
    val points: List<GridPoint>,
    val headDirection: Direction
)
