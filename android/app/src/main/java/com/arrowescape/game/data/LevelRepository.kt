package com.arrowescape.game.data

import com.arrowescape.game.engine.PuzzleEngine
import com.arrowescape.game.model.Arrow
import com.arrowescape.game.model.Difficulty
import com.arrowescape.game.model.Direction
import com.arrowescape.game.model.GridPoint
import com.arrowescape.game.model.Level

object LevelRepository {

    private val levels: List<Level> = listOf(
        // Levels 1-5: Easy
        Level(
            id = 1,
            name = "First Flight",
            gridWidth = 6,
            gridHeight = 6,
            difficulty = Difficulty.Easy,
            rewardRupees = PuzzleEngine.calculateRewardRupees(1),
            arrows = listOf(
                Arrow("a1", listOf(GridPoint(1, 1), GridPoint(4, 1)), Direction.RIGHT),
                Arrow("a2", listOf(GridPoint(2, 0), GridPoint(2, 0)), Direction.DOWN),
                Arrow("a3", listOf(GridPoint(4, 3), GridPoint(1, 3)), Direction.LEFT),
                Arrow("a4", listOf(GridPoint(3, 5), GridPoint(3, 4)), Direction.UP)
            )
        ),
        Level(
            id = 2,
            name = "Corner Turn",
            gridWidth = 6,
            gridHeight = 6,
            difficulty = Difficulty.Easy,
            rewardRupees = PuzzleEngine.calculateRewardRupees(2),
            arrows = listOf(
                Arrow("a1", listOf(GridPoint(1, 4), GridPoint(1, 1), GridPoint(1, 0)), Direction.UP),
                Arrow("a2", listOf(GridPoint(2, 4), GridPoint(2, 2), GridPoint(4, 2)), Direction.RIGHT),
                Arrow("a3", listOf(GridPoint(3, 0), GridPoint(3, 1)), Direction.DOWN),
                Arrow("a4", listOf(GridPoint(5, 4), GridPoint(2, 4)), Direction.LEFT),
                Arrow("a5", listOf(GridPoint(4, 3), GridPoint(4, 5)), Direction.DOWN)
            )
        ),
        Level(
            id = 3,
            name = "Crossroad",
            gridWidth = 6,
            gridHeight = 6,
            difficulty = Difficulty.Easy,
            rewardRupees = PuzzleEngine.calculateRewardRupees(3),
            arrows = listOf(
                Arrow("a1", listOf(GridPoint(0, 2), GridPoint(4, 2)), Direction.RIGHT),
                Arrow("a2", listOf(GridPoint(2, 5), GridPoint(2, 3)), Direction.UP),
                Arrow("a3", listOf(GridPoint(4, 4), GridPoint(1, 4)), Direction.LEFT),
                Arrow("a4", listOf(GridPoint(1, 0), GridPoint(1, 1)), Direction.DOWN),
                Arrow("a5", listOf(GridPoint(5, 1), GridPoint(5, 5)), Direction.DOWN),
                Arrow("a6", listOf(GridPoint(3, 0), GridPoint(0, 0)), Direction.LEFT)
            )
        ),
        Level(
            id = 4,
            name = "Square Dance",
            gridWidth = 7,
            gridHeight = 7,
            difficulty = Difficulty.Easy,
            rewardRupees = PuzzleEngine.calculateRewardRupees(4),
            arrows = listOf(
                Arrow("a1", listOf(GridPoint(1, 5), GridPoint(1, 1), GridPoint(5, 1)), Direction.RIGHT),
                Arrow("a2", listOf(GridPoint(5, 2), GridPoint(5, 5), GridPoint(2, 5)), Direction.LEFT),
                Arrow("a3", listOf(GridPoint(3, 2), GridPoint(3, 4)), Direction.DOWN),
                Arrow("a4", listOf(GridPoint(4, 4), GridPoint(4, 2)), Direction.UP),
                Arrow("a5", listOf(GridPoint(0, 3), GridPoint(0, 0)), Direction.UP),
                Arrow("a6", listOf(GridPoint(6, 3), GridPoint(6, 6)), Direction.DOWN),
                Arrow("a7", listOf(GridPoint(3, 6), GridPoint(0, 6)), Direction.LEFT)
            )
        ),
        Level(
            id = 5,
            name = "Zig Zag Simple",
            gridWidth = 7,
            gridHeight = 7,
            difficulty = Difficulty.Easy,
            rewardRupees = PuzzleEngine.calculateRewardRupees(5),
            arrows = listOf(
                Arrow("a1", listOf(GridPoint(1, 1), GridPoint(3, 1), GridPoint(3, 3), GridPoint(5, 3)), Direction.RIGHT),
                Arrow("a2", listOf(GridPoint(5, 4), GridPoint(3, 4), GridPoint(3, 6)), Direction.DOWN),
                Arrow("a3", listOf(GridPoint(1, 5), GridPoint(1, 2)), Direction.UP),
                Arrow("a4", listOf(GridPoint(4, 0), GridPoint(4, 2)), Direction.DOWN),
                Arrow("a5", listOf(GridPoint(6, 1), GridPoint(6, 5)), Direction.DOWN),
                Arrow("a6", listOf(GridPoint(2, 6), GridPoint(0, 6)), Direction.LEFT),
                Arrow("a7", listOf(GridPoint(2, 0), GridPoint(0, 0)), Direction.LEFT),
                Arrow("a8", listOf(GridPoint(0, 4), GridPoint(0, 1)), Direction.UP)
            )
        ),

        // Levels 6-12: Normal
        Level(
            id = 6,
            name = "Dual Spiral",
            gridWidth = 7,
            gridHeight = 7,
            difficulty = Difficulty.Normal,
            rewardRupees = PuzzleEngine.calculateRewardRupees(6),
            arrows = listOf(
                Arrow("a1", listOf(GridPoint(2, 2), GridPoint(4, 2), GridPoint(4, 4), GridPoint(2, 4), GridPoint(2, 5)), Direction.DOWN),
                Arrow("a2", listOf(GridPoint(5, 1), GridPoint(5, 5), GridPoint(6, 5)), Direction.RIGHT),
                Arrow("a3", listOf(GridPoint(1, 5), GridPoint(1, 1), GridPoint(4, 1)), Direction.RIGHT),
                Arrow("a4", listOf(GridPoint(3, 0), GridPoint(1, 0)), Direction.LEFT),
                Arrow("a5", listOf(GridPoint(0, 2), GridPoint(0, 6)), Direction.DOWN),
                Arrow("a6", listOf(GridPoint(3, 6), GridPoint(6, 6)), Direction.RIGHT),
                Arrow("a7", listOf(GridPoint(6, 3), GridPoint(6, 0)), Direction.UP),
                Arrow("a8", listOf(GridPoint(3, 3), GridPoint(3, 1)), Direction.UP)
            )
        ),
        Level(
            id = 7,
            name = "Double U",
            gridWidth = 8,
            gridHeight = 8,
            difficulty = Difficulty.Normal,
            rewardRupees = PuzzleEngine.calculateRewardRupees(7),
            arrows = listOf(
                Arrow("a1", listOf(GridPoint(2, 2), GridPoint(2, 5), GridPoint(5, 5), GridPoint(5, 2)), Direction.UP),
                Arrow("a2", listOf(GridPoint(1, 1), GridPoint(6, 1), GridPoint(6, 6), GridPoint(1, 6)), Direction.LEFT),
                Arrow("a3", listOf(GridPoint(3, 3), GridPoint(4, 3)), Direction.RIGHT),
                Arrow("a4", listOf(GridPoint(3, 4), GridPoint(4, 4)), Direction.RIGHT),
                Arrow("a5", listOf(GridPoint(0, 3), GridPoint(0, 0)), Direction.UP),
                Arrow("a6", listOf(GridPoint(7, 4), GridPoint(7, 7)), Direction.DOWN),
                Arrow("a7", listOf(GridPoint(4, 0), GridPoint(7, 0)), Direction.RIGHT),
                Arrow("a8", listOf(GridPoint(4, 7), GridPoint(0, 7)), Direction.LEFT),
                Arrow("a9", listOf(GridPoint(0, 4), GridPoint(0, 6)), Direction.DOWN)
            )
        ),
        Level(
            id = 8,
            name = "Interlock Matrix",
            gridWidth = 8,
            gridHeight = 8,
            difficulty = Difficulty.Normal,
            rewardRupees = PuzzleEngine.calculateRewardRupees(8),
            arrows = listOf(
                Arrow("a1", listOf(GridPoint(1, 2), GridPoint(3, 2), GridPoint(3, 4)), Direction.DOWN),
                Arrow("a2", listOf(GridPoint(4, 5), GridPoint(2, 5), GridPoint(2, 3)), Direction.UP),
                Arrow("a3", listOf(GridPoint(5, 2), GridPoint(5, 4), GridPoint(4, 4)), Direction.LEFT),
                Arrow("a4", listOf(GridPoint(4, 1), GridPoint(6, 1), GridPoint(6, 3)), Direction.DOWN),
                Arrow("a5", listOf(GridPoint(6, 5), GridPoint(6, 7)), Direction.DOWN),
                Arrow("a6", listOf(GridPoint(1, 6), GridPoint(5, 6)), Direction.RIGHT),
                Arrow("a7", listOf(GridPoint(7, 2), GridPoint(7, 0)), Direction.UP),
                Arrow("a8", listOf(GridPoint(0, 1), GridPoint(3, 1)), Direction.RIGHT),
                Arrow("a9", listOf(GridPoint(0, 4), GridPoint(0, 7)), Direction.DOWN),
                Arrow("a10", listOf(GridPoint(5, 7), GridPoint(2, 7)), Direction.LEFT)
            )
        ),
        Level(
            id = 9,
            name = "Windmill",
            gridWidth = 8,
            gridHeight = 8,
            difficulty = Difficulty.Normal,
            rewardRupees = PuzzleEngine.calculateRewardRupees(9),
            arrows = listOf(
                Arrow("a1", listOf(GridPoint(3, 3), GridPoint(1, 3), GridPoint(1, 1)), Direction.UP),
                Arrow("a2", listOf(GridPoint(4, 3), GridPoint(4, 1), GridPoint(6, 1)), Direction.RIGHT),
                Arrow("a3", listOf(GridPoint(4, 4), GridPoint(6, 4), GridPoint(6, 6)), Direction.DOWN),
                Arrow("a4", listOf(GridPoint(3, 4), GridPoint(3, 6), GridPoint(1, 6)), Direction.LEFT),
                Arrow("a5", listOf(GridPoint(0, 2), GridPoint(0, 0)), Direction.UP),
                Arrow("a6", listOf(GridPoint(5, 0), GridPoint(7, 0)), Direction.RIGHT),
                Arrow("a7", listOf(GridPoint(7, 5), GridPoint(7, 7)), Direction.DOWN),
                Arrow("a8", listOf(GridPoint(2, 7), GridPoint(0, 7)), Direction.LEFT),
                Arrow("a9", listOf(GridPoint(2, 0), GridPoint(2, 2)), Direction.DOWN),
                Arrow("a10", listOf(GridPoint(7, 2), GridPoint(5, 2)), Direction.LEFT),
                Arrow("a11", listOf(GridPoint(5, 7), GridPoint(5, 5)), Direction.UP)
            )
        ),
        Level(
            id = 10,
            name = "Snake Nest",
            gridWidth = 8,
            gridHeight = 8,
            difficulty = Difficulty.Normal,
            rewardRupees = PuzzleEngine.calculateRewardRupees(10),
            arrows = listOf(
                Arrow("a1", listOf(GridPoint(1, 1), GridPoint(4, 1), GridPoint(4, 2), GridPoint(2, 2), GridPoint(2, 3)), Direction.DOWN),
                Arrow("a2", listOf(GridPoint(5, 2), GridPoint(5, 5), GridPoint(3, 5)), Direction.LEFT),
                Arrow("a3", listOf(GridPoint(1, 4), GridPoint(1, 6), GridPoint(4, 6), GridPoint(4, 4)), Direction.UP),
                Arrow("a4", listOf(GridPoint(6, 1), GridPoint(6, 4), GridPoint(7, 4)), Direction.RIGHT),
                Arrow("a5", listOf(GridPoint(0, 2), GridPoint(0, 5)), Direction.DOWN),
                Arrow("a6", listOf(GridPoint(2, 7), GridPoint(6, 7)), Direction.RIGHT),
                Arrow("a7", listOf(GridPoint(7, 6), GridPoint(7, 0)), Direction.UP),
                Arrow("a8", listOf(GridPoint(5, 0), GridPoint(1, 0)), Direction.LEFT),
                Arrow("a9", listOf(GridPoint(3, 3), GridPoint(3, 4)), Direction.DOWN),
                Arrow("a10", listOf(GridPoint(0, 7), GridPoint(0, 6)), Direction.UP),
                Arrow("a11", listOf(GridPoint(7, 7), GridPoint(7, 6)), Direction.UP)
            )
        ),
        Level(
            id = 11,
            name = "Hook & Loop",
            gridWidth = 8,
            gridHeight = 8,
            difficulty = Difficulty.Normal,
            rewardRupees = PuzzleEngine.calculateRewardRupees(11),
            arrows = listOf(
                Arrow("a1", listOf(GridPoint(2, 3), GridPoint(4, 3), GridPoint(4, 1), GridPoint(1, 1)), Direction.LEFT),
                Arrow("a2", listOf(GridPoint(5, 2), GridPoint(5, 5), GridPoint(2, 5), GridPoint(2, 4)), Direction.UP),
                Arrow("a3", listOf(GridPoint(3, 6), GridPoint(6, 6), GridPoint(6, 3)), Direction.UP),
                Arrow("a4", listOf(GridPoint(1, 2), GridPoint(1, 5)), Direction.DOWN),
                Arrow("a5", listOf(GridPoint(0, 1), GridPoint(0, 7)), Direction.DOWN),
                Arrow("a6", listOf(GridPoint(7, 6), GridPoint(7, 1)), Direction.UP),
                Arrow("a7", listOf(GridPoint(6, 0), GridPoint(2, 0)), Direction.LEFT),
                Arrow("a8", listOf(GridPoint(1, 7), GridPoint(6, 7)), Direction.RIGHT),
                Arrow("a9", listOf(GridPoint(3, 2), GridPoint(3, 1)), Direction.UP),
                Arrow("a10", listOf(GridPoint(4, 4), GridPoint(4, 5)), Direction.DOWN),
                Arrow("a11", listOf(GridPoint(7, 0), GridPoint(7, 1)), Direction.DOWN),
                Arrow("a12", listOf(GridPoint(0, 0), GridPoint(1, 0)), Direction.RIGHT)
            )
        ),
        Level(
            id = 12,
            name = "Tangled Grid",
            gridWidth = 8,
            gridHeight = 8,
            difficulty = Difficulty.Normal,
            rewardRupees = PuzzleEngine.calculateRewardRupees(12),
            arrows = listOf(
                Arrow("a1", listOf(GridPoint(2, 1), GridPoint(5, 1), GridPoint(5, 3)), Direction.DOWN),
                Arrow("a2", listOf(GridPoint(6, 2), GridPoint(6, 5), GridPoint(3, 5)), Direction.LEFT),
                Arrow("a3", listOf(GridPoint(2, 6), GridPoint(2, 3), GridPoint(4, 3)), Direction.RIGHT),
                Arrow("a4", listOf(GridPoint(3, 4), GridPoint(5, 4)), Direction.RIGHT),
                Arrow("a5", listOf(GridPoint(4, 2), GridPoint(4, 1)), Direction.UP),
                Arrow("a6", listOf(GridPoint(1, 1), GridPoint(1, 5)), Direction.DOWN),
                Arrow("a7", listOf(GridPoint(1, 6), GridPoint(6, 6)), Direction.RIGHT),
                Arrow("a8", listOf(GridPoint(7, 5), GridPoint(7, 1)), Direction.UP),
                Arrow("a9", listOf(GridPoint(6, 0), GridPoint(1, 0)), Direction.LEFT),
                Arrow("a10", listOf(GridPoint(0, 1), GridPoint(0, 6)), Direction.DOWN),
                Arrow("a11", listOf(GridPoint(0, 7), GridPoint(7, 7)), Direction.RIGHT),
                Arrow("a12", listOf(GridPoint(3, 2), GridPoint(2, 2)), Direction.LEFT)
            )
        ),

        // Levels 13-17: Hard
        Level(
            id = 13,
            name = "Labyrinth Core",
            gridWidth = 9,
            gridHeight = 9,
            difficulty = Difficulty.Hard,
            rewardRupees = PuzzleEngine.calculateRewardRupees(13),
            arrows = listOf(
                Arrow("a1", listOf(GridPoint(2, 2), GridPoint(6, 2), GridPoint(6, 6), GridPoint(2, 6), GridPoint(2, 3)), Direction.UP),
                Arrow("a2", listOf(GridPoint(3, 3), GridPoint(5, 3), GridPoint(5, 5), GridPoint(3, 5)), Direction.LEFT),
                Arrow("a3", listOf(GridPoint(4, 4), GridPoint(4, 3)), Direction.UP),
                Arrow("a4", listOf(GridPoint(1, 1), GridPoint(7, 1), GridPoint(7, 7)), Direction.DOWN),
                Arrow("a5", listOf(GridPoint(7, 8), GridPoint(1, 8), GridPoint(1, 2)), Direction.UP),
                Arrow("a6", listOf(GridPoint(0, 0), GridPoint(8, 0)), Direction.RIGHT),
                Arrow("a7", listOf(GridPoint(8, 1), GridPoint(8, 8)), Direction.DOWN),
                Arrow("a8", listOf(GridPoint(0, 8), GridPoint(0, 1)), Direction.UP),
                Arrow("a9", listOf(GridPoint(3, 7), GridPoint(6, 7)), Direction.RIGHT),
                Arrow("a10", listOf(GridPoint(2, 7), GridPoint(2, 8)), Direction.DOWN),
                Arrow("a11", listOf(GridPoint(6, 1), GridPoint(6, 0)), Direction.UP),
                Arrow("a12", listOf(GridPoint(4, 6), GridPoint(4, 7)), Direction.DOWN),
                Arrow("a13", listOf(GridPoint(5, 6), GridPoint(5, 7)), Direction.DOWN),
                Arrow("a14", listOf(GridPoint(3, 1), GridPoint(3, 0)), Direction.UP)
            )
        ),
        Level(
            id = 14,
            name = "Woven Tapestry",
            gridWidth = 9,
            gridHeight = 9,
            difficulty = Difficulty.Hard,
            rewardRupees = PuzzleEngine.calculateRewardRupees(14),
            arrows = listOf(
                Arrow("a1", listOf(GridPoint(1, 2), GridPoint(4, 2), GridPoint(4, 5), GridPoint(1, 5)), Direction.LEFT),
                Arrow("a2", listOf(GridPoint(7, 2), GridPoint(5, 2), GridPoint(5, 6)), Direction.DOWN),
                Arrow("a3", listOf(GridPoint(2, 7), GridPoint(6, 7), GridPoint(6, 4)), Direction.UP),
                Arrow("a4", listOf(GridPoint(3, 3), GridPoint(3, 4), GridPoint(2, 4)), Direction.LEFT),
                Arrow("a5", listOf(GridPoint(2, 1), GridPoint(7, 1)), Direction.RIGHT),
                Arrow("a6", listOf(GridPoint(8, 2), GridPoint(8, 7)), Direction.DOWN),
                Arrow("a7", listOf(GridPoint(7, 8), GridPoint(1, 8)), Direction.LEFT),
                Arrow("a8", listOf(GridPoint(0, 7), GridPoint(0, 2)), Direction.UP),
                Arrow("a9", listOf(GridPoint(3, 6), GridPoint(3, 7)), Direction.DOWN),
                Arrow("a10", listOf(GridPoint(5, 3), GridPoint(5, 1)), Direction.UP),
                Arrow("a11", listOf(GridPoint(6, 3), GridPoint(7, 3)), Direction.RIGHT),
                Arrow("a12", listOf(GridPoint(1, 4), GridPoint(1, 3)), Direction.UP),
                Arrow("a13", listOf(GridPoint(0, 1), GridPoint(0, 0)), Direction.UP),
                Arrow("a14", listOf(GridPoint(8, 8), GridPoint(8, 8)), Direction.RIGHT),
                Arrow("a15", listOf(GridPoint(4, 8), GridPoint(4, 7)), Direction.UP)
            )
        ),
        Level(
            id = 15,
            name = "Circuit Chaos",
            gridWidth = 9,
            gridHeight = 9,
            difficulty = Difficulty.Hard,
            rewardRupees = PuzzleEngine.calculateRewardRupees(15),
            arrows = listOf(
                Arrow("a1", listOf(GridPoint(1, 1), GridPoint(3, 1), GridPoint(3, 3), GridPoint(1, 3)), Direction.LEFT),
                Arrow("a2", listOf(GridPoint(5, 1), GridPoint(7, 1), GridPoint(7, 3), GridPoint(5, 3)), Direction.LEFT),
                Arrow("a3", listOf(GridPoint(1, 5), GridPoint(3, 5), GridPoint(3, 7), GridPoint(1, 7)), Direction.LEFT),
                Arrow("a4", listOf(GridPoint(5, 5), GridPoint(7, 5), GridPoint(7, 7), GridPoint(5, 7)), Direction.LEFT),
                Arrow("a5", listOf(GridPoint(4, 2), GridPoint(4, 6)), Direction.DOWN),
                Arrow("a6", listOf(GridPoint(2, 4), GridPoint(6, 4)), Direction.RIGHT),
                Arrow("a7", listOf(GridPoint(0, 0), GridPoint(0, 8)), Direction.DOWN),
                Arrow("a8", listOf(GridPoint(1, 8), GridPoint(8, 8)), Direction.RIGHT),
                Arrow("a9", listOf(GridPoint(8, 7), GridPoint(8, 0)), Direction.UP),
                Arrow("a10", listOf(GridPoint(7, 0), GridPoint(1, 0)), Direction.LEFT),
                Arrow("a11", listOf(GridPoint(2, 2), GridPoint(2, 1)), Direction.UP),
                Arrow("a12", listOf(GridPoint(6, 2), GridPoint(6, 1)), Direction.UP),
                Arrow("a13", listOf(GridPoint(2, 6), GridPoint(2, 7)), Direction.DOWN),
                Arrow("a14", listOf(GridPoint(6, 6), GridPoint(6, 7)), Direction.DOWN),
                Arrow("a15", listOf(GridPoint(4, 1), GridPoint(4, 0)), Direction.UP),
                Arrow("a16", listOf(GridPoint(4, 7), GridPoint(4, 8)), Direction.DOWN)
            )
        ),
        Level(
            id = 16,
            name = "Hydra Heads",
            gridWidth = 9,
            gridHeight = 9,
            difficulty = Difficulty.Hard,
            rewardRupees = PuzzleEngine.calculateRewardRupees(16),
            arrows = listOf(
                Arrow("a1", listOf(GridPoint(4, 4), GridPoint(2, 4), GridPoint(2, 2), GridPoint(4, 2)), Direction.RIGHT),
                Arrow("a2", listOf(GridPoint(4, 5), GridPoint(6, 5), GridPoint(6, 7), GridPoint(4, 7)), Direction.LEFT),
                Arrow("a3", listOf(GridPoint(3, 3), GridPoint(3, 1)), Direction.UP),
                Arrow("a4", listOf(GridPoint(5, 3), GridPoint(7, 3), GridPoint(7, 1)), Direction.UP),
                Arrow("a5", listOf(GridPoint(5, 6), GridPoint(5, 8)), Direction.DOWN),
                Arrow("a6", listOf(GridPoint(3, 6), GridPoint(1, 6), GridPoint(1, 8)), Direction.DOWN),
                Arrow("a7", listOf(GridPoint(1, 3), GridPoint(1, 1), GridPoint(2, 1)), Direction.RIGHT),
                Arrow("a8", listOf(GridPoint(7, 5), GridPoint(7, 7), GridPoint(6, 7)), Direction.LEFT),
                Arrow("a9", listOf(GridPoint(0, 2), GridPoint(0, 7)), Direction.DOWN),
                Arrow("a10", listOf(GridPoint(8, 6), GridPoint(8, 1)), Direction.UP),
                Arrow("a11", listOf(GridPoint(2, 0), GridPoint(7, 0)), Direction.RIGHT),
                Arrow("a12", listOf(GridPoint(6, 8), GridPoint(2, 8)), Direction.LEFT),
                Arrow("a13", listOf(GridPoint(0, 8), GridPoint(0, 7)), Direction.UP),
                Arrow("a14", listOf(GridPoint(8, 0), GridPoint(8, 1)), Direction.DOWN),
                Arrow("a15", listOf(GridPoint(4, 3), GridPoint(4, 4)), Direction.DOWN),
                Arrow("a16", listOf(GridPoint(4, 6), GridPoint(4, 5)), Direction.UP),
                Arrow("a17", listOf(GridPoint(3, 5), GridPoint(3, 4)), Direction.UP)
            )
        ),
        Level(
            id = 17,
            name = "Vortex Loop",
            gridWidth = 9,
            gridHeight = 9,
            difficulty = Difficulty.Hard,
            rewardRupees = PuzzleEngine.calculateRewardRupees(17),
            arrows = listOf(
                Arrow("a1", listOf(GridPoint(2, 2), GridPoint(5, 2), GridPoint(5, 5), GridPoint(2, 5)), Direction.LEFT),
                Arrow("a2", listOf(GridPoint(3, 3), GridPoint(4, 3), GridPoint(4, 4), GridPoint(3, 4)), Direction.LEFT),
                Arrow("a3", listOf(GridPoint(1, 1), GridPoint(6, 1), GridPoint(6, 6), GridPoint(1, 6)), Direction.LEFT),
                Arrow("a4", listOf(GridPoint(0, 0), GridPoint(7, 0), GridPoint(7, 7), GridPoint(0, 7)), Direction.LEFT),
                Arrow("a5", listOf(GridPoint(8, 0), GridPoint(8, 8)), Direction.DOWN),
                Arrow("a6", listOf(GridPoint(7, 8), GridPoint(0, 8)), Direction.LEFT),
                Arrow("a7", listOf(GridPoint(0, 6), GridPoint(0, 1)), Direction.UP),
                Arrow("a8", listOf(GridPoint(2, 7), GridPoint(2, 6)), Direction.UP),
                Arrow("a9", listOf(GridPoint(4, 7), GridPoint(4, 6)), Direction.UP),
                Arrow("a10", listOf(GridPoint(6, 7), GridPoint(6, 8)), Direction.DOWN),
                Arrow("a11", listOf(GridPoint(1, 0), GridPoint(1, 1)), Direction.DOWN),
                Arrow("a12", listOf(GridPoint(3, 0), GridPoint(3, 1)), Direction.DOWN),
                Arrow("a13", listOf(GridPoint(5, 0), GridPoint(5, 1)), Direction.DOWN),
                Arrow("a14", listOf(GridPoint(7, 2), GridPoint(6, 2)), Direction.LEFT),
                Arrow("a15", listOf(GridPoint(7, 4), GridPoint(6, 4)), Direction.LEFT),
                Arrow("a16", listOf(GridPoint(1, 3), GridPoint(2, 3)), Direction.RIGHT),
                Arrow("a17", listOf(GridPoint(1, 5), GridPoint(2, 5)), Direction.RIGHT),
                Arrow("a18", listOf(GridPoint(3, 5), GridPoint(3, 6)), Direction.DOWN)
            )
        ),

        // Levels 18-20: Expert
        Level(
            id = 18,
            name = "Heart of Arrows",
            gridWidth = 10,
            gridHeight = 10,
            difficulty = Difficulty.Expert,
            rewardRupees = PuzzleEngine.calculateRewardRupees(18),
            arrows = listOf(
                Arrow("a1", listOf(GridPoint(2, 2), GridPoint(1, 1), GridPoint(3, 1), GridPoint(4, 2)), Direction.RIGHT),
                Arrow("a2", listOf(GridPoint(7, 2), GridPoint(8, 1), GridPoint(6, 1), GridPoint(5, 2)), Direction.LEFT),
                Arrow("a3", listOf(GridPoint(4, 3), GridPoint(4, 7), GridPoint(5, 7), GridPoint(5, 3)), Direction.UP),
                Arrow("a4", listOf(GridPoint(1, 2), GridPoint(1, 5), GridPoint(3, 7), GridPoint(4, 8)), Direction.DOWN),
                Arrow("a5", listOf(GridPoint(8, 2), GridPoint(8, 5), GridPoint(6, 7), GridPoint(5, 8)), Direction.DOWN),
                Arrow("a6", listOf(GridPoint(2, 3), GridPoint(3, 3), GridPoint(3, 5)), Direction.DOWN),
                Arrow("a7", listOf(GridPoint(7, 3), GridPoint(6, 3), GridPoint(6, 5)), Direction.DOWN),
                Arrow("a8", listOf(GridPoint(2, 4), GridPoint(2, 5)), Direction.DOWN),
                Arrow("a9", listOf(GridPoint(7, 4), GridPoint(7, 5)), Direction.DOWN),
                Arrow("a10", listOf(GridPoint(3, 6), GridPoint(3, 4)), Direction.UP),
                Arrow("a11", listOf(GridPoint(6, 6), GridPoint(6, 4)), Direction.UP),
                Arrow("a12", listOf(GridPoint(0, 0), GridPoint(9, 0)), Direction.RIGHT),
                Arrow("a13", listOf(GridPoint(9, 1), GridPoint(9, 9)), Direction.DOWN),
                Arrow("a14", listOf(GridPoint(8, 9), GridPoint(0, 9)), Direction.LEFT),
                Arrow("a15", listOf(GridPoint(0, 8), GridPoint(0, 1)), Direction.UP),
                Arrow("a16", listOf(GridPoint(2, 0), GridPoint(2, 1)), Direction.DOWN),
                Arrow("a17", listOf(GridPoint(7, 0), GridPoint(7, 1)), Direction.DOWN),
                Arrow("a18", listOf(GridPoint(4, 9), GridPoint(4, 8)), Direction.UP),
                Arrow("a19", listOf(GridPoint(5, 9), GridPoint(5, 8)), Direction.UP),
                Arrow("a20", listOf(GridPoint(1, 8), GridPoint(1, 9)), Direction.DOWN),
                Arrow("a21", listOf(GridPoint(8, 8), GridPoint(8, 9)), Direction.DOWN)
            )
        ),
        Level(
            id = 19,
            name = "Diamond Maze",
            gridWidth = 10,
            gridHeight = 10,
            difficulty = Difficulty.Expert,
            rewardRupees = PuzzleEngine.calculateRewardRupees(19),
            arrows = listOf(
                Arrow("a1", listOf(GridPoint(4, 1), GridPoint(7, 4), GridPoint(4, 7), GridPoint(1, 4), GridPoint(3, 2)), Direction.RIGHT),
                Arrow("a2", listOf(GridPoint(5, 2), GridPoint(7, 4), GridPoint(5, 6), GridPoint(3, 4)), Direction.LEFT),
                Arrow("a3", listOf(GridPoint(4, 3), GridPoint(5, 3), GridPoint(5, 4), GridPoint(4, 4)), Direction.LEFT),
                Arrow("a4", listOf(GridPoint(4, 0), GridPoint(8, 0), GridPoint(8, 4)), Direction.DOWN),
                Arrow("a5", listOf(GridPoint(9, 4), GridPoint(9, 8), GridPoint(5, 8)), Direction.LEFT),
                Arrow("a6", listOf(GridPoint(4, 9), GridPoint(0, 9), GridPoint(0, 5)), Direction.UP),
                Arrow("a7", listOf(GridPoint(0, 4), GridPoint(0, 0), GridPoint(3, 0)), Direction.RIGHT),
                Arrow("a8", listOf(GridPoint(2, 1), GridPoint(2, 3)), Direction.DOWN),
                Arrow("a9", listOf(GridPoint(7, 1), GridPoint(7, 3)), Direction.DOWN),
                Arrow("a10", listOf(GridPoint(2, 8), GridPoint(2, 6)), Direction.UP),
                Arrow("a11", listOf(GridPoint(7, 8), GridPoint(7, 6)), Direction.UP),
                Arrow("a12", listOf(GridPoint(1, 5), GridPoint(1, 7)), Direction.DOWN),
                Arrow("a13", listOf(GridPoint(8, 5), GridPoint(8, 7)), Direction.DOWN),
                Arrow("a14", listOf(GridPoint(5, 0), GridPoint(5, 1)), Direction.DOWN),
                Arrow("a15", listOf(GridPoint(4, 8), GridPoint(4, 9)), Direction.DOWN),
                Arrow("a16", listOf(GridPoint(6, 2), GridPoint(6, 1)), Direction.UP),
                Arrow("a17", listOf(GridPoint(3, 7), GridPoint(3, 8)), Direction.DOWN),
                Arrow("a18", listOf(GridPoint(6, 7), GridPoint(6, 8)), Direction.DOWN),
                Arrow("a19", listOf(GridPoint(9, 0), GridPoint(9, 3)), Direction.DOWN),
                Arrow("a20", listOf(GridPoint(0, 8), GridPoint(0, 6)), Direction.UP),
                Arrow("a21", listOf(GridPoint(3, 3), GridPoint(4, 3)), Direction.RIGHT),
                Arrow("a22", listOf(GridPoint(5, 5), GridPoint(4, 5)), Direction.LEFT)
            )
        ),
        Level(
            id = 20,
            name = "Master Escape",
            gridWidth = 10,
            gridHeight = 10,
            difficulty = Difficulty.Expert,
            rewardRupees = PuzzleEngine.calculateRewardRupees(20),
            arrows = listOf(
                Arrow("a1", listOf(GridPoint(3, 3), GridPoint(6, 3), GridPoint(6, 6), GridPoint(3, 6), GridPoint(3, 4)), Direction.UP),
                Arrow("a2", listOf(GridPoint(4, 4), GridPoint(5, 4), GridPoint(5, 5), GridPoint(4, 5)), Direction.LEFT),
                Arrow("a3", listOf(GridPoint(2, 2), GridPoint(7, 2), GridPoint(7, 7), GridPoint(2, 7), GridPoint(2, 3)), Direction.UP),
                Arrow("a4", listOf(GridPoint(1, 1), GridPoint(8, 1), GridPoint(8, 8), GridPoint(1, 8), GridPoint(1, 2)), Direction.UP),
                Arrow("a5", listOf(GridPoint(0, 0), GridPoint(9, 0), GridPoint(9, 9)), Direction.DOWN),
                Arrow("a6", listOf(GridPoint(8, 9), GridPoint(0, 9), GridPoint(0, 1)), Direction.UP),
                Arrow("a7", listOf(GridPoint(3, 1), GridPoint(3, 0)), Direction.UP),
                Arrow("a8", listOf(GridPoint(5, 1), GridPoint(5, 0)), Direction.UP),
                Arrow("a9", listOf(GridPoint(7, 1), GridPoint(7, 0)), Direction.UP),
                Arrow("a10", listOf(GridPoint(2, 8), GridPoint(2, 9)), Direction.DOWN),
                Arrow("a11", listOf(GridPoint(4, 8), GridPoint(4, 9)), Direction.DOWN),
                Arrow("a12", listOf(GridPoint(6, 8), GridPoint(6, 9)), Direction.DOWN),
                Arrow("a13", listOf(GridPoint(8, 3), GridPoint(9, 3)), Direction.RIGHT),
                Arrow("a14", listOf(GridPoint(8, 5), GridPoint(9, 5)), Direction.RIGHT),
                Arrow("a15", listOf(GridPoint(8, 7), GridPoint(9, 7)), Direction.RIGHT),
                Arrow("a16", listOf(GridPoint(1, 4), GridPoint(0, 4)), Direction.LEFT),
                Arrow("a17", listOf(GridPoint(1, 6), GridPoint(0, 6)), Direction.LEFT),
                Arrow("a18", listOf(GridPoint(4, 2), GridPoint(4, 1)), Direction.UP),
                Arrow("a19", listOf(GridPoint(5, 7), GridPoint(5, 8)), Direction.DOWN),
                Arrow("a20", listOf(GridPoint(7, 4), GridPoint(8, 4)), Direction.RIGHT),
                Arrow("a21", listOf(GridPoint(2, 5), GridPoint(1, 5)), Direction.LEFT),
                Arrow("a22", listOf(GridPoint(4, 3), GridPoint(4, 2)), Direction.UP),
                Arrow("a23", listOf(GridPoint(5, 6), GridPoint(5, 7)), Direction.DOWN),
                Arrow("a24", listOf(GridPoint(6, 4), GridPoint(7, 4)), Direction.RIGHT)
            )
        )
    )

    fun getAllLevels(): List<Level> = levels

    fun getLevel(id: Int): Level? = levels.find { it.id == id }
}
