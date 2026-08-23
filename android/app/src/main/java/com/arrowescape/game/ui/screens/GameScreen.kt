package com.arrowescape.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arrowescape.game.model.Arrow
import com.arrowescape.game.ui.components.PuzzleBoard
import com.arrowescape.game.viewmodel.GameUiState

@Composable
fun GameScreen(
    uiState: GameUiState,
    onArrowTapped: (Arrow) -> Unit,
    onUseHint: () -> Unit,
    onRestartLevel: () -> Unit,
    onOpenLevels: () -> Unit,
    onBack: () -> Unit,
    onNextLevel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val level = uiState.currentLevel ?: return

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --------------------------------
            // TOP BAR
            // --------------------------------

            Surface(
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 12.dp,
                            vertical = 8.dp
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    IconButton(
                        onClick = onBack
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = level.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF64748B)
                        )

                        Text(
                            text = "Level ${level.id}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = "${uiState.remainingArrows.size} left",
                            modifier = Modifier.padding(
                                horizontal = 10.dp,
                                vertical = 6.dp
                            ),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )
                    }
                }
            }

            // --------------------------------
            // STATS
            // --------------------------------

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 24.dp,
                        vertical = 6.dp
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE0F2FE)
                ) {
                    Text(
                        text = level.difficulty.name,
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 4.dp
                        ),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0369A1)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (i in 1..3) {

                        val isAlive =
                            i <= uiState.lives

                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Heart $i",
                            tint = if (isAlive) {
                                Color(0xFFEF4444)
                            } else {
                                Color(0xFFCBD5E1)
                            },
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // --------------------------------
            // PUZZLE BOARD
            // --------------------------------

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {

                PuzzleBoard(
                    gridWidth = level.gridWidth,
                    gridHeight = level.gridHeight,
                    arrows = uiState.remainingArrows,
                    escapingArrowIds = uiState.escapingArrowIds,
                    blockedArrowId = uiState.blockedArrowId,
                    hintedArrowId = uiState.hintedArrowId,
                    onArrowTapped = onArrowTapped
                )
            }

            // --------------------------------
            // BOTTOM CONTROLS
            // --------------------------------

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // HINT

                BadgedBox(
                    badge = {
                        Badge(
                            containerColor = Color(0xFF0284C7)
                        ) {
                            Text(
                                text = "${uiState.hintsRemaining}"
                            )
                        }
                    }
                ) {

                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 4.dp,
                        modifier = Modifier.size(56.dp)
                    ) {

                        IconButton(
                            onClick = onUseHint,
                            enabled =
                                uiState.hintsRemaining > 0 &&
                                        uiState.lives > 0 &&
                                        !uiState.isLevelCompleted
                        ) {

                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "Hint",
                                tint =
                                    if (uiState.hintsRemaining > 0) {
                                        Color(0xFF0284C7)
                                    } else {
                                        Color(0xFF94A3B8)
                                    }
                            )
                        }
                    }
                }

                // RESTART

                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(56.dp)
                ) {

                    IconButton(
                        onClick = onRestartLevel
                    ) {

                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restart",
                            tint = Color(0xFF334155)
                        )
                    }
                }

                // LEVELS

                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(56.dp)
                ) {

                    IconButton(
                        onClick = onOpenLevels
                    ) {

                        Icon(
                            imageVector = Icons.Default.GridOn,
                            contentDescription = "Levels",
                            tint = Color(0xFF334155)
                        )
                    }
                }
            }
        }

        // ========================================
        // LEVEL COMPLETE OVERLAY
        // ========================================

        if (uiState.isLevelCompleted) {

            Surface(
                color = Color(0x99000000),
                modifier = Modifier.fillMaxSize()
            ) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {

                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        color = Color.White,
                        shadowElevation = 12.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp)
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Text(
                                text = "🎉",
                                fontSize = 56.sp
                            )

                            Spacer(
                                modifier = Modifier.height(8.dp)
                            )

                            Text(
                                text = "Level Complete!",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F172A)
                            )

                            Spacer(
                                modifier = Modifier.height(8.dp)
                            )

                            Text(
                                text = "Level ${level.id} cleared successfully!",
                                fontSize = 15.sp,
                                color = Color(0xFF64748B)
                            )

                            Spacer(
                                modifier = Modifier.height(20.dp)
                            )

                            // REWARD CARD

                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = Color(0xFFE0F2FE),
                                modifier = Modifier.fillMaxWidth()
                            ) {

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {

                                    Text(
                                        text = "LEVEL REWARD",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0369A1)
                                    )

                                    Spacer(
                                        modifier = Modifier.height(4.dp)
                                    )

                                    Text(
                                        text = "₹${calculateReward(level.id)}",
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF0284C7)
                                    )
                                }
                            }

                            Spacer(
                                modifier = Modifier.height(22.dp)
                            )

                            // NEXT LEVEL

                            Button(
                                onClick = onNextLevel,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF0284C7)
                                )
                            ) {

                                Text(
                                    text = "Next Level →",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(
                                modifier = Modifier.height(10.dp)
                            )

                            // REPLAY

                            OutlinedButton(
                                onClick = onRestartLevel,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {

                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Replay"
                                )

                                Spacer(
                                    modifier = Modifier.size(8.dp)
                                )

                                Text(
                                    text = "Replay Level"
                                )
                            }

                            Spacer(
                                modifier = Modifier.height(8.dp)
                            )

                            Text(
                                text = "Moves: ${uiState.movesCount}",
                                fontSize = 13.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Same reward system used by the game.
 */
private fun calculateReward(levelId: Int): Int {
    return when {
        levelId <= 50 -> 2
        levelId <= 100 -> 3
        levelId <= 150 -> 5
        levelId <= 200 -> 10
        else -> 15
    }
}
