package com.arrowescape.game.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
import kotlin.math.sqrt

private fun distanceToSegment(
    px: Float, py: Float,
    x1: Float, y1: Float,
    x2: Float, y2: Float
): Float {
    val dx = x2 - x1
    val dy = y2 - y1
    val lenSq = dx * dx + dy * dy
    if (lenSq == 0f) {
        val dpx = px - x1
        val dpy = py - y1
        return sqrt(dpx * dpx + dpy * dpy)
    }
    val t = (((px - x1) * dx + (py - y1) * dy) / lenSq).coerceIn(0f, 1f)
    val projX = x1 + t * dx
    val projY = y1 + t * dy
    val dpx = px - projX
    val dpy = py - projY
    return sqrt(dpx * dpx + dpy * dpy)
}

private fun distanceToArrow(
    arrow: Arrow,
    tapX: Float,
    tapY: Float,
    cellW: Float,
    cellH: Float,
    headLen: Float
): Float {
    var minD = Float.MAX_VALUE
    val points = arrow.points

    for (i in 0 until points.size - 1) {
        val p1 = points[i]
        val p2 = points[i + 1]
        val x1 = p1.x * cellW
        val y1 = p1.y * cellH
        val x2 = p2.x * cellW
        val y2 = p2.y * cellH
        val d = distanceToSegment(tapX, tapY, x1, y1, x2, y2)
        if (d < minD) {
            minD = d
        }
    }

    if (points.isNotEmpty()) {
        val head = points.last()
        val hx = head.x * cellW
        val hy = head.y * cellH
        val dHead = sqrt((tapX - hx) * (tapX - hx) + (tapY - hy) * (tapY - hy))
        if (dHead < minD) {
            minD = dHead
        }

        val tipX = when (arrow.headDirection) {
            Direction.LEFT -> hx - headLen
            Direction.RIGHT -> hx + headLen
            else -> hx
        }
        val tipY = when (arrow.headDirection) {
            Direction.UP -> hy - headLen
            Direction.DOWN -> hy + headLen
            else -> hy
        }
        val dTip = sqrt((tapX - tipX) * (tapX - tipX) + (tapY - tipY) * (tapY - tipY))
        if (dTip < minD) {
            minD = dTip
        }
    }

    return minD
}

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

    val currentArrows by rememberUpdatedState(arrows)
    val currentEscapingIds by rememberUpdatedState(escapingArrowIds)
    val currentOnArrowTapped by rememberUpdatedState(onArrowTapped)

    val escapeProgress = remember { Animatable(0f) }

    val isAnyEscaping = escapingArrowIds.isNotEmpty()

    LaunchedEffect(isAnyEscaping) {
        if (isAnyEscaping) {
            escapeProgress.snapTo(0f)
            escapeProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 350,
                    easing = FastOutSlowInEasing
                )
            )
        } else {
            escapeProgress.snapTo(0f)
        }
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(14.dp)
                .pointerInput(gridWidth, gridHeight) {
                    detectTapGestures { tapOffset ->
                        val cellW = size.width / (gridWidth - 1).coerceAtLeast(1).toFloat()
                        val cellH = size.height / (gridHeight - 1).coerceAtLeast(1).toFloat()
                        val headLen = (cellW * 0.38f).coerceIn(5.5.dp.toPx(), 16.dp.toPx())

                        val hitThreshold = maxOf(cellW * 0.88f, 26.dp.toPx())

                        var bestArrow: Arrow? = null
                        var minDistance = Float.MAX_VALUE

                        for (arrow in currentArrows) {
                            if (currentEscapingIds.contains(arrow.id)) continue

                            val d = distanceToArrow(
                                arrow = arrow,
                                tapX = tapOffset.x,
                                tapY = tapOffset.y,
                                cellW = cellW,
                                cellH = cellH,
                                headLen = headLen
                            )

                            if (d <= hitThreshold && d < minDistance) {
                                minDistance = d
                                bestArrow = arrow
                            }
                        }

                        bestArrow?.let {
                            currentOnArrowTapped(it)
                        }
                    }
                }
        ) {
            val cellW: Float =
                size.width / (gridWidth - 1).coerceAtLeast(1).toFloat()
            val cellH: Float =
                size.height / (gridHeight - 1).coerceAtLeast(1).toFloat()

            val dotRadius = (cellW * 0.08f).coerceIn(1.2.dp.toPx(), 3.5.dp.toPx())

            // --------------------------------
            // GRID DOTS
            // --------------------------------
            for (gx in 0 until gridWidth) {
                for (gy in 0 until gridHeight) {
                    drawCircle(
                        color = dotColor,
                        radius = dotRadius,
                        center = Offset(
                            gx.toFloat() * cellW,
                            gy.toFloat() * cellH
                        )
                    )
                }
            }

            // --------------------------------
            // DRAW ARROWS
            // --------------------------------
            val baseStrokeWidth = (cellW * 0.16f).coerceIn(2.2.dp.toPx(), 6.5.dp.toPx())
            val headLen = (cellW * 0.38f).coerceIn(5.5.dp.toPx(), 16.dp.toPx())
            val headHalf = headLen * 0.58f

            arrows.forEach { arrow ->
                val isEscaping = escapingArrowIds.contains(arrow.id)
                val isBlocked = blockedArrowId == arrow.id
                val isHinted = hintedArrowId == arrow.id

                val color = when {
                    isBlocked -> blockedColor
                    isEscaping || isHinted -> escapeColor
                    else -> navyColor
                }

                val strokeWidth = if (isHinted) baseStrokeWidth * 1.3f else baseStrokeWidth

                // --------------------------------
                // ESCAPE MOVEMENT
                // --------------------------------
                var offsetX = 0f
                var offsetY = 0f

                if (isEscaping) {
                    val distanceX: Float = size.width + cellW * 2f
                    val distanceY: Float = size.height + cellH * 2f
                    val progress: Float = escapeProgress.value

                    when (arrow.headDirection) {
                        Direction.UP -> offsetY = -distanceY * progress
                        Direction.DOWN -> offsetY = distanceY * progress
                        Direction.LEFT -> offsetX = -distanceX * progress
                        Direction.RIGHT -> offsetX = distanceX * progress
                    }
                }

                // --------------------------------
                // ARROW BODY / PATH
                // --------------------------------
                if (arrow.points.size >= 2) {
                    val path = Path().apply {
                        val first = arrow.points.first()
                        moveTo(
                            first.x.toFloat() * cellW + offsetX,
                            first.y.toFloat() * cellH + offsetY
                        )

                        for (i in 1 until arrow.points.size) {
                            val point = arrow.points[i]
                            lineTo(
                                point.x.toFloat() * cellW + offsetX,
                                point.y.toFloat() * cellH + offsetY
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

                // --------------------------------
                // ARROW HEAD
                // --------------------------------
                val head = arrow.points.last()
                val hx = head.x.toFloat() * cellW + offsetX
                val hy = head.y.toFloat() * cellH + offsetY

                val headPath = Path().apply {
                    when (arrow.headDirection) {
                        Direction.UP -> {
                            moveTo(hx, hy - 2.dp.toPx())
                            lineTo(hx - headHalf, hy + headLen)
                            lineTo(hx + headHalf, hy + headLen)
                        }
                        Direction.DOWN -> {
                            moveTo(hx, hy + 2.dp.toPx())
                            lineTo(hx - headHalf, hy - headLen)
                            lineTo(hx + headHalf, hy - headLen)
                        }
                        Direction.LEFT -> {
                            moveTo(hx - 2.dp.toPx(), hy)
                            lineTo(hx + headLen, hy - headHalf)
                            lineTo(hx + headLen, hy + headHalf)
                        }
                        Direction.RIGHT -> {
                            moveTo(hx + 2.dp.toPx(), hy)
                            lineTo(hx - headLen, hy - headHalf)
                            lineTo(hx - headLen, hy + headHalf)
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
