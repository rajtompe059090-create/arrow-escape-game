package com.arrowescape.game.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.arrowescape.game.model.Difficulty
import com.arrowescape.game.model.EarningTransaction
import com.arrowescape.game.model.TransactionType
import com.arrowescape.game.sound.SoundManager
import com.arrowescape.game.viewmodel.GameUiState
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// CONFIGURATION CONSTANTS
const val TELEGRAM_SUPPORT_USERNAME = "Earning_adda0590"
const val SUPPORT_EMAIL = "rajtompe0590@gmail.com"

// ==========================================
// 1. WALLET DIALOG (with Withdraw & Support triggers)
// ==========================================
@Composable
fun WalletDialog(
    uiState: GameUiState,
    onOpenWithdraw: () -> Unit,
    onOpenSupport: () -> Unit,
    onDismiss: () -> Unit
) {
    var activeTab by remember { mutableStateOf(0) } // 0: Tiers, 1: History

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFECFDF5),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = Color(0xFF059669),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Earnings Wallet",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "User: ${uiState.username}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                    IconButton(onClick = {
                        SoundManager.playTap()
                        onDismiss()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Dual Balance Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "WALLET BALANCE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF94A3B8),
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "₹${"%.2f".format(uiState.walletBalance)}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF34D399)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "TOTAL EARNINGS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF94A3B8),
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "₹${"%.2f".format(uiState.totalEarnings)}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Solved Levels: ${uiState.completedLevels.size}",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF064E3B)
                            ) {
                                Text(
                                    text = "Active Ledger",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF34D399),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons: Withdraw & Support
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            SoundManager.playTap()
                            onOpenWithdraw()
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text(text = "Withdraw", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            SoundManager.playTap()
                            onOpenSupport()
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Headphones, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text(text = "Support", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tabs: Reward Tiers vs History
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(4.dp)) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (activeTab == 0) Color.White else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    SoundManager.playTap()
                                    activeTab = 0
                                }
                        ) {
                            Text(
                                text = "Reward Tiers",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (activeTab == 0) Color(0xFF0F172A) else Color(0xFF64748B),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (activeTab == 1) Color.White else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    SoundManager.playTap()
                                    activeTab = 1
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = if (activeTab == 1) Color(0xFF0F172A) else Color(0xFF64748B),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "History (${uiState.earningHistory.size})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (activeTab == 1) Color(0xFF0F172A) else Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (activeTab == 0) {
                    // Reward Tiers List
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Difficulty.values().forEach { diff ->
                            val isCurrentTier = Difficulty.fromLevel(uiState.unlockedLevel) == diff
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isCurrentTier) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                                border = if (isCurrentTier) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)) else null,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (isCurrentTier) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFCBD5E1))
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = "${diff.displayName} (${diff.levelRange})",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1E293B)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "₹${diff.rewardRupees}.00 / lvl",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF16A34A)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // History List
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (uiState.earningHistory.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No transactions recorded yet.\nSolve puzzles to earn rewards!",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            val sdf = remember { SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()) }
                            uiState.earningHistory.forEach { tx ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFF8FAFC),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(text = tx.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                            Text(text = sdf.format(Date(tx.timestamp)), fontSize = 10.sp, color = Color(0xFF94A3B8))
                                        }
                                        val isWithdrawal = tx.type == TransactionType.WITHDRAWAL
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = if (isWithdrawal) "-₹${"%.2f".format(tx.amount)}" else "+₹${"%.2f".format(tx.amount)}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Black,
                                                color = if (isWithdrawal) Color(0xFFDC2626) else Color(0xFF16A34A)
                                            )
                                            if (tx.status != "SUCCESS") {
                                                Text(text = tx.status, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Footer Notice
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                        Text(
                            text = "Earnings are saved to your secure offline ledger. UPI payouts available on manual review.",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B),
                            lineHeight = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        SoundManager.playTap()
                        onDismiss()
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
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
// 2. WITHDRAW DIALOG (Manual/Demo validation)
// ==========================================
@Composable
fun WithdrawDialog(
    uiState: GameUiState,
    onRequestWithdrawal: (amount: Double, upiId: String, onResult: (Boolean, String) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var amountText by remember { mutableStateOf("50") }
    var upiIdText by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFE0F2FE),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Send, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(20.dp))
                            }
                        }
                        Text(
                            text = "Withdraw Earnings",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0F172A)
                        )
                    }
                    IconButton(onClick = {
                        SoundManager.playTap()
                        onDismiss()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Available Balance Strip
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Available Balance", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        Text(text = "₹${"%.2f".format(uiState.walletBalance)}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF16A34A))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Amount Input
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Withdraw Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("50", "100", "200").forEach { chipAmount ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    SoundManager.playTap()
                                    amountText = chipAmount
                                }
                        ) {
                            Text(
                                text = "₹$chipAmount",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF334155),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                SoundManager.playTap()
                                amountText = uiState.walletBalance.toInt().coerceAtLeast(50).toString()
                            }
                    ) {
                        Text(
                            text = "MAX",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0284C7),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // UPI ID Input
                OutlinedTextField(
                    value = upiIdText,
                    onValueChange = { upiIdText = it },
                    label = { Text("UPI ID (e.g. yourname@upi)") },
                    placeholder = { Text("username@okhdfcbank") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (statusMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = statusMessage!!,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSuccess) Color(0xFF16A34A) else Color(0xFFDC2626),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        onRequestWithdrawal(amount, upiIdText) { success, msg ->
                            isSuccess = success
                            statusMessage = msg
                            if (success) {
                                upiIdText = ""
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(text = "Submit Withdrawal", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Support Shortcut Button inside Withdrawal Screen
                OutlinedButton(
                    onClick = {
                        SoundManager.playTap()
                        try {
                            val defaultMsg = "Check my withdrawal"
                            val encodedMsg = Uri.encode(defaultMsg)
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://t.me/$TELEGRAM_SUPPORT_USERNAME?text=$encodedMsg")
                            )
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Telegram: @$TELEGRAM_SUPPORT_USERNAME",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Headphones,
                            contentDescription = null,
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Withdrawal Support (Telegram)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0284C7)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. DAILY CHECK-IN DIALOG (7-Day Cycle & Live Countdown)
// ==========================================
@Composable
fun DailyRewardDialog(
    uiState: GameUiState,
    onClaim: (multiplier: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val dailyRewards = listOf(1 to 1, 2 to 2, 3 to 3, 4 to 4, 5 to 5, 6 to 7, 7 to 10)
    val now = System.currentTimeMillis()
    val twentyFourHoursMs = 24 * 60 * 60 * 1000L
    val lastTs = uiState.lastDailyRewardTimestamp
    val isWithin24h = (lastTs > 0 && now - lastTs < twentyFourHoursMs)

    var countdownStr by remember { mutableStateOf("") }

    LaunchedEffect(lastTs) {
        while (true) {
            val current = System.currentTimeMillis()
            val diff = twentyFourHoursMs - (current - lastTs)
            if (diff > 0 && lastTs > 0) {
                val hours = diff / (1000 * 60 * 60)
                val minutes = (diff % (1000 * 60 * 60)) / (1000 * 60)
                val seconds = (diff % (1000 * 60)) / 1000
                countdownStr = "%02d:%02d:%02d".format(hours, minutes, seconds)
            } else {
                countdownStr = ""
            }
            delay(1000)
        }
    }

    val currentStreakDay = if (isWithin24h) {
        ((uiState.dailyStreak - 1) % 7) + 1
    } else {
        (uiState.dailyStreak % 7) + 1
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFFEF3C7),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(20.dp))
                            }
                        }
                        Column {
                            Text(text = "Daily Check-In", fontSize = 17.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                            Text(text = "7-Day Streak Rewards", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF64748B))
                        }
                    }
                    IconButton(onClick = {
                        SoundManager.playTap()
                        onDismiss()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 7-Day Grid
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
                                val isClaimed = if (isWithin24h) day <= currentStreakDay else day < currentStreakDay
                                val isToday = day == currentStreakDay && !isWithin24h

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = when {
                                        isToday -> Color(0xFFFEF3C7)
                                        isClaimed -> Color(0xFFECFDF5)
                                        else -> Color(0xFFF8FAFC)
                                    },
                                    border = if (isToday) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFF59E0B)) else null,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(72.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "Day $day",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isToday) Color(0xFFB45309) else Color(0xFF64748B)
                                        )
                                        Text(
                                            text = "₹$amount",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Black,
                                            color = when {
                                                isToday -> Color(0xFFB45309)
                                                isClaimed -> Color(0xFF059669)
                                                else -> Color(0xFF0F172A)
                                            }
                                        )
                                        if (isClaimed) {
                                            Text(text = "Claimed ✓", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                                        }
                                    }
                                }
                            }
                            if (rowItems.size < 4) {
                                Spacer(modifier = Modifier.weight((4 - rowItems.size).toFloat()))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Streak & Status
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🔥 Current Streak: ${uiState.dailyStreak} Days", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                        if (isWithin24h && countdownStr.isNotEmpty()) {
                            Text(text = "Next in $countdownStr", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!isWithin24h) {
                    val todayAmount = dailyRewards.find { it.first == currentStreakDay }?.second ?: 1
                    Button(
                        onClick = { onClaim(1) },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(text = "Claim ₹$todayAmount.00", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            SoundManager.playTap()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(text = "Come Back Tomorrow", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. WEEKLY DASHBOARD DIALOG (Demo Leaderboard & Winners)
// ==========================================
@Composable
fun WeeklyDashboardDialog(
    uiState: GameUiState,
    onDismiss: () -> Unit
) {
    val demoWinners = listOf(
        Triple("PlayerOne", 251, "🥇"),
        Triple("Rahul", 151, "🥈"),
        Triple("Aman", 51, "🥉"),
        Triple("Priya", 41, "4"),
        Triple("Neha", 31, "5")
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFEFF6FF),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(22.dp))
                            }
                        }
                        Column {
                            Text(text = "Weekly Dashboard", fontSize = 17.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                            Text(text = "Top Escapers & Earnings", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF64748B))
                        }
                    }
                    IconButton(onClick = {
                        SoundManager.playTap()
                        onDismiss()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Player Weekly Stats Strip
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Weekly Earnings", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                            Text(text = "₹${"%.2f".format(uiState.totalEarnings)}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF16A34A))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Levels Cleared", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                            Text(text = "${uiState.completedLevels.size}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF0284C7))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Your Rank", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                            Text(text = "#6", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFFD97706))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "🏆 WEEKLY WINNERS (DEMO)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0F172A),
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Winners List
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    demoWinners.forEachIndexed { idx, (name, amount, badge) ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (idx == 0) Color(0xFFFEF3C7) else Color(0xFFF8FAFC),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(text = badge, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                    Column {
                                        Text(text = name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                        Text(text = "Verified Escaper", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                    }
                                }
                                Text(
                                    text = "₹$amount",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (idx == 0) Color(0xFFB45309) else Color(0xFF16A34A)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Weekly leaderboard resets every Sunday midnight. Keep escaping arrows to climb up!",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        SoundManager.playTap()
                        onDismiss()
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
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
// 5. REWARDS HUB DIALOG
// ==========================================
@Composable
fun RewardsDialog(
    uiState: GameUiState,
    onOpenDaily: () -> Unit,
    onOpenWallet: () -> Unit,
    onOpenWeekly: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFFEF3C7),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(22.dp))
                            }
                        }
                        Column {
                            Text(text = "🏆 Rewards Hub", fontSize = 17.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                            Text(text = "Real ₹ Rewards & Bonuses", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF64748B))
                        }
                    }
                    IconButton(onClick = {
                        SoundManager.playTap()
                        onDismiss()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Balance Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFFECFDF5),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                SoundManager.playTap()
                                onOpenWallet()
                            }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(text = "Wallet Balance", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                            Text(text = "₹${"%.2f".format(uiState.walletBalance)}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF059669))
                            Text(text = "Tap to View →", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFFEFF6FF),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(text = "Total Earned", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E40AF))
                            Text(text = "₹${"%.2f".format(uiState.totalEarnings)}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF2563EB))
                            Text(text = "Lifetime Earned", fontSize = 9.sp, color = Color(0xFF3B82F6))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Option 1: Level Rewards
                val currentDiff = Difficulty.fromLevel(uiState.unlockedLevel)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "🎮 Level Rewards", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            Text(text = "Current Rate: ₹${currentDiff.rewardRupees}.00 / level (${currentDiff.displayName})", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                        Text(text = "Active", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Option 2: Daily Check-In
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            SoundManager.playTap()
                            onOpenDaily()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "🎁 Daily Check-In", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            Text(text = "7-Day progressive ₹ cash rewards", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Option 3: Weekly Tournament Dashboard
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            SoundManager.playTap()
                            onOpenWeekly()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "🏆 Weekly Tournament", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            Text(text = "Compete on leaderboard for prizes", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        SoundManager.playTap()
                        onDismiss()
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
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
// 6. SUPPORT DIALOG
// ==========================================
@Composable
fun SupportDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFEFF6FF),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Headphones, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(20.dp))
                            }
                        }
                        Text(text = "Customer Support", fontSize = 17.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                    }
                    IconButton(onClick = {
                        SoundManager.playTap()
                        onDismiss()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Need help with payouts, game levels, or bug reports? Reach out to our dedicated support channels below:",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Telegram Support Button
                Button(
                    onClick = {
                        SoundManager.playTap()
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/$TELEGRAM_SUPPORT_USERNAME"))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Telegram link: @$TELEGRAM_SUPPORT_USERNAME", Toast.LENGTH_LONG).show()
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(text = "Telegram Support", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Email Support Button
                OutlinedButton(
                    onClick = {
                        SoundManager.playTap()
                        try {
                            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:$SUPPORT_EMAIL")
                                putExtra(Intent.EXTRA_SUBJECT, "Arrow Escape Support Request")
                            }
                            context.startActivity(emailIntent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Email: $SUPPORT_EMAIL", Toast.LENGTH_LONG).show()
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(text = "Email: $SUPPORT_EMAIL", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        SoundManager.playTap()
                        onDismiss()
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
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
// 7. SETTINGS DIALOG (Audio, Haptics, Music, Reset)
// ==========================================
@Composable
fun SettingsDialog(
    uiState: GameUiState,
    onToggleSound: () -> Unit,
    onToggleMusic: () -> Unit,
    onToggleHaptics: () -> Unit,
    onOpenSupport: () -> Unit,
    onResetProgress: () -> Unit,
    onDismiss: () -> Unit
) {
    var showResetConfirm by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFF334155), modifier = Modifier.size(20.dp))
                            }
                        }
                        Column {
                            Text(text = "Settings", fontSize = 17.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                            Text(text = "Audio & Preferences", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF64748B))
                        }
                    }
                    IconButton(onClick = {
                        SoundManager.playTap()
                        onDismiss()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Toggle 1: Sound Effects
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(if (uiState.soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute, contentDescription = null, tint = Color(0xFF0284C7))
                            Column {
                                Text(text = "Sound Effects", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                Text(text = "Taps, escapes & level wins", fontSize = 10.sp, color = Color(0xFF64748B))
                            }
                        }
                        Switch(
                            checked = uiState.soundEnabled,
                            onCheckedChange = { onToggleSound() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Toggle 2: Background Music
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(if (uiState.musicEnabled) Icons.Default.MusicNote else Icons.Default.MusicOff, contentDescription = null, tint = Color(0xFF7C3AED))
                            Column {
                                Text(text = "Background Music", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                Text(text = "Ambient calm synth loop", fontSize = 10.sp, color = Color(0xFF64748B))
                            }
                        }
                        Switch(
                            checked = uiState.musicEnabled,
                            onCheckedChange = { onToggleMusic() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Toggle 3: Vibration
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Vibration, contentDescription = null, tint = Color(0xFF059669))
                            Column {
                                Text(text = "Vibration", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                Text(text = "Haptics on arrow escape & collision", fontSize = 10.sp, color = Color(0xFF64748B))
                            }
                        }
                        Switch(
                            checked = uiState.hapticsEnabled,
                            onCheckedChange = { onToggleHaptics() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Support Shortcut
                OutlinedButton(
                    onClick = {
                        SoundManager.playTap()
                        onOpenSupport()
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Headphones, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(text = "Contact Support", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Reset Progress
                if (!showResetConfirm) {
                    TextButton(onClick = { showResetConfirm = true }) {
                        Text(text = "Reset All Game Progress", color = Color(0xFFDC2626), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFEF2F2),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "Are you sure you want to reset all progress?", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        showResetConfirm = false
                                        onResetProgress()
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(text = "Yes, Reset", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { showResetConfirm = false },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(text = "Cancel", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        SoundManager.playTap()
                        onDismiss()
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
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
// 8. LEVEL COMPLETE DIALOG
// ==========================================
@Composable
fun LevelCompleteDialog(
    uiState: GameUiState,
    onNextLevel: () -> Unit,
    onDismiss: () -> Unit
) {
    val level = uiState.currentLevel ?: return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFECFDF5),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "🎉", fontSize = 32.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Level ${level.id} Solved!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (uiState.isLastLevelAlreadyClaimed) "Already completed previously." else "Reward credited to your wallet balance!",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Reward Banner
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF0F172A),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "REWARD", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF94A3B8), letterSpacing = 1.sp)
                        Text(
                            text = if (uiState.isLastLevelAlreadyClaimed) "₹0.00" else "+₹${"%.2f".format(uiState.lastCompletedReward)}",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF34D399)
                        )
                        Text(text = "Wallet: ₹${"%.2f".format(uiState.walletBalance)}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onNextLevel,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "Next Level", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    }
}

// ==========================================
// 9. GAME OVER DIALOG
// ==========================================
@Composable
fun GameOverDialog(
    onRetry: () -> Unit,
    onBackToHome: () -> Unit
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFEF2F2),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "💥", fontSize = 32.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Out of Lives!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "You collided with blocked arrows. Try again with careful planning!",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onRetry,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Text(text = "Try Again", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onBackToHome,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(text = "Back to Home", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
