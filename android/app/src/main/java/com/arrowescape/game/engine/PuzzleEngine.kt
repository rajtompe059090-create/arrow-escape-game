package com.arrowescape.game.engine

import com.arrowescape.game.model.Arrow
import com.arrowescape.game.model.Difficulty
import com.arrowescape.game.model.Direction
import com.arrowescape.game.model.GridPoint

data class PathCheckResult(
    val isClear: Boolean,
    val blockingArrowId: String? = null,
    val blockingPoint: GridPoint? = null
)

data class SolverMetrics(
    val isSolvable: Boolean,
    val solutionDepth: Int,
    val dependencyDepth: Int,
    val initialFreeCount: Int,
    val maxBranchingFactor: Int,
    val solutionOrder: List<String>
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

    fun findAllFreeArrows(
        arrows: List<Arrow>,
        gridWidth: Int,
        gridHeight: Int
    ): List<Arrow> {
        return arrows.filter { arrow ->
            isArrowPathClear(arrow, arrows, gridWidth, gridHeight).isClear
        }
    }

    /**
     * Algorithmically analyzes a level to measure its solution depth, dependency depth,
     * branching factor, and initial free count.
     */
    fun analyzePuzzle(
        arrows: List<Arrow>,
        gridWidth: Int,
        gridHeight: Int
    ): SolverMetrics {
        if (arrows.isEmpty()) {
            return SolverMetrics(
                isSolvable = true,
                solutionDepth = 0,
                dependencyDepth = 0,
                initialFreeCount = 0,
                maxBranchingFactor = 0,
                solutionOrder = emptyList()
            )
        }

        var remaining = arrows.toList()
        val solutionOrder = ArrayList<String>()
        var initialFreeCount = 0
        var maxBranchingFactor = 0
        var stepCount = 0

        val initialFree = findAllFreeArrows(remaining, gridWidth, gridHeight)
        initialFreeCount = initialFree.size

        while (remaining.isNotEmpty()) {
            val currentFree = findAllFreeArrows(remaining, gridWidth, gridHeight)
            if (currentFree.isEmpty()) {
                // Deadlock / unsolvable
                return SolverMetrics(
                    isSolvable = false,
                    solutionDepth = solutionOrder.size,
                    dependencyDepth = 0,
                    initialFreeCount = initialFreeCount,
                    maxBranchingFactor = maxBranchingFactor,
                    solutionOrder = solutionOrder
                )
            }

            maxBranchingFactor = maxOf(maxBranchingFactor, currentFree.size)
            // Pick the first free arrow
            val picked = currentFree.first()
            solutionOrder.add(picked.id)
            remaining = remaining.filter { it.id != picked.id }
            stepCount++
        }

        // Calculate dependency depth using the direct blocker graph
        val blockerMap = HashMap<String, MutableSet<String>>()
        for (arrow in arrows) {
            val res = isArrowPathClear(arrow, arrows, gridWidth, gridHeight)
            if (!res.isClear && res.blockingArrowId != null) {
                blockerMap.getOrPut(arrow.id) { HashSet() }.add(res.blockingArrowId)
            }
        }

        var maxDep = 1
        for (arrow in arrows) {
            var curr = arrow.id
            var depth = 1
            val visited = HashSet<String>()
            visited.add(curr)

            while (true) {
                val nextBlocker = blockerMap[curr]?.firstOrNull { !visited.contains(it) } ?: break
                visited.add(nextBlocker)
                curr = nextBlocker
                depth++
            }
            maxDep = maxOf(maxDep, depth)
        }

        return SolverMetrics(
            isSolvable = true,
            solutionDepth = solutionOrder.size,
            dependencyDepth = maxDep,
            initialFreeCount = initialFreeCount,
            maxBranchingFactor = maxBranchingFactor,
            solutionOrder = solutionOrder
        )
    }

    /**
     * Algorithmically verifies that a level has at least one valid solution sequence to completion.
     */
    fun isLevelSolvable(
        arrows: List<Arrow>,
        gridWidth: Int,
        gridHeight: Int
    ): Boolean {
        return analyzePuzzle(arrows, gridWidth, gridHeight).isSolvable
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

    fun calculateRewardRupees(levelId: Int): Double {
        return Difficulty.fromLevel(levelId).rewardRupees
    }
}
