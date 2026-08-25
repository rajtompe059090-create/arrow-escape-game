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
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arrowescape.game.viewmodel.GameUiState

@Composable
fun HomeScreen(
    uiState: GameUiState,
    onPlayContinue: () -> Unit,
    onOpenLevels: () -> Unit,
    onOpenWallet: () -> Unit,
    onOpenRewards: () -> Unit,
    onOpenDailyReward: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleSound: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // TOP APP BAR: Sound Toggle & Quick Wallet Pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.size(44.dp)
            ) {
                IconButton(onClick = onToggleSound) {
                    Icon(
                        imageVector = if (uiState.soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                        contentDescription = "Sound Toggle",
                        tint = Color(0xFF64748B)
                    )
                }
            }

            // Wallet Pill
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFFFFBEB),
                shadowElevation = 2.dp,
                modifier = Modifier.clickable { onOpenWallet() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Wallet",
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Wallet: ₹${uiState.earnedRupees}",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E),
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // BRANDING & TITLE
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Arrow Escape",
                style = MaterialTheme.typography.headlineLarge,
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.Black,
                fontSize = 32.sp
            )
            Text(
                text = "Tap • Solve • Escape",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF64748B),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // CURRENT LEVEL & EARNINGS HERO CARD
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF0284C7), Color(0xFF0369A1))
                            )
                        )
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CURRENT STAGE",
                        color = Color(0xFFBAE6FD),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Level ${uiState.unlockedLevel}",
                        color = Color.White,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // STATS ROW: Total Earnings & Wallet
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0x33FFFFFF),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(vertical = 10.dp, horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "TOTAL EARNINGS",
                                color = Color(0xFFBAE6FD),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "₹${uiState.earnedRupees}",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Box(
                            modifier = Modifier
                                .height(28.dp)
                                .width(1.dp)
                                .background(Color(0x44FFFFFF))
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "WALLET BALANCE",
                                color = Color(0xFFBAE6FD),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "₹${uiState.earnedRupees}",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // PRIMARY ACTION BUTTONS
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // PLAY / CONTINUE BUTTON
            Button(
                onClick = onPlayContinue,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(8.dp, RoundedCornerShape(20.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PLAY LEVEL ${uiState.unlockedLevel}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            // LEVELS BUTTON
            OutlinedButton(
                onClick = onOpenLevels,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF1E293B)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .shadow(2.dp, RoundedCornerShape(20.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.GridOn,
                    contentDescription = null,
                    tint = Color(0xFF0284C7)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SELECT LEVEL",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // NAVIGATION FEATURE CARDS (Wallet, Rewards, Daily Reward, Settings)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HomeFeatureCard(
                title = "Wallet",
                subtitle = "₹${uiState.earnedRupees}",
                icon = Icons.Default.AccountBalanceWallet,
                iconColor = Color(0xFFD97706),
                bgColor = Color(0xFFFFFBEB),
                onClick = onOpenWallet,
                modifier = Modifier.weight(1f)
            )

            HomeFeatureCard(
                title = "Daily Reward",
                subtitle = "Claim ₹ Cash",
                icon = Icons.Default.CardGiftcard,
                iconColor = Color(0xFF16A34A),
                bgColor = Color(0xFFF0FDF4),
                onClick = onOpenDailyReward,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HomeFeatureCard(
                title = "Rewards",
                subtitle = "Tiers & Rates",
                icon = Icons.Default.EmojiEvents,
                iconColor = Color(0xFF9333EA),
                bgColor = Color(0xFFFAF5FF),
                onClick = onOpenRewards,
                modifier = Modifier.weight(1f)
            )

            HomeFeatureCard(
                title = "Settings",
                subtitle = "Audio & More",
                icon = Icons.Default.Settings,
                iconColor = Color(0xFF475569),
                bgColor = Color(0xFFF1F5F9),
                onClick = onOpenSettings,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun HomeFeatureCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    bgColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = modifier
            .height(84.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = bgColor,
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
