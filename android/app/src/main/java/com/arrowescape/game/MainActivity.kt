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
            val currentId = uiState.currentLevel?.id ?: 1
            val nextId = currentId + 1

            viewModel.loadLevel(nextId)
            currentScreen = AppScreen.GAME
        }
    )
}
