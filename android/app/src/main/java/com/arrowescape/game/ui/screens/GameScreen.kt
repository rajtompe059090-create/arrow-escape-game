package com.arrowescape.game.ui.screens

import android.app.Activity
import android.widget.Toast
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
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arrowescape.game.ads.AdManager
import com.arrowescape.game.model.Arrow
import com.arrowescape.game.sound.SoundManager
import com.arrowescape.game.ui.components.GameOverDialog
import com.arrowescape.game.ui.components.LevelCompleteDialog
import com.arrowescape.game.ui.components.PuzzleBoard
import com.arrowescape.game.viewmodel.GameUiState

@Composable
fun GameScreen(
    uiState: GameUiState,
    onArrowTapped: (Arrow) -> Unit,
    onUseHint: () -> Unit,
    onGrantRewardedHint: () -> Unit,
    onRestartLevel: () -> Unit,
    onOpenLevels: () -> Unit,
    onBack: () -> Unit,
    onNextLevel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val level = uiState.currentLevel ?: return
    val context = LocalContext.current
    val activity = context as? Activity

    var isShowingCompleteDialog by remember(level.id) {
        mutableStateOf(false)
    }

    // =========================================================
    // LEVEL COMPLETE -> INTERSTITIAL -> COMPLETE DIALOG
    // =========================================================

    LaunchedEffect(
        uiState.isLevelCompleted,
        level.id
    ) {
        if (uiState.isLevelCompleted) {

            isShowingCompleteDialog = false

            if (activity != null) {

                AdManager.showInterstitial(
                    activity = activity
                ) {
                    isShowingCompleteDialog = true
                }

            } else {

                isShowingCompleteDialog = true
            }
        } else {

            isShowingCompleteDialog = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // =================================================
            // TOP SECTION
            // =================================================

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {

                // TOP BAR
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
                        horizontalArrangement =
                            Arrangement.SpaceBetween,
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        IconButton(
                            onClick = {
                                SoundManager.playTap()
                                onBack()
                            }
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFF334155)
                            )
                        }

                        Column(
                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ) {

                            Text(
                                text =
                                    "${level.difficulty.displayName} • Level ${level.id}",
                                style =
                                    MaterialTheme.typography.titleMedium,
                                fontWeight =
                                    FontWeight.Black,
                                color =
                                    Color(0xFF0F172A)
                            )

                            Text(
                                text =
                                    "Reward: ₹${level.rewardRupees}.00",
                                fontSize = 11.sp,
                                fontWeight =
                                    FontWeight.Bold,
                                color =
                                    Color(0xFF16A34A)
                            )
                        }

                        Surface(
                            shape =
                                RoundedCornerShape(12.dp),
                            color =
                                Color(0xFFF1F5F9)
                        ) {

                            Text(
                                text =
                                    "${uiState.remainingArrows.size} left",
                                modifier =
                                    Modifier.padding(
                                        horizontal = 10.dp,
                                        vertical = 6.dp
                                    ),
                                fontSize = 12.sp,
                                fontWeight =
                                    FontWeight.Bold,
                                color =
                                    Color(0xFF334155)
                            )
                        }
                    }
                }

                // STATS
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 20.dp,
                            vertical = 8.dp
                        ),
                    horizontalArrangement =
                        Arrangement.SpaceBetween,
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Surface(
                        shape =
                            RoundedCornerShape(12.dp),
                        color =
                            Color(0xFFEFF6FF)
                    ) {

                        Text(
                            text =
                                "Moves: ${uiState.movesCount}",
                            modifier =
                                Modifier.padding(
                                    horizontal = 10.dp,
                                    vertical = 4.dp
                                ),
                            fontSize = 11.sp,
                            fontWeight =
                                FontWeight.Bold,
                            color =
                                Color(0xFF2563EB)
                        )
                    }

                    Row(
                        horizontalArrangement =
                            Arrangement.spacedBy(6.dp)
                    ) {

                        for (i in 1..3) {

                            val isAlive =
                                i <= uiState.lives

                            Icon(
                                imageVector =
                                    Icons.Default.Favorite,
                                contentDescription =
                                    "Heart $i",
                                tint =
                                    if (isAlive)
                                        Color(0xFFEF4444)
                                    else
                                        Color(0xFFCBD5E1),
                                modifier =
                                    Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // =================================================
            // PUZZLE BOARD
            // =================================================

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                contentAlignment =
                    Alignment.Center
            ) {

                PuzzleBoard(
                    gridWidth =
                        level.gridWidth,
                    gridHeight =
                        level.gridHeight,
                    arrows =
                        uiState.remainingArrows,
                    escapingArrowIds =
                        uiState.escapingArrowIds,
                    blockedArrowId =
                        uiState.blockedArrowId,
                    hintedArrowId =
                        uiState.hintedArrowId,
                    onArrowTapped =
                        onArrowTapped
                )
            }

            // =================================================
            // BANNER AD (Between puzzle board and bottom controls)
            // =================================================

            AdManager.BannerAdView(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )

            // =================================================
            // BOTTOM CONTROLS
            // =================================================

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 24.dp,
                            vertical = 8.dp
                        ),
                    horizontalArrangement =
                        Arrangement.SpaceEvenly,
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    // HINT
                    BadgedBox(
                        badge = {

                            Badge(
                                containerColor =
                                    if (uiState.hintsRemaining > 0)
                                        Color(0xFF0284C7)
                                    else
                                        Color(0xFFF59E0B)
                            ) {

                                Text(
                                    text =
                                        if (uiState.hintsRemaining > 0)
                                            "${uiState.hintsRemaining}"
                                        else
                                            "+1 AD"
                                )
                            }
                        }
                    ) {

                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            shadowElevation = 3.dp,
                            modifier =
                                Modifier.size(52.dp)
                        ) {

                            IconButton(
                                onClick = {

                                    SoundManager.playTap()

                                    if (
                                        uiState.hintsRemaining > 0
                                    ) {

                                        onUseHint()

                                    } else {

                                        if (activity != null) {

                                            AdManager.showRewarded(
                                                activity = activity,
                                                onUserEarnedReward = {
                                                    onGrantRewardedHint()
                                                },
                                                onAdUnavailable = {

                                                    Toast.makeText(
                                                        context,
                                                        "Reward ad is not available. Please try again.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                },
                                                onAdDismissed = {}
                                            )

                                        } else {

                                            Toast.makeText(
                                                context,
                                                "Reward ad is not available. Please try again.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                },
                                enabled =
                                    uiState.lives > 0 &&
                                            !uiState.isLevelCompleted
                            ) {

                                Icon(
                                    imageVector =
                                        Icons.Default.Lightbulb,
                                    contentDescription =
                                        "Hint",
                                    tint =
                                        if (
                                            uiState.hintsRemaining > 0
                                        )
                                            Color(0xFF0284C7)
                                        else
                                            Color(0xFFD97706)
                                )
                            }
                        }
                    }

                    // RESTART
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 3.dp,
                        modifier =
                            Modifier.size(52.dp)
                    ) {

                        IconButton(
                            onClick = {
                                SoundManager.playTap()
                                onRestartLevel()
                            }
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Default.Refresh,
                                contentDescription =
                                    "Restart",
                                tint =
                                    Color(0xFF334155)
                            )
                        }
                    }

                    // LEVEL SELECT
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 3.dp,
                        modifier =
                            Modifier.size(52.dp)
                    ) {

                        IconButton(
                            onClick = {
                                SoundManager.playTap()
                                onOpenLevels()
                            }
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Default.GridOn,
                                contentDescription =
                                    "Levels",
                                tint =
                                    Color(0xFF334155)
                            )
                        }
                    }
                }
            }

            // =================================================
            // BANNER AD
            // =================================================
            AdManager.BannerAdView(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(vertical = 2.dp)
            )
        }

        // =====================================================
        // LEVEL COMPLETE DIALOG
        // =====================================================

        if (
            uiState.isLevelCompleted &&
            isShowingCompleteDialog
        ) {

            LevelCompleteDialog(
                uiState = uiState,
                onNextLevel = onNextLevel,
                onDismiss = {}
            )
        }

        // =====================================================
        // GAME OVER
        // =====================================================

        if (uiState.isGameOver) {

            GameOverDialog(
                onRetry = onRestartLevel,
                onBackToHome = onBack
            )
        }
    }
}
