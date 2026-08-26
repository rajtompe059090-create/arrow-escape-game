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

    val escapingArrow = arrows.firstOrNull { arrow ->
        escapingArrowIds.contains(arrow.id)
    }

    LaunchedEffect(escapingArrow?.id) {
        if (escapingArrow != null) {
            escapeProgress.snapTo(0f)
            escapeProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 450
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
                .pointerInput(arrows, escapingArrowIds, gridWidth, gridHeight) {
                    detectTapGestures { tapOffset ->
                        val cellW: Float =
                            size.width / (gridWidth - 1).coerceAtLeast(1).toFloat()
                        val cellH: Float =
                            size.height / (gridHeight - 1).coerceAtLeast(1).toFloat()

                        val hitThreshold = maxOf(cellW * 0.72f, 16.dp.toPx())

                        val hit = arrows.findLast { arrow ->
                            if (escapingArrowIds.contains(arrow.id)) {
                                false
                            } else {
                                arrow.points.any { point ->
                                    val px = point.x.toFloat() * cellW
                                    val py = point.y.toFloat() * cellH
                                    val dx = tapOffset.x - px
                                    val dy = tapOffset.y - py
                                    val distance = sqrt(dx * dx + dy * dy)
                                    distance <= hitThreshold
                                }
                            }
                        }

                        hit?.let {
                            onArrowTapped(it)
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
