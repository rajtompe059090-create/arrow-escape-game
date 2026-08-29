package com.arrowescape.game.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arrowescape.game.ads.AdManager
import com.arrowescape.game.data.LevelRepository
import com.arrowescape.game.model.Difficulty
import com.arrowescape.game.sound.SoundManager
import com.arrowescape.game.viewmodel.GameUiState
import kotlinx.coroutines.launch

/**
 * Calculates a smooth alternating horizontal position (0.22f to 0.78f)
 * for a dynamic, clearly visible zigzag roadmap path.
 */
private fun getNodeXFraction(indexInTier: Int): Float {
    return when (indexInTier % 6) {
        0 -> 0.25f
        1 -> 0.72f
        2 -> 0.38f
        3 -> 0.78f
        4 -> 0.22f
        5 -> 0.60f
        else -> 0.50f
    }
}

@Composable
fun LevelSelectScreen(
    uiState: GameUiState,
    onSelectLevel: (Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val initialDifficulty = remember(uiState.unlockedLevel) {
        Difficulty.fromLevel(uiState.unlockedLevel)
    }

    var selectedDifficulty by remember { mutableStateOf(initialDifficulty) }
    var lockedNotice by remember { mutableStateOf<String?>(null) }

    val tabScrollState = rememberScrollState()
    val listState = rememberLazyListState()

    val levelIds = remember(selectedDifficulty) {
        LevelRepository.getLevelsForTier(selectedDifficulty)
    }

    // Scroll to current unlocked level when tier opens or matches
    LaunchedEffect(selectedDifficulty, uiState.unlockedLevel) {
        val targetIndex = levelIds.indexOf(uiState.unlockedLevel)
        if (targetIndex >= 0) {
            val scrollTarget = (targetIndex - 2).coerceAtLeast(0)
            listState.animateScrollToItem(scrollTarget)
        } else {
            listState.scrollToItem(0)
        }
    }

    // Auto-dismiss locked notice after 2.5 seconds
    LaunchedEffect(lockedNotice) {
        if (lockedNotice != null) {
            kotlinx.coroutines.delay(2500)
            lockedNotice = null
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // ==========================================
        // 1. TOP APP BAR
        // ==========================================
        Surface(
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.size(38.dp)
                    ) {
                        IconButton(onClick = {
                            SoundManager.playTap()
                            onBack()
                        }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "Select Level",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "${uiState.completedLevels.size} Solved • Active: #${uiState.unlockedLevel}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // Wallet Badge
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFECFDF5),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "₹${"%.2f".format(uiState.walletBalance)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF059669)
                        )
                    }
                }
            }
        }

        // ==========================================
        // 2. DIFFICULTY CATEGORY TABS
        // ==========================================
        Surface(
            color = Color.White,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(tabScrollState)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Difficulty.values().forEach { diff ->
                    val isSelected = selectedDifficulty == diff
                    val isTierUnlocked = uiState.unlockedLevel >= (LevelRepository.getLevelsForTier(diff).firstOrNull() ?: 1)

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) Color(0xFF0284C7) else if (isTierUnlocked) Color(0xFFF1F5F9) else Color(0xFFF8FAFC),
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.clickable {
                            SoundManager.playTap()
                            selectedDifficulty = diff
                        }
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = diff.displayName.uppercase(),
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                color = if (isSelected) Color.White else if (isTierUnlocked) Color(0xFF334155) else Color(0xFF94A3B8)
                            )
                            Text(
                                text = "${diff.levelRange} (+₹${"%.0f".format(diff.rewardRupees)})",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color(0xFFBAE6FD) else Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 3. TIER PROGRESS BANNER
        // ==========================================
        val tierLevels = levelIds
        val tierCompletedCount = tierLevels.count { uiState.completedLevels.contains(it) }

        Surface(
            color = Color(0xFFF1F5F9),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${selectedDifficulty.displayName.uppercase()} ROADMAP",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0284C7),
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "$tierCompletedCount / ${tierLevels.size} Cleared",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
            }
        }

        // Locked Notice Toast
        if (lockedNotice != null) {
            Surface(
                color = Color(0xFFF59E0B),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = lockedNotice ?: "",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }

        // ==========================================
        // 4. ROADMAP (ZIGZAG + CURVES + STRAIGHT MIX)
        // ==========================================
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(
                    items = levelIds,
                    key = { _, levelId -> levelId }
                ) { index, levelId ->
                    val isUnlocked = levelId <= uiState.unlockedLevel
                    val isCurrent = levelId == uiState.unlockedLevel
                    val isCompleted = uiState.completedLevels.contains(levelId)
                    val earnedStars = uiState.levelStars[levelId] ?: if (isCompleted) 3 else 0

                    val curXFraction = getNodeXFraction(index)
                    val nextXFraction = if (index < levelIds.size - 1) getNodeXFraction(index + 1) else curXFraction
                    val isNextUnlocked = (index < levelIds.size - 1) && (levelIds[index + 1] <= uiState.unlockedLevel)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(92.dp)
                    ) {
                        // Connecting Roadmap Path (Smooth S-curve connecting exact node centers)
                        if (index < levelIds.size - 1) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val startX = size.width * curXFraction
                                val startY = size.height * 0.5f
                                val endX = size.width * nextXFraction
                                val endY = size.height * 1.5f

                                val lineColor = when {
                                    isCompleted && isNextUnlocked -> Color(0xFF10B981)
                                    isCurrent -> Color(0xFF0284C7)
                                    else -> Color(0xFFCBD5E1)
                                }

                                val path = Path().apply {
                                    moveTo(startX, startY)
                                    cubicTo(
                                        startX,
                                        startY + (endY - startY) * 0.5f,
                                        endX,
                                        startY + (endY - startY) * 0.5f,
                                        endX,
                                        endY
                                    )
                                }

                                drawPath(
                                    path = path,
                                    color = lineColor,
                                    style = Stroke(
                                        width = 10f,
                                        cap = StrokeCap.Round,
                                        pathEffect = if (!isUnlocked) PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f) else null
                                    )
                                )
                            }
                        }

                        // Roadmap Node positioned with exact center matching startX/startY
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .layout { measurable, constraints ->
                                    val placeable = measurable.measure(constraints)
                                    val centerX = (constraints.maxWidth * curXFraction).toInt()
                                    val centerY = (constraints.maxHeight * 0.5f).toInt()
                                    val x = centerX - placeable.width / 2
                                    val y = centerY - placeable.height / 2
                                    layout(constraints.maxWidth, constraints.maxHeight) {
                                        placeable.placeRelative(x, y)
                                    }
                                }
                        ) {
                            RoadmapNode(
                                levelId = levelId,
                                isCurrent = isCurrent,
                                isCompleted = isCompleted,
                                isUnlocked = isUnlocked,
                                earnedStars = earnedStars,
                                rewardRupees = selectedDifficulty.rewardRupees,
                                pulseScale = if (isCurrent) pulseScale else 1f,
                                onClick = {
                                    if (isUnlocked) {
                                        SoundManager.playTap()
                                        onSelectLevel(levelId)
                                    } else {
                                        SoundManager.playBlocked()
                                        lockedNotice = "Complete Level ${levelId - 1} to unlock!"
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 5. BANNER AD
        // ==========================================
        AdManager.BannerAdView(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(vertical = 2.dp)
        )

        // ==========================================
        // 6. BOTTOM STICKY ACTION BAR
        // ==========================================
        Surface(
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFE0F2FE),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "#${uiState.unlockedLevel}",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = Color(0xFF0284C7)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "ACTIVE PROGRESS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF94A3B8),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Level ${uiState.unlockedLevel} (${Difficulty.fromLevel(uiState.unlockedLevel).displayName})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0F172A)
                        )
                    }
                }

                Button(
                    onClick = {
                        SoundManager.playTap()
                        onSelectLevel(uiState.unlockedLevel)
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    modifier = Modifier.height(44.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Play #${uiState.unlockedLevel}",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Modern tactile Node for the Arrow Escape Level Roadmap.
 * Shows level number, stars (⭐⭐⭐), checkmark/lock, and cash reward tag.
 */
@Composable
private fun RoadmapNode(
    levelId: Int,
    isCurrent: Boolean,
    isCompleted: Boolean,
    isUnlocked: Boolean,
    earnedStars: Int,
    rewardRupees: Double,
    pulseScale: Float,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = when {
            isCurrent -> Color(0xFF0284C7)
            isCompleted -> Color.White
            isUnlocked -> Color.White
            else -> Color(0xFFF1F5F9)
        },
        shadowElevation = when {
            isCurrent -> 6.dp
            isCompleted -> 3.dp
            isUnlocked -> 2.dp
            else -> 0.dp
        },
        border = when {
            isCurrent -> androidx.compose.foundation.BorderStroke(3.dp, Color(0xFFBAE6FD))
            isCompleted -> androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF10B981))
            isUnlocked -> androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
            else -> androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        },
        modifier = Modifier
            .size(width = 72.dp, height = 72.dp)
            .scale(pulseScale)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Header row: Level number + status icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#$levelId",
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    color = when {
                        isCurrent -> Color.White
                        isCompleted -> Color(0xFF0F172A)
                        isUnlocked -> Color(0xFF0F172A)
                        else -> Color(0xFF94A3B8)
                    }
                )

                if (isCompleted) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF10B981),
                        modifier = Modifier.size(14.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                } else if (!isUnlocked) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            // Center Content: Stars / PLAY / Lock
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isCompleted) {
                    // Star Rating (⭐⭐⭐)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (s in 1..3) {
                            Text(
                                text = if (s <= earnedStars) "⭐" else "☆",
                                fontSize = 10.sp
                            )
                        }
                    }
                } else if (isCurrent) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "PLAY",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0284C7)
                            )
                        }
                    }
                } else if (isUnlocked) {
                    Text(
                        text = "READY",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF64748B)
                    )
                } else {
                    Text(
                        text = "LOCKED",
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            // Bottom Cash Tag
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = when {
                    isCurrent -> Color(0x33FFFFFF)
                    isCompleted -> Color(0xFFECFDF5)
                    isUnlocked -> Color(0xFFF1F5F9)
                    else -> Color(0xFFF1F5F9)
                }
            ) {
                Text(
                    text = "₹${"%.0f".format(rewardRupees)}",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = when {
                        isCurrent -> Color.White
                        isCompleted -> Color(0xFF059669)
                        isUnlocked -> Color(0xFF475569)
                        else -> Color(0xFF94A3B8)
                    },
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 0.5.dp)
                )
            }
        }
    }
}

