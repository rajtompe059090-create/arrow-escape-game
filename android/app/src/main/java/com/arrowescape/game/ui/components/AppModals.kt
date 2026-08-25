package com.arrowescape.game.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.arrowescape.game.viewmodel.GameUiState

// ==========================================
// WALLET DIALOG
// ==========================================
@Composable
fun WalletDialog(
    uiState: GameUiState,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFFBEB),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "My Wallet",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Balance Card
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF0284C7),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "AVAILABLE BALANCE",
                            color = Color(0xFFBAE6FD),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "₹${uiState.earnedRupees}.00",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Total Earned: ₹${uiState.earnedRupees}",
                            color = Color(0xFFE0F2FE),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats breakdown
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Levels Solved", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                        Text(text = "${uiState.completedLevels.size}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Reward Rate", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                        Text(text = "Up to ₹15/lvl", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(text = "Close", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// DAILY REWARD DIALOG
// ==========================================
@Composable
fun DailyRewardDialog(
    uiState: GameUiState,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF0FDF4),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CardGiftcard,
                                    contentDescription = null,
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Daily Rewards",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Login daily to claim free ₹ cash bonuses!",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 7-day schedule grid
                val dailyRewards = listOf(
                    1 to 2, 2 to 3, 3 to 5, 4 to 5, 5 to 10, 6 to 10, 7 to 20
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    dailyRewards.chunked(4).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { (day, amount) ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (day == 1) Color(0xFFE0F2FE) else Color(0xFFF8FAFC),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(68.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(text = "Day $day", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                                        Text(text = "₹$amount", fontSize = 15.sp, fontWeight = FontWeight.Black, color = if (day == 1) Color(0xFF0284C7) else Color(0xFF0F172A))
                                    }
                                }
                            }
                            if (rowItems.size < 4) {
                                Spacer(modifier = Modifier.weight((4 - rowItems.size).toFloat()))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(text = "Got It", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// REWARDS DIALOG (Tier Rates)
// ==========================================
@Composable
fun RewardsDialog(
    uiState: GameUiState,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFAF5FF),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color(0xFF9333EA),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Level Rewards",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tier rate items
                val tiers = listOf(
                    "Levels 1 – 50" to "₹2.00 / level",
                    "Levels 51 – 100" to "₹3.00 / level",
                    "Levels 101 – 150" to "₹5.00 / level",
                    "Levels 151 – 200" to "₹10.00 / level",
                    "Levels 201+" to "₹15.00 / level"
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tiers.forEach { (range, rate) ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = range, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                                Text(text = rate, fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFF0284C7))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(text = "Close", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// SETTINGS DIALOG
// ==========================================
@Composable
fun SettingsDialog(
    uiState: GameUiState,
    onToggleSound: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = Color(0xFF475569),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sound Setting Row
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (uiState.soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = "Game Sound Effects", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                                Text(text = "Audio feedback on arrow tap", fontSize = 12.sp, color = Color(0xFF64748B))
                            }
                        }

                        Switch(
                            checked = uiState.soundEnabled,
                            onCheckedChange = { onToggleSound() },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF0284C7))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // About & App Info
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Text(text = "Arrow Escape", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                        Text(text = "Version 1.0.0 (Production Build)", fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(text = "Done", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
