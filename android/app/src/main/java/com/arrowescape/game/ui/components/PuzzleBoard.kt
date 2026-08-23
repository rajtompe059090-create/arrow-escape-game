package com.arrowescape.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
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
                .pointerInput(arrows) {
                    detectTapGestures { tapOffset ->
                        val cellW = size.width / (gridWidth - 1).coerceAtLeast(1)
                        val cellH = size.height / (gridHeight - 1).coerceAtLeast(1)

                        val hit = arrows.findLast { arrow ->
                            arrow.points.any { pt ->
                                val px = pt.x * cellW
                                val py = pt.y * cellH
                                val dist = sqrt((tapOffset.x - px) * (tapOffset.x - px) + (tapOffset.y - py) * (tapOffset.y - py))
                                dist <= cellW * 0.65f
                            }
                        }
                        hit?.let { onArrowTapped(it) }
                    }
                }
        ) {
            val cellW = size.width / (gridWidth - 1).coerceAtLeast(1)
            val cellH = size.height / (gridHeight - 1).coerceAtLeast(1)

            // Draw grid dots
            for (gx in 0 until gridWidth) {
                for (gy in 0 until gridHeight) {
                    drawCircle(
                        color = dotColor,
                        radius = 3.dp.toPx(),
                        center = Offset(gx * cellW, gy * cellH)
                    )
                }
            }

            // Draw Arrows
            arrows.forEach { arrow ->
                val isEscaping = escapingArrowIds.contains(arrow.id)
                val isBlocked = blockedArrowId == arrow.id
                val isHinted = hintedArrowId == arrow.id

                val color = when {
                    isBlocked -> blockedColor
                    isEscaping || isHinted -> escapeColor
                    else -> navyColor
                }

                val strokeWidth = if (isHinted) 7.dp.toPx() else 5.5.dp.toPx()

                // Draw Path
                if (arrow.points.size >= 2) {
                    val path = Path().apply {
                        val first = arrow.points.first()
                        moveTo(first.x * cellW, first.y * cellH)
                        for (i in 1 until arrow.points.size) {
                            val pt = arrow.points[i]
                            lineTo(pt.x * cellW, pt.y * cellH)
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

                // Draw Arrowhead
                val head = arrow.points.last()
                val hx = head.x * cellW
                val hy = head.y * cellH
                val headLen = 14.dp.toPx()
                val headHalf = 8.dp.toPx()

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

                drawPath(path = headPath, color = color)
            }
        }
    }
}
