import React, { useState } from 'react';
import { X, Copy, Check, FileCode, Folder, ChevronRight, Download } from 'lucide-react';
import { sounds } from '../utils/audio';

interface FileTreeItem {
  name: string;
  path: string;
  category: string;
  content: string;
}

const ANDROID_FILES: FileTreeItem[] = [
  {
    name: 'PuzzleEngine.kt',
    path: 'app/src/main/java/com/arrowescape/game/engine/PuzzleEngine.kt',
    category: 'Puzzle Engine & Game Logic',
    content: `package com.arrowescape.game.engine

import com.arrowescape.game.model.Arrow
import com.arrowescape.game.model.Direction
import com.arrowescape.game.model.GridPoint
import com.arrowescape.game.model.Level

/**
 * Pure Kotlin Puzzle Engine for Arrow Escape.
 * Handles collision raycasting, path verification, hint discovery, and level solvability.
 */
object PuzzleEngine {

    fun isArrowPathClear(
        arrow: Arrow,
        allArrows: List<Arrow>,
        gridWidth: Int,
        gridHeight: Int
    ): PathCheckResult {
        val head = arrow.points.last()
        var dx = 0
        var dy = 0

        when (arrow.headDirection) {
            Direction.UP -> dy = -1
            Direction.DOWN -> dy = 1
            Direction.LEFT -> dx = -1
            Direction.RIGHT -> dx = 1
        }

        var checkX = head.x + dx
        var checkY = head.y + dy

        // Raycast straight forward until the board boundary
        while (checkX in 0 until gridWidth && checkY in 0 until gridHeight) {
            val targetPoint = GridPoint(checkX, checkY)

            for (other in allArrows) {
                if (other.id == arrow.id) continue
                val occupied = getAllOccupiedPoints(other)
                if (occupied.any { it.x == targetPoint.x && it.y == targetPoint.y }) {
                    return PathCheckResult(
                        isClear = false,
                        blockingArrowId = other.id,
                        blockingPoint = targetPoint
                    )
                }
            }

            checkX += dx
            checkY += dy
        }

        return PathCheckResult(isClear = true)
    }

    fun findFreeArrow(
        arrows: List<Arrow>,
        gridWidth: Int,
        gridHeight: Int
    ): Arrow? {
        return arrows.firstOrNull { arrow ->
            isArrowPathClear(arrow, arrows, gridWidth, gridHeight).isClear
        }
    }

    fun getAllOccupiedPoints(arrow: Arrow): List<GridPoint> {
        val pointsSet = LinkedHashSet<GridPoint>()
        for (i in 0 until arrow.points.size - 1) {
            val p1 = arrow.points[i]
            val p2 = arrow.points[i + 1]

            val dx = (p2.x - p1.x).coerceIn(-1, 1)
            val dy = (p2.y - p1.y).coerceIn(-1, 1)

            var curX = p1.x
            var curY = p1.y
            pointsSet.add(GridPoint(curX, curY))

            while (curX != p2.x || curY != p2.y) {
                curX += dx
                curY += dy
                pointsSet.add(GridPoint(curX, curY))
            }
        }
        return pointsSet.toList()
    }

    fun calculateRewardRupees(levelId: Int): Int {
        return when {
            levelId <= 50 -> 2
            levelId <= 100 -> 3
            levelId <= 150 -> 5
            levelId <= 200 -> 10
            else -> 15
        }
    }
}

data class PathCheckResult(
    val isClear: boolean,
    val blockingArrowId: String? = null,
    val blockingPoint: GridPoint? = null
)`
  },
  {
    name: 'GameViewModel.kt',
    path: 'app/src/main/java/com/arrowescape/game/viewmodel/GameViewModel.kt',
    category: 'ViewModel & State',
    content: `package com.arrowescape.game.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrowescape.game.data.LevelRepository
import com.arrowescape.game.data.UserPreferencesRepository
import com.arrowescape.game.engine.PuzzleEngine
import com.arrowescape.game.model.Arrow
import com.arrowescape.game.model.Level
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GameUiState(
    val currentLevel: Level? = null,
    val remainingArrows: List<Arrow> = emptyList(),
    val escapingArrowIds: Set<String> = emptySet(),
    val blockedArrowId: String? = null,
    val hintedArrowId: String? = null,
    val lives: Int = 3,
    val maxLives: Int = 3,
    val movesCount: Int = 0,
    val isLevelCompleted: Boolean = false,
    val isGameOver: Boolean = false,
    val unlockedLevel: Int = 1,
    val completedLevels: Set<Int> = emptySet(),
    val earnedRupees: Int = 0,
    val hintsRemaining: Int = 3,
    val soundEnabled: Boolean = true
)

class GameViewModel(
    private val prefsRepo: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            prefsRepo.userPreferencesFlow.collect { prefs ->
                _uiState.update {
                    it.copy(
                        unlockedLevel = prefs.unlockedLevel,
                        completedLevels = prefs.completedLevels,
                        earnedRupees = prefs.earnedRupees,
                        hintsRemaining = prefs.hintsRemaining,
                        soundEnabled = prefs.soundEnabled
                    )
                }
            }
        }
    }

    fun loadLevel(levelId: Int) {
        val level = LevelRepository.getLevel(levelId) ?: return
        _uiState.update {
            it.copy(
                currentLevel = level,
                remainingArrows = level.arrows,
                escapingArrowIds = emptySet(),
                blockedArrowId = null,
                hintedArrowId = null,
                lives = 3,
                movesCount = 0,
                isLevelCompleted = false,
                isGameOver = false
            )
        }
    }

    fun onArrowTapped(arrow: Arrow) {
        val state = _uiState.value
        val level = state.currentLevel ?: return
        if (state.lives <= 0 || state.isLevelCompleted || state.escapingArrowIds.contains(arrow.id)) return

        val check = PuzzleEngine.isArrowPathClear(
            arrow = arrow,
            allArrows = state.remainingArrows,
            gridWidth = level.gridWidth,
            gridHeight = level.gridHeight
        )

        if (check.isClear) {
            _uiState.update {
                it.copy(
                    escapingArrowIds = it.escapingArrowIds + arrow.id,
                    movesCount = it.movesCount + 1,
                    hintedArrowId = if (it.hintedArrowId == arrow.id) null else it.hintedArrowId
                )
            }

            viewModelScope.launch {
                delay(380)
                val updatedArrows = _uiState.value.remainingArrows.filter { it.id != arrow.id }
                val isCompleted = updatedArrows.isEmpty()

                _uiState.update {
                    it.copy(
                        remainingArrows = updatedArrows,
                        escapingArrowIds = it.escapingArrowIds - arrow.id,
                        isLevelCompleted = isCompleted
                    )
                }

                if (isCompleted) {
                    val reward = PuzzleEngine.calculateRewardRupees(level.id)
                    prefsRepo.recordLevelCompleted(level.id, reward)
                }
            }
        } else {
            val newLives = state.lives - 1
            _uiState.update {
                it.copy(
                    blockedArrowId = arrow.id,
                    lives = newLives,
                    movesCount = it.movesCount + 1
                )
            }

            viewModelScope.launch {
                delay(400)
                _uiState.update {
                    it.copy(
                        blockedArrowId = null,
                        isGameOver = newLives <= 0
                    )
                }
            }
        }
    }

    fun useHint() {
        val state = _uiState.value
        val level = state.currentLevel ?: return
        if (state.hintsRemaining <= 0 || state.lives <= 0) return

        val free = PuzzleEngine.findFreeArrow(state.remainingArrows, level.gridWidth, level.gridHeight)
        if (free != null) {
            _uiState.update { it.copy(hintedArrowId = free.id) }
            viewModelScope.launch {
                prefsRepo.decrementHint()
                delay(4000)
                _uiState.update { if (it.hintedArrowId == free.id) it.copy(hintedArrowId = null) else it }
            }
        }
    }

    fun restartCurrentLevel() {
        _uiState.value.currentLevel?.let { loadLevel(it.id) }
    }

    fun toggleSound() {
        viewModelScope.launch {
            prefsRepo.setSoundEnabled(!_uiState.value.soundEnabled)
        }
    }
}`
  },
  {
    name: 'PuzzleBoard.kt',
    path: 'app/src/main/java/com/arrowescape/game/ui/components/PuzzleBoard.kt',
    category: 'Compose UI Component',
    content: `package com.arrowescape.game.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.arrowescape.game.model.Arrow
import com.arrowescape.game.model.Direction
import com.arrowescape.game.model.GridPoint

@Composable
fun PuzzleBoard(
    gridWidth: Int,
    gridHeight: Int,
    arrows: List<Arrow>,
    escapingArrowIds: Set<String>,
    blockedArrowId: String?,
    hintedArrowId: String?,
    onArrowTapped: (Arrow) -> Unit,
    modifier: Modifier = Modifier
) {
    val navyColor = Color(0xFF1E293B)
    val escapeColor = Color(0xFF0284C7)
    val blockedColor = Color(0xFFEF4444)
    val dotColor = Color(0xFFCBD5E1)

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(24.dp)
                .pointerInput(arrows) {
                    detectTapGestures { tapOffset ->
                        val cellW = size.width / (gridWidth - 1).coerceAtLeast(1)
                        val cellH = size.height / (gridHeight - 1).coerceAtLeast(1)

                        // Hit test all arrows
                        val hitArrow = arrows.findLast { arrow ->
                            arrow.points.any { pt ->
                                val ptOffset = Offset(pt.x * cellW, pt.y * cellH)
                                (tapOffset - ptOffset).getDistance() <= cellW * 0.65f
                            }
                        }
                        hitArrow?.let { onArrowTapped(it) }
                    }
                }
        ) {
            val cellW = size.width / (gridWidth - 1).coerceAtLeast(1)
            val cellH = size.height / (gridHeight - 1).coerceAtLeast(1)

            // Draw grid dots
            for (gx in 0 until gridWidth) {
                for (gy in 0 until gridHeight) {
                    drawCircle(
                        color = dotColor,
                        radius = 3.dp.toPx(),
                        center = Offset(gx * cellW, gy * cellH)
                    )
                }
            }

            // Draw arrows
            arrows.forEach { arrow ->
                val isEscaping = escapingArrowIds.contains(arrow.id)
                val isBlocked = blockedArrowId == arrow.id
                val isHinted = hintedArrowId == arrow.id

                val color = when {
                    isBlocked -> blockedColor
                    isEscaping || isHinted -> escapeColor
                    else -> navyColor
                }

                val strokeWidth = if (isHinted) 7.dp.toPx() else 5.5.dp.toPx()

                // Draw path lines
                if (arrow.points.size >= 2) {
                    val path = Path().apply {
                        val first = arrow.points.first()
                        moveTo(first.x * cellW, first.y * cellH)
                        for (i in 1 until arrow.points.size) {
                            val pt = arrow.points[i]
                            lineTo(pt.x * cellW, pt.y * cellH)
                        }
                    }

                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }

                // Draw Arrowhead
                val head = arrow.points.last()
                val hx = head.x * cellW
                val hy = head.y * cellH
                val headLen = 14.dp.toPx()
                val headHalf = 8.dp.toPx()

                val headPath = Path().apply {
                    when (arrow.headDirection) {
                        Direction.UP -> {
                            moveTo(hx, hy - 2.dp.toPx())
                            lineTo(hx - headHalf, hy + headLen)
                            lineTo(hx + headHalf, hy + headLen)
                        }
                        Direction.DOWN -> {
                            moveTo(hx, hy + 2.dp.toPx())
                            lineTo(hx - headHalf, hy - headLen)
                            lineTo(hx + headHalf, hy - headLen)
                        }
                        Direction.LEFT -> {
                            moveTo(hx - 2.dp.toPx(), hy)
                            lineTo(hx + headLen, hy - headHalf)
                            lineTo(hx + headLen, hy + headHalf)
                        }
                        Direction.RIGHT -> {
                            moveTo(hx + 2.dp.toPx(), hy)
                            lineTo(hx - headLen, hy - headHalf)
                            lineTo(hx - headLen, hy + headHalf)
                        }
                    }
                    close()
                }

                drawPath(path = headPath, color = color)
            }
        }
    }
}`
  },
  {
    name: 'build.gradle.kts (app)',
    path: 'app/build.gradle.kts',
    category: 'Gradle Configuration',
    content: `plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.arrowescape.game"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.arrowescape.game"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.core:core-ktx:1.15.0")
}`
  },
  {
    name: 'android-build.yml',
    path: '.github/workflows/android-build.yml',
    category: 'CI/CD & GitHub Actions',
    content: `name: Android Build & Package APK

on:
  push:
    branches: [ "main", "master" ]
  pull_request:
    branches: [ "main", "master" ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout Code
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: gradle

      - name: Grant execute permission for gradlew
        run: chmod +x ./gradlew || true

      - name: Build Debug APK
        run: ./gradlew assembleDebug --stacktrace

      - name: Upload Debug APK Artifact
        uses: actions/upload-artifact@v4
        with:
          name: arrow-escape-debug-apk
          path: app/build/outputs/apk/debug/app-debug.apk`
  }
];

