package com.arrowescape.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState = viewModel.uiState
            var currentScreen by remember {
                mutableStateOf(AppScreen.LEVEL_SELECT)
            }

            when (currentScreen) {

                AppScreen.LEVEL_SELECT -> {
                    LevelSelectScreen(
                        uiState = uiState,

                        onLevelSelected = { levelId ->
                            viewModel.loadLevel(levelId)
                            currentScreen = AppScreen.GAME
                        },

                        onBack = {
                            finish()
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
                            currentScreen = AppScreen.LEVEL_SELECT
                        },

                        onNextLevel = {
                            val currentLevelId =
                                uiState.currentLevel?.id ?: 1

                            val nextLevelId =
                                currentLevelId + 1

                            viewModel.loadLevel(nextLevelId)
                            currentScreen = AppScreen.GAME
                        }
                    )
                }
            }
        }
    }
}
