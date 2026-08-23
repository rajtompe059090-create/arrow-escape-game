package com.arrowescape.game.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.arrowescape.game.model.Arrow
import com.arrowescape.game.model.Direction
import kotlin.math.abs
import kotlin.math.sqrt

@Composable
fun PuzzleBoard(
    gridWidth: Int,
    gridHeight: Int,
    arrows: List<Arrow>,
    escapingArrowIds: Set<String>,
    blockedArrowId: String?,
    hintedArrowId: String?,
    onArrowTapped: (Arrow) -> Unit,
    modifier: Modifier = Modifier
) {
    val navyColor = Color(0xFF1E293B)
    val escapeColor = Color(0xFF0284C7)
    val blockedColor = Color(0xFFEF4444)
    val dotColor = Color(0xFFE2E8F0)

    val escapeProgress = remember {
        Animatable(0f)
    }

    val escapingArrow = arrows.firstOrNull {
        escapingArrowIds.contains(it.id)
    }

    LaunchedEffect(escapingArrow?.id) {
        if (escapingArrow != null) {
            escapeProgress.snapTo(0f)

            escapeProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 550
                )
            )
        } else {
            escapeProgress.snapTo(0f)
        }
    }

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(20.dp)
                .pointerInput(arrows, escapingArrowIds) {
                    detectTapGestures { tapOffset ->

                        val cellW =
                            size.width / (gridWidth - 1).coerceAtLeast(1)

                        val cellH =
                            size.height / (gridHeight - 1).coerceAtLeast(1)

                        val hitRadius =
                            minOf(cellW, cellH) * 0.75f

                        val hit = arrows
                            .asReversed()
                            .firstOrNull { arrow ->

                                if (escapingArrowIds.contains(arrow.id)) {
                                    false
                                } else {

                                    // Check every segment of the arrow.
                                    arrow.points.zipWithNext().any { segment ->

                                        val p1 = segment.first
                                        val p2 = segment.second

                                        val x1 =
                                            p1.x * cellW

                                        val y1 =
                                            p1.y * cellH

                                        val x2 =
                                            p2.x * cellW

                                        val y2 =
                                            p2.y * cellH

                                        distanceToSegment(
                                            tapOffset.x,
                                            tapOffset.y,
                                            x1,
                                            y1,
                                            x2,
                                            y2
                                        ) <= hitRadius
                                    }
                                }
                            }

                        hit?.let {
                            onArrowTapped(it)
                        }
                    }
                }
        ) {

            val cellW =
                size.width / (gridWidth - 1).coerceAtLeast(1)

            val cellH =
                size.height / (gridHeight - 1).coerceAtLeast(1)

            // -----------------------------
            // GRID
            // -----------------------------

            for (gx in 0 until gridWidth) {
                for (gy in 0 until gridHeight) {

                    drawCircle(
                        color = dotColor,
                        radius = 3.dp.toPx(),
                        center = Offset(
                            gx * cellW,
                            gy * cellH
                        )
                    )
                }
            }

            // -----------------------------
            // ARROWS
            // -----------------------------

            arrows.forEach { arrow ->

                val isEscaping =
                    escapingArrowIds.contains(arrow.id)

                val isBlocked =
                    blockedArrowId == arrow.id

                val isHinted =
                    hintedArrowId == arrow.id

                val color = when {
                    isBlocked -> blockedColor
                    isEscaping || isHinted -> escapeColor
                    else -> navyColor
                }

                val strokeWidth =
                    if (isHinted) {
                        7.dp.toPx()
                    } else {
                        5.5.dp.toPx()
                    }

                // -----------------------------
                // ESCAPE MOVEMENT
                // -----------------------------

                var offsetX = 0f
                var offsetY = 0f

                if (isEscaping) {

                    val distanceX =
                        size.width + cellW * 3f

                    val distanceY =
                        size.height + cellH * 3f

                    when (arrow.headDirection) {

                        Direction.UP -> {
                            offsetY =
                                -distanceY * escapeProgress.value
                        }

                        Direction.DOWN -> {
                            offsetY =
                                distanceY * escapeProgress.value
                        }

                        Direction.LEFT -> {
                            offsetX =
                                -distanceX * escapeProgress.value
                        }

                        Direction.RIGHT -> {
                            offsetX =
                                distanceX * escapeProgress.value
                        }
                    }
                }

                // -----------------------------
                // ARROW PATH
                // -----------------------------

                if (arrow.points.size >= 2) {

                    val path = Path().apply {

                        val first =
                            arrow.points.first()

                        moveTo(
                            first.x * cellW + offsetX,
                            first.y * cellH + offsetY
                        )

                        for (i in 1 until arrow.points.size) {

                            val pt =
                                arrow.points[i]

                            lineTo(
                                pt.x * cellW + offsetX,
                                pt.y * cellH + offsetY
                            )
                        }
                    }

                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }

                // -----------------------------
                // ARROW HEAD
                // -----------------------------

                val head =
                    arrow.points.last()

                val hx =
                    head.x * cellW + offsetX

                val hy =
                    head.y * cellH + offsetY

                val headLen =
                    14.dp.toPx()

                val headHalf =
                    8.dp.toPx()

                val headPath =
                    Path().apply {

                        when (arrow.headDirection) {

                            Direction.UP -> {
                                moveTo(
                                    hx,
                                    hy - 2.dp.toPx()
                                )

                                lineTo(
                                    hx - headHalf,
                                    hy + headLen
                                )

                                lineTo(
                                    hx + headHalf,
                                    hy + headLen
                                )
                            }

                            Direction.DOWN -> {
                                moveTo(
                                    hx,
                                    hy + 2.dp.toPx()
                                )

                                lineTo(
                                    hx - headHalf,
                                    hy - headLen
                                )

                                lineTo(
                                    hx + headHalf,
                                    hy - headLen
                                )
                            }

                            Direction.LEFT -> {
                                moveTo(
                                    hx - 2.dp.toPx(),
                                    hy
                                )

                                lineTo(
                                    hx + headLen,
                                    hy - headHalf
                                )

                                lineTo(
                                    hx + headLen,
                                    hy + headHalf
                                )
                            }

                            Direction.RIGHT -> {
                                moveTo(
                                    hx + 2.dp.toPx(),
                                    hy
                                )

                                lineTo(
                                    hx - headLen,
                                    hy - headHalf
                                )

                                lineTo(
                                    hx - headLen,
                                    hy + headHalf
                                )
                            }
                        }

                        close()
                    }

                drawPath(
                    path = headPath,
                    color = color
                )
            }
        }
    }
}

/**
 * Returns the shortest distance between a point
 * and a line segment.
 *
 * This makes straight + L-shaped arrows
 * properly touchable.
 */
private fun distanceToSegment(
    px: Float,
    py: Float,
    x1: Float,
    y1: Float,
    x2: Float,
    y2: Float
): Float {

    val dx = x2 - x1
    val dy = y2 - y1

    if (dx == 0f && dy == 0f) {
        return sqrt(
            (px - x1) * (px - x1) +
                    (py - y1) * (py - y1)
        )
    }

    val lengthSquared =
        dx * dx + dy * dy

    var t =
        ((px - x1) * dx +
                (py - y1) * dy) /
                lengthSquared

    t = t.coerceIn(0f, 1f)

    val nearestX =
        x1 + t * dx

    val nearestY =
        y1 + t * dy

    return sqrt(
        (px - nearestX) *
                (px - nearestX) +
                (py - nearestY) *
                (py - nearestY)
    )
}
