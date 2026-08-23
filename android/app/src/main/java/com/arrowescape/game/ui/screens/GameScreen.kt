package com.arrowescape.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
    modifier: Modifier = Modifier
) {
    val level = uiState.currentLevel ?: return

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // App Bar
        Surface(
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
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

                // Remaining Arrows Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Text(
                        text = "${uiState.remainingArrows.size} left",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155)
                    )
                }
            }
        }

        // Stats & 3 Hearts Indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Difficulty Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFE0F2FE)
            ) {
                Text(
                    text = level.difficulty.name,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0369A1)
                )
            }

            // Lives
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (i in 1..3) {
                    val isAlive = i <= uiState.lives
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Heart $i",
                        tint = if (isAlive) Color(0xFFEF4444) else Color(0xFFCBD5E1),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Puzzle Board
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

        // Bottom Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hint Button
            BadgedBox(
                badge = {
                    Badge(containerColor = Color(0xFF0284C7)) {
                        Text("${uiState.hintsRemaining}")
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
                        enabled = uiState.hintsRemaining > 0 && uiState.lives > 0
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Hint",
                            tint = if (uiState.hintsRemaining > 0) Color(0xFF0284C7) else Color(0xFF94A3B8)
                        )
                    }
                }
            }

            // Retry Button
            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier.size(56.dp)
            ) {
                IconButton(onClick = onRestartLevel) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Restart",
                        tint = Color(0xFF334155)
                    )
                }
            }

            // Levels Grid Button
            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier.size(56.dp)
            ) {
                IconButton(onClick = onOpenLevels) {
                    Icon(
                        imageVector = Icons.Default.GridOn,
                        contentDescription = "Levels",
                        tint = Color(0xFF334155)
                    )
                }
            }
        }
    }
}
