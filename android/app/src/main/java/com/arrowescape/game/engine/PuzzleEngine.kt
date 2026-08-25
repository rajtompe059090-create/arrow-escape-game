package com.arrowescape.game.engine

import com.arrowescape.game.model.Arrow
import com.arrowescape.game.model.Direction
import com.arrowescape.game.model.GridPoint

data class PathCheckResult(
    val isClear: Boolean,
    val blockingArrowId: String? = null,
    val blockingPoint: GridPoint? = null
)

/**
 * Pure Kotlin Puzzle Engine for Arrow Escape.
 * Handles collision raycasting, path verification, hint discovery, and level solvability.
 */
object PuzzleEngine {

    fun isArrowPathClear(
        arrow: Arrow,
        allArrows: List<Arrow>,
        gridWidth: Int,
        gridHeight: Int
    ): PathCheckResult {
        val head = arrow.points.last()
        var dx = 0
        var dy = 0

        when (arrow.headDirection) {
            Direction.UP -> dy = -1
            Direction.DOWN -> dy = 1
            Direction.LEFT -> dx = -1
            Direction.RIGHT -> dx = 1
        }

        var checkX = head.x + dx
        var checkY = head.y + dy

        // Raycast straight forward until the board boundary
        while (checkX in 0 until gridWidth && checkY in 0 until gridHeight) {
            val targetPoint = GridPoint(checkX, checkY)

            for (other in allArrows) {
                if (other.id == arrow.id) continue
                val occupied = getAllOccupiedPoints(other)
                if (occupied.any { it.x == targetPoint.x && it.y == targetPoint.y }) {
                    return PathCheckResult(
                        isClear = false,
                        blockingArrowId = other.id,
                        blockingPoint = targetPoint
                    )
                }
            }

            checkX += dx
            checkY += dy
        }

        return PathCheckResult(isClear = true)
    }

    fun findFreeArrow(
        arrows: List<Arrow>,
        gridWidth: Int,
        gridHeight: Int
    ): Arrow? {
        return arrows.firstOrNull { arrow ->
            isArrowPathClear(arrow, arrows, gridWidth, gridHeight).isClear
        }
    }

    /**
     * Algorithmically verifies that a level has at least one valid solution sequence to completion.
     */
    fun isLevelSolvable(
        arrows: List<Arrow>,
        gridWidth: Int,
        gridHeight: Int
    ): Boolean {
        var remaining = arrows.toList()
        val maxSteps = remaining.size + 5
        var steps = 0

        while (remaining.isNotEmpty() && steps < maxSteps) {
            val free = findFreeArrow(remaining, gridWidth, gridHeight) ?: return false
            remaining = remaining.filter { it.id != free.id }
            steps++
        }

        return remaining.isEmpty()
    }

    fun getAllOccupiedPoints(arrow: Arrow): List<GridPoint> {
        val pointsSet = LinkedHashSet<GridPoint>()
        for (i in 0 until arrow.points.size - 1) {
            val p1 = arrow.points[i]
            val p2 = arrow.points[i + 1]

            val dx = (p2.x - p1.x).coerceIn(-1, 1)
            val dy = (p2.y - p1.y).coerceIn(-1, 1)

            var curX = p1.x
            var curY = p1.y
            pointsSet.add(GridPoint(curX, curY))

            while (curX != p2.x || curY != p2.y) {
                curX += dx
                curY += dy
                pointsSet.add(GridPoint(curX, curY))
            }
        }
        return pointsSet.toList()
    }

    fun calculateRewardRupees(levelId: Int): Int {
        return when {
            levelId <= 50 -> 2
            levelId <= 100 -> 3
            levelId <= 150 -> 5
            levelId <= 200 -> 10
            else -> 15
        }
    }
}
