import React, { useState, useEffect, useCallback } from 'react';
import { ArrowLeft, RotateCcw, Lightbulb, Heart, Settings, Grid, Award } from 'lucide-react';
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
}) => {
  const [remainingArrows, setRemainingArrows] = useState<Arrow[]>([]);
  const [escapingArrowIds, setEscapingArrowIds] = useState<string[]>([]);
  const [blockedArrowId, setBlockedArrowId] = useState<string | null>(null);
  const [hintedArrowId, setHintedArrowId] = useState<string | null>(null);
  const [lives, setLives] = useState<number>(3);
  const [movesCount, setMovesCount] = useState<number>(0);
  const [isProcessing, setIsProcessing] = useState<boolean>(false);

  // Initialize/Reset level state
  const initLevel = useCallback(() => {
    setRemainingArrows([...level.arrows]);
    setEscapingArrowIds([]);
    setBlockedArrowId(null);
    setHintedArrowId(null);
    setLives(3);
    setMovesCount(0);
    setIsProcessing(false);
  }, [level]);

  useEffect(() => {
    initLevel();
  }, [initLevel]);

  // Handle player tapping an arrow
  const handleArrowTap = (arrow: Arrow) => {
    if (isProcessing || lives <= 0 || escapingArrowIds.includes(arrow.id)) return;

    setMovesCount(prev => prev + 1);

    // Clear active hint on tap
    if (hintedArrowId === arrow.id) {
      setHintedArrowId(null);
    }

    const check = isArrowPathClear(
      arrow,
      remainingArrows,
      level.gridWidth,
      level.gridHeight
    );

    if (check.isClear) {
      // SUCCESS: Escape!
      sounds.playEscape();
      setEscapingArrowIds(prev => [...prev, arrow.id]);

      // Remove after flying animation completes
      setTimeout(() => {
        setRemainingArrows(prev => {
          const updated = prev.filter(a => a.id !== arrow.id);
          setEscapingArrowIds(curr => curr.filter(id => id !== arrow.id));

          // Check if all arrows cleared!
          if (updated.length === 0) {
            sounds.playLevelComplete();
            onLevelComplete(level.id, movesCount + 1, lives);
          }

          return updated;
        });
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
          sounds.playGameOver();
          onGameOver(level.id);
        }
      }, 450);
    }
  };

  // Handle Hint Button
  const handleHintClick = () => {
    if (stats.hintsRemaining <= 0 || isProcessing || lives <= 0) return;

    const freeArrow = findFreeArrow(
      remainingArrows,
      level.gridWidth,
      level.gridHeight
    );

    if (freeArrow) {
      sounds.playHint();
      setHintedArrowId(freeArrow.id);
      onUseHint();

      // Auto-clear hint highlight after 4 seconds
      setTimeout(() => {
        setHintedArrowId(curr => (curr === freeArrow.id ? null : curr));
      }, 4000);
    }
  };

  // Difficulty badge colors
  let diffBadgeClass = 'bg-emerald-50 text-emerald-700 border-emerald-200';
  if (level.difficulty === 'Normal') diffBadgeClass = 'bg-sky-50 text-sky-700 border-sky-200';
  if (level.difficulty === 'Hard') diffBadgeClass = 'bg-amber-50 text-amber-700 border-amber-200';
  if (level.difficulty === 'Expert') diffBadgeClass = 'bg-rose-50 text-rose-700 border-rose-200';

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
          <p className="text-xs font-bold text-blue-600 uppercase tracking-wider">
            {level.difficulty}
          </p>
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
      <div className="flex-1 flex items-center justify-center my-auto w-full">
        <PuzzleBoard
          gridWidth={level.gridWidth}
          gridHeight={level.gridHeight}
          arrows={remainingArrows}
          escapingArrowIds={escapingArrowIds}
          blockedArrowId={blockedArrowId}
          hintedArrowId={hintedArrowId}
          onArrowTap={handleArrowTap}
          disabled={lives <= 0}
        />
      </div>

      {/* Sleek Bottom Section: Remaining Count & Action Buttons */}
      <div className="mt-2 flex flex-col items-center gap-4 w-full">
        {/* Remaining Arrows Metric */}
        <div className="text-center">
          <p className="text-slate-400 text-xs font-medium">Remaining Arrows</p>
          <p className="text-2xl font-black text-slate-800 tracking-tight">
            {remainingArrows.length} / {level.arrows.length}
          </p>
        </div>

        {/* Action Controls */}
        <div className="flex gap-3 w-full max-w-sm">
          {/* Hint Button */}
          <button
            id="game-hint-button"
            onClick={handleHintClick}
            disabled={stats.hintsRemaining <= 0 || lives <= 0}
            className={`flex-1 bg-blue-600 hover:bg-blue-700 active:scale-98 text-white py-3.5 px-4 rounded-2xl font-bold text-sm shadow-lg shadow-blue-200 flex items-center justify-center space-x-2 transition-all ${
              stats.hintsRemaining > 0 ? '' : 'opacity-50 cursor-not-allowed'
            }`}
          >
            <Lightbulb className="w-4 h-4 fill-blue-200" />
            <span>HINT</span>
            <span className="ml-1 bg-blue-500/80 px-2 py-0.5 rounded-full text-xs font-mono border border-blue-400/40">
              {stats.hintsRemaining}
            </span>
          </button>

          {/* Restart Level Button */}
          <button
            id="game-retry-button"
            onClick={() => {
              sounds.playTap();
              initLevel();
            }}
            className="w-14 bg-slate-100 hover:bg-slate-200 active:scale-95 text-slate-600 rounded-2xl flex items-center justify-center text-xl shadow-inner transition-all shrink-0"
            title="Restart Level"
          >
            <RotateCcw className="w-5 h-5" />
          </button>

          {/* Grid Levels Quick Jump */}
          <button
            id="game-levels-quick-button"
            onClick={() => {
              sounds.playTap();
              onOpenLevels();
            }}
            className="w-14 bg-slate-100 hover:bg-slate-200 active:scale-95 text-slate-600 rounded-2xl flex items-center justify-center text-xl shadow-inner transition-all shrink-0"
            title="All Levels"
          >
            <Grid className="w-5 h-5" />
          </button>
        </div>

        {/* Small reward badge */}
        <div className="flex items-center justify-center gap-1.5 text-[11px] font-semibold text-slate-400">
          <Award className="w-3.5 h-3.5 text-amber-500" />
          <span>Reward: +₹{level.rewardRupees} on completion</span>
        </div>
      </div>
    </div>
  );
};
