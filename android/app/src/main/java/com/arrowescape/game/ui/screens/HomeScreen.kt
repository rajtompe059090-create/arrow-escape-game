package com.arrowescape.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arrowescape.game.model.Difficulty
import com.arrowescape.game.sound.SoundManager
import com.arrowescape.game.viewmodel.GameUiState

@Composable
fun HomeScreen(
    uiState: GameUiState,
    onPlayContinue: () -> Unit,
    onOpenLevels: () -> Unit,
    onOpenWallet: () -> Unit,
    onOpenDailyReward: () -> Unit,
    onOpenWeeklyDashboard: () -> Unit,
    onOpenRewards: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSupport: () -> Unit,
    onToggleSound: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentLevel = if (uiState.unlockedLevel > 0) uiState.unlockedLevel else 1
    val difficulty = Difficulty.fromLevel(currentLevel)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // TOP BAR: Audio Quick Toggle, App Branding, Settings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    shadowElevation = 1.dp,
                    modifier = Modifier.size(42.dp)
                ) {
                    IconButton(onClick = {
                        SoundManager.playTap()
                        onToggleSound()
                    }) {
                        Icon(
                            imageVector = if (uiState.soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                            contentDescription = "Sound Toggle",
                            tint = if (uiState.soundEnabled) Color(0xFF0284C7) else Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0284C7),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = "Arrow Escape",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A),
                        letterSpacing = (-0.5).sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    shadowElevation = 1.dp,
                    modifier = Modifier.size(42.dp)
                ) {
                    IconButton(onClick = {
                        SoundManager.playTap()
                        onOpenSettings()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // TOP EARNINGS SUMMARY CARDS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Total Earnings Card
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            SoundManager.playTap()
                            onOpenWallet()
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(14.dp))
                            Text(text = "TOTAL EARNINGS", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFF94A3B8), letterSpacing = 0.5.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "₹${"%.2f".format(uiState.totalEarnings)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0F172A)
                        )
                    }
                }

                // Wallet Balance Card
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            SoundManager.playTap()
                            onOpenWallet()
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(14.dp))
                            Text(text = "WALLET BALANCE", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFF94A3B8), letterSpacing = 0.5.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "₹${"%.2f".format(uiState.walletBalance)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF059669)
                        )
                    }
                }
            }

            // PRIMARY HERO PLAY CARD
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = when (difficulty) {
                                Difficulty.EASY -> Color(0xFFECFDF5)
                                Difficulty.NORMAL -> Color(0xFFEFF6FF)
                                Difficulty.MEDIUM -> Color(0xFFF0FDFA)
                                Difficulty.HARD -> Color(0xFFFEF3C7)
                                Difficulty.VERY_HARD -> Color(0xFFFFEDD5)
                                Difficulty.MASTER -> Color(0xFFF3E8FF)
                                Difficulty.GRANDMASTER -> Color(0xFFFFE4E6)
                                Difficulty.LEGENDARY -> Color(0xFFFEF2F2)
                            }
                        ) {
                            Text(
                                text = "${difficulty.displayName} Tier",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (difficulty) {
                                    Difficulty.EASY -> Color(0xFF059669)
                                    Difficulty.NORMAL -> Color(0xFF2563EB)
                                    Difficulty.MEDIUM -> Color(0xFF0D9488)
                                    Difficulty.HARD -> Color(0xFFD97706)
                                    Difficulty.VERY_HARD -> Color(0xFFEA580C)
                                    Difficulty.MASTER -> Color(0xFF9333EA)
                                    Difficulty.GRANDMASTER -> Color(0xFFE11D48)
                                    Difficulty.LEGENDARY -> Color(0xFFDC2626)
                                },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = "Reward: ₹${"%.2f".format(difficulty.rewardRupees)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF16A34A)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Level $currentLevel",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A)
                    )

                    Text(
                        text = "Clear obstacles & guide all arrows to safety",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            SoundManager.playTap()
                            onPlayContinue()
                        },
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
                            Text(text = "Play Level $currentLevel", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // NAVIGATION MENU TILES (2x2 Grid)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Level Select Tile
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            SoundManager.playTap()
                            onOpenLevels()
                        }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFEFF6FF),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.GridOn, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "Levels (5 Tiers)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        Text(text = "Easy to Extreme", fontSize = 10.sp, color = Color(0xFF64748B))
                    }
                }

                // 2. Daily Check-In Tile
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            SoundManager.playTap()
                            onOpenDailyReward()
                        }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFEF3C7),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "Daily Check-In", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        Text(text = "Streak: Day ${((uiState.dailyStreak) % 7) + 1}", fontSize = 10.sp, color = Color(0xFFD97706), fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 3. Weekly Dashboard Tile
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            SoundManager.playTap()
                            onOpenWeeklyDashboard()
                        }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF3E8FF),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFF9333EA), modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "Weekly Winners", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        Text(text = "Top Tournaments", fontSize = 10.sp, color = Color(0xFF64748B))
                    }
                }

                // 4. Rewards Hub Tile
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            SoundManager.playTap()
                            onOpenRewards()
                        }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFECFDF5),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "Rewards & Rates", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        Text(text = "₹2 – ₹15 / level", fontSize = 10.sp, color = Color(0xFF059669), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // FOOTER STATUS
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                Text(text = "${uiState.hintsRemaining} Free Hints Available", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                modifier = Modifier.clickable {
                    SoundManager.playTap()
                    onOpenSupport()
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Headphones, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(12.dp))
                    Text(text = "Support", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                }
            }
        }
    }
}
