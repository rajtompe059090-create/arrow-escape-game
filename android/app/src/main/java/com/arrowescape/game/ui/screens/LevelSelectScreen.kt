package com.arrowescape.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arrowescape.game.data.LevelRepository
import com.arrowescape.game.model.Difficulty
import com.arrowescape.game.viewmodel.GameUiState

@Composable
fun LevelSelectScreen(
    uiState: GameUiState,
    onSelectLevel: (Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val initialDifficulty = remember(uiState.unlockedLevel) {
        Difficulty.fromLevel(uiState.unlockedLevel)
    }

    var selectedDifficulty by remember { mutableStateOf(initialDifficulty) }
    val tabScrollState = rememberScrollState()

    val levelIds = remember(selectedDifficulty) {
        LevelRepository.getLevelsForTier(selectedDifficulty)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
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
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Column(modifier = Modifier.padding(start = 4.dp)) {
                    Text(
                        text = "Select Level",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Unlocked: Level ${uiState.unlockedLevel} • ₹${"%.2f".format(selectedDifficulty.rewardRupees)}/level",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Difficulty Tier Tabs (EASY, NORMAL, HARD, VERY HARD, EXTREME)
        Surface(
            color = Color.White,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(tabScrollState)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Difficulty.values().forEach { diff ->
                    val isSelected = selectedDifficulty == diff
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) Color(0xFF0284C7) else Color(0xFFF1F5F9),
                        modifier = Modifier.clickable { selectedDifficulty = diff }
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = diff.displayName.uppercase(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isSelected) Color.White else Color(0xFF475569)
                            )
                            Text(
                                text = "${diff.levelRange} (₹${"%.2f".format(diff.rewardRupees)})",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) Color(0xFFE0F2FE) else Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        }

        // Levels Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(levelIds) { levelId ->
                val isUnlocked = levelId <= uiState.unlockedLevel
                val isCurrent = levelId == uiState.unlockedLevel
                val isCompleted = uiState.completedLevels.contains(levelId)

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = when {
                        isCurrent -> Color(0xFFE0F2FE)
                        isCompleted -> Color(0xFFF0FDF4)
                        isUnlocked -> Color.White
                        else -> Color(0xFFF1F5F9)
                    },
                    shadowElevation = if (isUnlocked) 3.dp else 0.dp,
                    modifier = Modifier
                        .aspectRatio(1f)
                        .then(
                            if (isCurrent) {
                                Modifier.border(2.dp, Color(0xFF0284C7), RoundedCornerShape(20.dp))
                            } else {
                                Modifier
                            }
                        )
                        .clickable(enabled = isUnlocked) { onSelectLevel(levelId) }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (!isUnlocked) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$levelId",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp,
                                    color = when {
                                        isCurrent -> Color(0xFF0284C7)
                                        isCompleted -> Color(0xFF15803D)
                                        else -> Color(0xFF0F172A)
                                    }
                                )
                                if (isCompleted) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Completed",
                                        tint = Color(0xFF16A34A),
                                        modifier = Modifier.size(13.dp)
                                    )
                                } else if (isCurrent) {
                                    Text(
                                        text = "PLAY",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF0284C7)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
