package com.arrowescape.game.ui.screens

import android.app.Activity
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arrowescape.game.ads.AdManager
import com.arrowescape.game.ads.AdMobBannerView
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
    val context = LocalContext.current
    val activity = context as? Activity

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ==============================
            // TOP ADMOB BANNER
            // ==============================
            AdMobBannerView(
                adUnitId = AdManager.TOP_BANNER_ID,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            )

            // ==============================
            // GAME TOP BAR
            // ==============================
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
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
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

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF1F5F9)
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

            // ==============================
            // STATS BAR (Difficulty & Lives)
            // ==============================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE0F2FE)
                ) {
                    Text(
                        text = "${level.difficulty.displayName.uppercase()} (₹${level.rewardRupees})",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0369A1)
                    )
                }

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

            // ==============================
            // PUZZLE BOARD AREA
            // ==============================
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

            // ==============================
            // GAMEPLAY CONTROLS
            // ==============================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // HINT REWARDED AD BUTTON
                BadgedBox(
                    badge = {
                        Badge(containerColor = Color(0xFF0284C7)) {
                            Text(text = "${uiState.hintsRemaining}")
                        }
                    }
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 4.dp,
                        modifier = Modifier.size(54.dp)
                    ) {
                        IconButton(
                            onClick = {
                                if (activity != null) {
                                    AdManager.showRewarded(
                                        activity = activity,
                                        onUserEarnedReward = {
                                            onUseHint()
                                        },
                                        onAdDismissed = {}
                                    )
                                } else {
                                    onUseHint()
                                }
                            },
                            enabled = uiState.lives > 0 && !uiState.isLevelCompleted
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "Hint (Rewarded)",
                                tint = Color(0xFF0284C7)
                            )
                        }
                    }
                }

                // RESTART BUTTON
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(54.dp)
                ) {
                    IconButton(onClick = onRestartLevel) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restart",
                            tint = Color(0xFF334155)
                        )
                    }
                }

                // LEVEL SELECT BUTTON
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(54.dp)
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

            // ==============================
            // BOTTOM ADMOB BANNER
            // ==============================
            AdMobBannerView(
                adUnitId = AdManager.BOTTOM_BANNER_ID,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            )
        }

        // ========================================
        // LEVEL COMPLETE OVERLAY DIALOG
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
                                .padding(26.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🎉", fontSize = 54.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Level Complete!",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Level ${level.id} cleared successfully!",
                                fontSize = 14.sp,
                                color = Color(0xFF64748B)
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // REWARD EARNED CARD
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = Color(0xFFE0F2FE),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "LEVEL CASH REWARD",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0369A1)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "₹${level.rewardRupees}",
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF0284C7)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // NEXT LEVEL BUTTON (Plays Interstitial Ad first if available)
                            Button(
                                onClick = {
                                    if (activity != null) {
                                        AdManager.showInterstitial(activity) {
                                            onNextLevel()
                                        }
                                    } else {
                                        onNextLevel()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                            ) {
                                Text(
                                    text = "Next Level →",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // REPLAY BUTTON
                            OutlinedButton(
                                onClick = onRestartLevel,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Replay"
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(text = "Replay Level")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Moves: ${uiState.movesCount}",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }
        }
    }
}
