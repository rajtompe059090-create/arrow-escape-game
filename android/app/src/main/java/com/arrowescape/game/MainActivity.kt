package com.arrowescape.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.arrowescape.game.data.UserPreferencesRepository
import com.arrowescape.game.ui.components.DailyRewardDialog
import com.arrowescape.game.ui.components.RewardsDialog
import com.arrowescape.game.ui.components.SettingsDialog
import com.arrowescape.game.ui.components.WalletDialog
import com.arrowescape.game.ui.screens.GameScreen
import com.arrowescape.game.ui.screens.HomeScreen
import com.arrowescape.game.ui.screens.LevelSelectScreen
import com.arrowescape.game.ui.theme.ArrowEscapeTheme
import com.arrowescape.game.viewmodel.GameViewModel

enum class AppScreen {
    HOME,
    LEVEL_SELECT,
    GAME
}

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T {
                if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return GameViewModel(
                        UserPreferencesRepository(applicationContext)
                    ) as T
                }

                throw IllegalArgumentException(
                    "Unknown ViewModel class: ${modelClass.name}"
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ArrowEscapeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ArrowEscapeMainApp(
                        viewModel = viewModel,
                        onExitApp = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
fun ArrowEscapeMainApp(
    viewModel: GameViewModel,
    onExitApp: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // ALWAYS START ON HOME SCREEN
    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }

    // Dialog States
    var showWalletDialog by remember { mutableStateOf(false) }
    var showRewardsDialog by remember { mutableStateOf(false) }
    var showDailyRewardDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Handle system back navigation
    BackHandler {
        when {
            showWalletDialog -> showWalletDialog = false
            showRewardsDialog -> showRewardsDialog = false
            showDailyRewardDialog -> showDailyRewardDialog = false
            showSettingsDialog -> showSettingsDialog = false
            currentScreen == AppScreen.GAME -> currentScreen = AppScreen.HOME
            currentScreen == AppScreen.LEVEL_SELECT -> currentScreen = AppScreen.HOME
            currentScreen == AppScreen.HOME -> onExitApp()
        }
    }

    when (currentScreen) {
        AppScreen.HOME -> {
            HomeScreen(
                uiState = uiState,
                onPlayContinue = {
                    val targetLevel = if (uiState.unlockedLevel > 0) uiState.unlockedLevel else 1
                    viewModel.loadLevel(targetLevel)
                    currentScreen = AppScreen.GAME
                },
                onOpenLevels = {
                    currentScreen = AppScreen.LEVEL_SELECT
                },
                onOpenWallet = {
                    showWalletDialog = true
                },
                onOpenRewards = {
                    showRewardsDialog = true
                },
                onOpenDailyReward = {
                    showDailyRewardDialog = true
                },
                onOpenSettings = {
                    showSettingsDialog = true
                },
                onToggleSound = {
                    viewModel.toggleSound()
                }
            )
        }

        AppScreen.LEVEL_SELECT -> {
            LevelSelectScreen(
                uiState = uiState,
                onSelectLevel = { levelId ->
                    viewModel.loadLevel(levelId)
                    currentScreen = AppScreen.GAME
                },
                onBack = {
                    currentScreen = AppScreen.HOME
                }
            )
        }

        AppScreen.GAME -> {
            GameScreen(
                uiState = uiState,
                onArrowTapped = { arrow ->
                    viewModel.onArrowTapped(arrow)
                },
                onUseHint = {
                    viewModel.useHint()
                },
                onRestartLevel = {
                    viewModel.restartCurrentLevel()
                },
                onOpenLevels = {
                    currentScreen = AppScreen.LEVEL_SELECT
                },
                onBack = {
                    currentScreen = AppScreen.HOME
                },
                onNextLevel = {
                    val currentLevel = uiState.currentLevel?.id ?: 1
                    viewModel.loadLevel(currentLevel + 1)
                }
            )
        }
    }

    // Modal Overlays
    if (showWalletDialog) {
        WalletDialog(
            uiState = uiState,
            onDismiss = { showWalletDialog = false }
        )
    }

    if (showRewardsDialog) {
        RewardsDialog(
            uiState = uiState,
            onDismiss = { showRewardsDialog = false }
        )
    }

    if (showDailyRewardDialog) {
        DailyRewardDialog(
            uiState = uiState,
            onDismiss = { showDailyRewardDialog = false }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            uiState = uiState,
            onToggleSound = { viewModel.toggleSound() },
            onDismiss = { showSettingsDialog = false }
        )
    }
}
