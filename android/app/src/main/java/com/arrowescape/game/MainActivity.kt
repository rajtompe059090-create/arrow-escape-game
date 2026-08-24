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
            val currentLevelId = uiState.currentLevel?.id ?: 1
            val nextLevelId = currentLevelId + 1

            viewModel.loadLevel(nextLevelId)
            currentScreen = AppScreen.GAME
        }
    )
}
