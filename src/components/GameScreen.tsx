import React, { useState, useEffect, useCallback } from 'react';
import { ArrowLeft, RotateCcw, Lightbulb, Settings, Grid, Award, PlayCircle } from 'lucide-react';
import { Arrow, LevelData, UserStats } from '../types/game';
import { PuzzleBoard } from './PuzzleBoard';
import { isArrowPathClear, findFreeArrow } from '../engine/puzzleEngine';
import { sounds } from '../utils/audio';

interface GameScreenProps {
  level: LevelData;
  stats: UserStats;
  onBack: () => void;
  onLevelComplete: (levelId: number, moves: number, remainingLives: number) => void;
  onGameOver: (levelId: number) => void;
  onOpenLevels: () => void;
  onOpenSettings: () => void;
  onUseHint: () => void;
  onWatchHintAd?: () => void;
}

export const GameScreen: React.FC<GameScreenProps> = ({
  level,
  stats,
  onBack,
  onLevelComplete,
  onGameOver,
  onOpenLevels,
  onOpenSettings,
  onUseHint,
  onWatchHintAd,
}) => {
  const [remainingArrows, setRemainingArrows] = useState<Arrow[]>([]);
  const [escapingArrowIds, setEscapingArrowIds] = useState<string[]>([]);
  const [blockedArrowId, setBlockedArrowId] = useState<string | null>(null);
  const [hintedArrowId, setHintedArrowId] = useState<string | null>(null);
  const [lives, setLives] = useState<number>(3);
  const [movesCount, setMovesCount] = useState<number>(0);
  const [isProcessing, setIsProcessing] = useState<boolean>(false);
  const [isLevelCompleted, setIsLevelCompleted] = useState<boolean>(false);

  const [hintStatusMessage, setHintStatusMessage] = useState<string | null>(null);

  // Initialize/Reset level state
  const initLevel = useCallback(() => {
    setRemainingArrows([...level.arrows]);
    setEscapingArrowIds([]);
    setBlockedArrowId(null);
    setHintedArrowId(null);
    setHintStatusMessage(null);
    setLives(3);
    setMovesCount(0);
    setIsProcessing(false);
    setIsLevelCompleted(false);
  }, [level]);

  useEffect(() => {
    initLevel();
  }, [initLevel]);

  // Handle player tapping an arrow
  const handleArrowTap = (arrow: Arrow) => {
    // Guard against tapping during processing, game over, level complete, or while an arrow is already escaping/blocked
    if (
      isProcessing ||
      isLevelCompleted ||
      lives <= 0 ||
      escapingArrowIds.includes(arrow.id) ||
      blockedArrowId !== null
    ) {
      return;
    }

    const currentMoves = movesCount + 1;
    setMovesCount(currentMoves);

    // Clear active hint on tap
    if (hintedArrowId === arrow.id) {
      setHintedArrowId(null);
      setHintStatusMessage(null);
    }

    // Treat currently escaping arrows as already cleared from the board
    const activeArrows = remainingArrows.filter(a => !escapingArrowIds.includes(a.id));

    const check = isArrowPathClear(
      arrow,
      activeArrows,
      level.gridWidth,
      level.gridHeight
    );

    if (check.isClear) {
      // SUCCESS: Escape!
      sounds.playEscape();
      const updatedEscaping = [...escapingArrowIds, arrow.id];
      setEscapingArrowIds(updatedEscaping);

      // Remaining count after this arrow escapes
      const nextRemainingCount = remainingArrows.filter(
        a => a.id !== arrow.id && !escapingArrowIds.includes(a.id)
      ).length;

      if (nextRemainingCount === 0) {
        // Mark level as completed immediately to lock down any stray taps
        setIsLevelCompleted(true);
        setIsProcessing(true);
      }

      // Remove after flying animation completes
      setTimeout(() => {
        setRemainingArrows(prev => prev.filter(a => a.id !== arrow.id));
        setEscapingArrowIds(curr => curr.filter(id => id !== arrow.id));

        // Check if all arrows cleared!
        if (nextRemainingCount === 0) {
          sounds.playLevelComplete();
          onLevelComplete(level.id, currentMoves, lives);
        }
      }, 400);
    } else {
      // BLOCKED: Player loses 1 heart
      sounds.playBlocked();
      setBlockedArrowId(arrow.id);
      const newLives = lives - 1;
      setLives(newLives);

      setTimeout(() => {
        setBlockedArrowId(null);
        if (newLives <= 0) {
          setIsProcessing(true);
          sounds.playGameOver();
          onGameOver(level.id);
        }
      }, 400);
    }
  };

  // Handle Hint Button
  const handleHintClick = () => {
    if (isProcessing || isLevelCompleted || lives <= 0) return;

    if (stats.hintsRemaining <= 0) {
      if (onWatchHintAd) {
        sounds.playTap();
        onWatchHintAd();
      }
      return;
    }

    const activeArrows = remainingArrows.filter(a => !escapingArrowIds.includes(a.id));
    const freeArrow = findFreeArrow(
      activeArrows,
      level.gridWidth,
      level.gridHeight
    );

    if (freeArrow) {
      sounds.playHint();
      setHintedArrowId(freeArrow.id);
      setHintStatusMessage('Hint Unlocked: Follow glowing arrow!');
      onUseHint();

      // Auto-clear hint highlight and message after 4 seconds
      setTimeout(() => {
        setHintedArrowId(curr => (curr === freeArrow.id ? null : curr));
        setHintStatusMessage(null);
      }, 4000);
    }
  };

  // Difficulty badge colors
  let diffBadgeClass = 'bg-emerald-50 text-emerald-700 border-emerald-200';
  if (level.difficulty === 'Normal') diffBadgeClass = 'bg-blue-50 text-blue-700 border-blue-200';
  if (level.difficulty === 'Hard') diffBadgeClass = 'bg-amber-50 text-amber-700 border-amber-200';
  if (level.difficulty === 'Very Hard') diffBadgeClass = 'bg-orange-50 text-orange-700 border-orange-200';
  if (level.difficulty === 'Extreme') diffBadgeClass = 'bg-rose-50 text-rose-700 border-rose-200';

  return (
    <div
      id="game-screen"
      className="flex flex-col justify-between h-full w-full bg-white text-slate-900 select-none p-5"
    >
      {/* Top Bar matching Sleek Interface */}
      <div className="flex justify-between items-center mb-2">
        {/* Lives indicator */}
        <div id="lives-container" className="flex items-center gap-1">
          {[1, 2, 3].map(heartIndex => {
            const isAlive = heartIndex <= lives;
            return (
              <span
                key={heartIndex}
                className={`text-xl transition-all duration-200 ${
                  isAlive ? 'text-red-500 scale-100' : 'text-slate-200 scale-90 opacity-40'
                }`}
                role="img"
                aria-label="heart"
              >
                ❤️
              </span>
            );
          })}
        </div>

        {/* Level & Difficulty Info */}
        <div className="text-center">
          <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">
            Level {level.id < 10 ? `0${level.id}` : level.id}
          </p>
          <span className={`px-2 py-0.5 rounded-full text-[10px] font-black uppercase tracking-wider border ${diffBadgeClass}`}>
            {level.difficulty}
          </span>
        </div>

        {/* Actions / Menu Button */}
        <div className="flex items-center space-x-1.5">
          <button
            id="game-back-button"
            onClick={() => {
              sounds.playTap();
              onBack();
            }}
            className="w-8 h-8 rounded-full bg-slate-100 hover:bg-slate-200 active:scale-95 flex items-center justify-center text-slate-600 transition-all"
            title="Level Select"
          >
            <ArrowLeft className="w-4 h-4" />
          </button>
          <button
            id="game-settings-button"
            onClick={() => {
              sounds.playTap();
              onOpenSettings();
            }}
            className="w-8 h-8 rounded-full bg-slate-100 hover:bg-slate-200 active:scale-95 flex items-center justify-center text-slate-600 font-bold transition-all"
            title="Settings"
          >
            <Settings className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Interactive Board Area */}
      <div className="flex-1 flex flex-col items-center justify-center my-auto w-full relative">
        {hintStatusMessage && (
          <div className="absolute top-0 px-3 py-1 bg-amber-500 text-white font-black text-xs rounded-full shadow-md animate-bounce z-10 flex items-center gap-1.5 border border-amber-300">
            <Lightbulb className="w-3.5 h-3.5 fill-current" />
            <span>{hintStatusMessage}</span>
          </div>
        )}

        <PuzzleBoard
          gridWidth={level.gridWidth}
          gridHeight={level.gridHeight}
          arrows={remainingArrows}
          escapingArrowIds={escapingArrowIds}
          blockedArrowId={blockedArrowId}
          hintedArrowId={hintedArrowId}
          onArrowTap={handleArrowTap}
          disabled={lives <= 0 || isProcessing || isLevelCompleted}
        />
      </div>

      {/* Sleek Bottom Section: Remaining Count & Action Controls */}
      <div className="mt-2 flex flex-col items-center gap-3.5 w-full">
        {/* Remaining Arrows Metric */}
        <div className="text-center">
          <p className="text-slate-400 text-xs font-medium">Remaining Arrows</p>
          <p className="text-2xl font-black text-slate-800 tracking-tight">
            {remainingArrows.length} / {level.arrows.length}
          </p>
        </div>

        {/* Action Controls */}
        <div className="flex gap-2.5 w-full max-w-sm">
          {/* Hint Button (or Watch Ad for Hint) */}
          <button
            id="game-hint-button"
            onClick={handleHintClick}
            disabled={lives <= 0}
            className={`flex-1 bg-blue-600 hover:bg-blue-700 active:scale-98 text-white py-3.5 px-3 rounded-2xl font-bold text-xs shadow-lg shadow-blue-200 flex items-center justify-center space-x-1.5 transition-all`}
          >
            {stats.hintsRemaining > 0 ? (
              <>
                <Lightbulb className="w-4 h-4 fill-blue-200 shrink-0" />
                <span>HINT</span>
                <span className="ml-1 bg-blue-500/80 px-2 py-0.5 rounded-full text-xs font-mono border border-blue-400/40">
                  {stats.hintsRemaining}
                </span>
              </>
            ) : (
              <>
                <PlayCircle className="w-4 h-4 text-amber-300 fill-amber-300 shrink-0" />
                <span>+2 HINTS (AD)</span>
              </>
            )}
          </button>

          {/* Restart Level Button */}
          <button
            id="game-retry-button"
            onClick={() => {
              sounds.playTap();
              initLevel();
            }}
            className="w-12 bg-slate-100 hover:bg-slate-200 active:scale-95 text-slate-600 rounded-2xl flex items-center justify-center text-xl shadow-2xs transition-all shrink-0"
            title="Restart Level"
          >
            <RotateCcw className="w-4 h-4" />
          </button>

          {/* Grid Levels Quick Jump */}
          <button
            id="game-levels-quick-button"
            onClick={() => {
              sounds.playTap();
              onOpenLevels();
            }}
            className="w-12 bg-slate-100 hover:bg-slate-200 active:scale-95 text-slate-600 rounded-2xl flex items-center justify-center text-xl shadow-2xs transition-all shrink-0"
            title="All Levels"
          >
            <Grid className="w-4 h-4" />
          </button>
        </div>

        {/* Reward badge */}
        <div className="flex items-center justify-center gap-1.5 text-[11px] font-bold text-green-700">
          <Award className="w-3.5 h-3.5 text-green-600" />
          <span>Reward: +₹{level.rewardRupees}.00 Real ₹ on completion</span>
        </div>
      </div>
    </div>
  );
};
