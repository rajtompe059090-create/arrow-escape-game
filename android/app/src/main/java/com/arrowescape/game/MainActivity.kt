package com.arrowescape.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.arrowescape.game.data.UserPreferencesRepository
import com.arrowescape.game.ui.screens.GameScreen
import com.arrowescape.game.ui.screens.LevelSelectScreen
import com.arrowescape.game.viewmodel.GameViewModel

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

        // Start with Level 1
        viewModel.loadLevel(1)

        setContent {

            val uiState by viewModel.uiState.collectAsState()

            var showingGame by remember {
                mutableStateOf(false)
            }

            if (showingGame) {

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
                        showingGame = false
                    },

                    onBack = {
                        showingGame = false
                    },

                    onNextLevel = {
                        val currentLevel =
                            uiState.currentLevel?.id ?: 1

                        viewModel.loadLevel(
                            currentLevel + 1
                        )
                    }
                )

            } else {

                LevelSelectScreen(
                    uiState = uiState,

                    onSelectLevel = { levelId ->
                        viewModel.loadLevel(levelId)
                        showingGame = true
                    },

                    onBack = {
                        finish()
                    }
                )
            }
        }
    }
}