interface CodeExplorerModalProps {
  onClose: () => void;
}

export const CodeExplorerModal: React.FC<CodeExplorerModalProps> = ({ onClose }) => {
  const [selectedFile, setSelectedFile] = useState<FileTreeItem>(ANDROID_FILES[0]);
  const [copied, setCopied] = useState<boolean>(false);

  const handleCopy = () => {
    sounds.playTap();
    navigator.clipboard.writeText(selectedFile.content);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div
      id="code-explorer-modal"
      className="fixed inset-0 z-50 flex items-center justify-center p-3 bg-slate-950/70 backdrop-blur-xs select-none animate-fade-in"
    >
      <div className="relative w-full max-w-2xl bg-slate-900 text-slate-100 rounded-3xl p-5 shadow-2xl flex flex-col space-y-3 border border-slate-800 h-[88vh] max-h-[720px]">
        
        {/* Header */}
        <div className="flex items-center justify-between border-b border-slate-800 pb-3">
          <div className="flex items-center space-x-2.5">
            <div className="p-2 bg-sky-500/20 text-sky-400 rounded-xl border border-sky-500/30">
              <FileCode className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-base font-bold text-white">Android Jetpack Compose Codebase</h2>
              <p className="text-xs text-slate-400">Complete Kotlin source files & Gradle setup</p>
            </div>
          </div>
          <button
            onClick={() => {
              sounds.playTap();
              onClose();
            }}
            className="p-2 bg-slate-800 hover:bg-slate-700 rounded-full text-slate-300 transition-all"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Content Layout */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-3 flex-1 overflow-hidden min-h-0">
          {/* File Picker Sidebar */}
          <div className="bg-slate-950/60 rounded-2xl p-2 border border-slate-800/80 overflow-y-auto space-y-1">
            <span className="text-[11px] font-bold text-slate-500 uppercase tracking-wider px-2 py-1 block">
              Android Project Files
            </span>
            {ANDROID_FILES.map(file => {
              const isSelected = selectedFile.name === file.name;
              return (
                <button
                  key={file.name}
                  onClick={() => {
                    sounds.playTap();
                    setSelectedFile(file);
                    setCopied(false);
                  }}
                  className={`w-full flex items-center justify-between p-2.5 rounded-xl text-xs font-semibold transition-all text-left ${
                    isSelected
                      ? 'bg-blue-600 text-white shadow-xs'
                      : 'text-slate-300 hover:bg-slate-800/80'
                  }`}
                >
                  <div className="truncate mr-1">
                    <p className="truncate">{file.name}</p>
                    <p className="text-[10px] opacity-75">{file.category}</p>
                  </div>
                  <ChevronRight className={`w-3.5 h-3.5 shrink-0 ${isSelected ? 'opacity-100' : 'opacity-40'}`} />
                </button>
              );
            })}
          </div>

          {/* Code Viewer */}
          <div className="md:col-span-2 bg-slate-950 rounded-2xl border border-slate-800/90 flex flex-col overflow-hidden">
            <div className="flex items-center justify-between px-4 py-2.5 bg-slate-900/90 border-b border-slate-800">
              <span className="text-xs font-mono text-blue-300 truncate max-w-[280px]">
                {selectedFile.path}
              </span>
              <button
                onClick={handleCopy}
                className="flex items-center space-x-1.5 px-3 py-1 bg-slate-800 hover:bg-slate-700 active:scale-95 text-xs font-medium text-slate-200 rounded-lg transition-all"
              >
                {copied ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5 text-slate-400" />}
                <span>{copied ? 'Copied' : 'Copy'}</span>
              </button>
            </div>

            <pre className="p-4 text-xs font-mono text-slate-200 overflow-auto flex-1 select-text leading-relaxed">
              <code>{selectedFile.content}</code>
            </pre>
          </div>
        </div>

        {/* Footer */}
        <div className="flex items-center justify-between pt-1 border-t border-slate-800/80 text-xs text-slate-400">
          <span>Target SDK 35 • Kotlin 2.0 • Jetpack Compose</span>
          <button
            onClick={() => {
              sounds.playTap();
              onClose();
            }}
            className="px-4 py-2 bg-blue-600 hover:bg-blue-500 active:scale-95 text-white font-bold rounded-xl text-xs transition-all shadow-md shadow-blue-900/30"
          >
            Back to Game
          </button>
        </div>
      </div>
    </div>
  );
};
