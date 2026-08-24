package com.arrowescape.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import androidx.compose.ui.viewinterop.AndroidView
import com.arrowescape.game.data.UserPreferencesRepository
import com.arrowescape.game.ui.screens.GameScreen
import com.arrowescape.game.ui.screens.HomeScreen
import com.arrowescape.game.ui.screens.LevelSelectScreen
import com.arrowescape.game.ui.screens.SplashScreen
import com.arrowescape.game.ui.theme.ArrowEscapeTheme
import com.arrowescape.game.viewmodel.GameViewModel

enum class AppScreen {
    SPLASH,
    HOME,
    LEVEL_SELECT,
    GAME
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        MobileAds.initialize(this) {}

        setContent {
            ArrowEscapeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val prefsRepo =
                        remember { UserPreferencesRepository(applicationContext) }

                    val viewModel: GameViewModel =
                        viewModel { GameViewModel(prefsRepo) }

                    ArrowEscapeApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun ArrowEscapeApp(
    viewModel: GameViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    var currentScreen by remember {
        mutableStateOf(AppScreen.SPLASH)
    }

    when (currentScreen) {

        AppScreen.SPLASH -> {
            SplashScreen(
                onStart = {
                    currentScreen = AppScreen.HOME
                }
            )
        }

        AppScreen.HOME -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                HomeScreen(
                    uiState = uiState,
                    onContinueGame = {
                        viewModel.loadLevel(uiState.unlockedLevel)
                        currentScreen = AppScreen.GAME
                    },
                    onSelectLevels = {
                        currentScreen = AppScreen.LEVEL_SELECT
                    },
                    onOpenWallet = {
                    },
                    onToggleSound = {
                        viewModel.toggleSound()
                    },
                    modifier = Modifier.weight(1f)
                )

                TestBannerAd()
            }
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
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                GameScreen(
                    modifier = Modifier.weight(1f),
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
                    }
                )

                TestBannerAd()
            }
        }
    }
}

@Composable
fun TestBannerAd() {

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),

        factory = { context ->

            AdView(context).apply {

                setAdSize(AdSize.BANNER)

                adUnitId =
                    "ca-app-pub-3940256099942544/9214589741"

                loadAd(
                    AdRequest.Builder().build()
                )
            }
        }
    )
}
