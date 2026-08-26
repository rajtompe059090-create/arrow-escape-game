import React, { useState, useEffect, useRef, useMemo, useCallback } from 'react';
import {
  ArrowLeft,
  Lock,
  Star,
  CheckCircle,
  Award,
  Sparkles,
  Zap,
  Flame,
  ShieldAlert,
  Crown,
  ChevronRight,
  Compass,
  ChevronsRight,
} from 'lucide-react';
import { UserStats, DifficultyTier } from '../types/game';
import { sounds } from '../utils/audio';
import { getLevelDifficulty, calculateLevelReward } from '../engine/puzzleEngine';

interface LevelSelectScreenProps {
  stats: UserStats;
  onSelectLevel: (levelId: number) => void;
  onBack: () => void;
}

interface TierInfo {
  id: DifficultyTier;
  label: string;
  shortLabel: string;
  start: number;
  end: number | null; // null represents infinite/unbounded (201+)
  defaultEnd: number;
  reward: number;
  badgeBg: string;
  badgeBorder: string;
  badgeText: string;
  activeBg: string;
  icon: React.ReactNode;
}

const TIERS: TierInfo[] = [
  {
    id: 'Easy',
    label: 'Levels 1–100',
    shortLabel: 'Easy',
    start: 1,
    end: 100,
    defaultEnd: 100,
    reward: 0.25,
    badgeBg: 'bg-emerald-50',
    badgeBorder: 'border-emerald-200',
    badgeText: 'text-emerald-700',
    activeBg: 'bg-emerald-600',
    icon: <Zap className="w-3.5 h-3.5" />,
  },
  {
    id: 'Normal',
    label: 'Levels 101–200',
    shortLabel: 'Normal',
    start: 101,
    end: 200,
    defaultEnd: 200,
    reward: 0.50,
    badgeBg: 'bg-blue-50',
    badgeBorder: 'border-blue-200',
    badgeText: 'text-blue-700',
    activeBg: 'bg-blue-600',
    icon: <Sparkles className="w-3.5 h-3.5" />,
  },
  {
    id: 'Hard',
    label: 'Levels 201–300',
    shortLabel: 'Hard',
    start: 201,
    end: 300,
    defaultEnd: 300,
    reward: 0.75,
    badgeBg: 'bg-amber-50',
    badgeBorder: 'border-amber-200',
    badgeText: 'text-amber-700',
    activeBg: 'bg-amber-600',
    icon: <Flame className="w-3.5 h-3.5" />,
  },
  {
    id: 'Very Hard',
    label: 'Levels 301–400',
    shortLabel: 'Very Hard',
    start: 301,
    end: 400,
    defaultEnd: 400,
    reward: 1.00,
    badgeBg: 'bg-orange-50',
    badgeBorder: 'border-orange-200',
    badgeText: 'text-orange-700',
    activeBg: 'bg-orange-600',
    icon: <ShieldAlert className="w-3.5 h-3.5" />,
  },
  {
    id: 'Master',
    label: 'Levels 401–600',
    shortLabel: 'Master',
    start: 401,
    end: 600,
    defaultEnd: 600,
    reward: 1.25,
    badgeBg: 'bg-purple-50',
    badgeBorder: 'border-purple-200',
    badgeText: 'text-purple-700',
    activeBg: 'bg-purple-600',
    icon: <Crown className="w-3.5 h-3.5" />,
  },
  {
    id: 'Grandmaster',
    label: 'Levels 601–800',
    shortLabel: 'Grandmaster',
    start: 601,
    end: 800,
    defaultEnd: 800,
    reward: 1.50,
    badgeBg: 'bg-indigo-50',
    badgeBorder: 'border-indigo-200',
    badgeText: 'text-indigo-700',
    activeBg: 'bg-indigo-600',
    icon: <Crown className="w-3.5 h-3.5" />,
  },
  {
    id: 'Legendary',
    label: 'Levels 801+',
    shortLabel: 'Legendary',
    start: 801,
    end: null, // Unlimited future levels
    defaultEnd: 1000,
    reward: 2.00,
    badgeBg: 'bg-rose-50',
    badgeBorder: 'border-rose-200',
    badgeText: 'text-rose-700',
    activeBg: 'bg-rose-600',
    icon: <Crown className="w-3.5 h-3.5" />,
  },
];

const INITIAL_CHUNK_SIZE = 28;
const LOAD_MORE_CHUNK_SIZE = 24;

// Memoized individual level card component for ultra-smooth 60fps scrolling
interface LevelCardProps {
  levelId: number;
  isUnlocked: boolean;
  isCompleted: boolean;
  isCurrent: boolean;
  reward: number;
  onSelect: (levelId: number) => void;
}

const LevelCard: React.FC<LevelCardProps> = React.memo(
  ({ levelId, isUnlocked, isCompleted, isCurrent, reward, onSelect }) => {
    return (
      <button
        id={`level-btn-${levelId}`}
        onClick={() => onSelect(levelId)}
        className={`relative aspect-square rounded-2xl flex flex-col items-center justify-between p-2.5 transition-all duration-150 active:scale-95 ${
          isCurrent
            ? 'bg-gradient-to-b from-blue-500 to-indigo-600 text-white ring-4 ring-blue-300 shadow-md shadow-blue-500/30 font-black'
            : isCompleted
            ? 'bg-white hover:bg-slate-50 text-slate-900 border-2 border-emerald-400 shadow-2xs font-bold'
            : isUnlocked
            ? 'bg-white hover:bg-slate-50 text-slate-800 border border-slate-200 shadow-2xs font-bold'
            : 'bg-slate-100/90 text-slate-400 border border-slate-200 cursor-not-allowed opacity-60'
        }`}
      >
        {/* Top Header: Level # */}
        <div className="w-full flex items-center justify-between">
          <span
            className={`text-xs font-black ${
              isCurrent
                ? 'text-white'
                : isCompleted
                ? 'text-emerald-700'
                : isUnlocked
                ? 'text-slate-800'
                : 'text-slate-400'
            }`}
          >
            #{levelId}
          </span>

          {isCompleted && (
            <span className="w-3.5 h-3.5 rounded-full bg-emerald-500 text-white flex items-center justify-center text-[9px] shadow-2xs">
              ✓
            </span>
          )}
          {isCurrent && (
            <span className="w-2 h-2 rounded-full bg-amber-300 animate-ping" />
          )}
        </div>

        {/* Center: Status Icon */}
        <div className="my-auto flex flex-col items-center">
          {isCurrent ? (
            <div className="flex flex-col items-center">
              <Star className="w-5 h-5 fill-amber-300 text-amber-300 animate-pulse" />
              <span className="text-[9px] font-black uppercase tracking-wider text-white mt-0.5">
                PLAY
              </span>
            </div>
          ) : isCompleted ? (
            <CheckCircle className="w-5 h-5 text-emerald-600 fill-emerald-100" />
          ) : isUnlocked ? (
            <Compass className="w-4 h-4 text-blue-500" />
          ) : (
            <Lock className="w-4 h-4 text-slate-400" />
          )}
        </div>

        {/* Bottom: ₹ Cash Reward Tag */}
        <div className="w-full flex items-center justify-center">
          <span
            className={`text-[9px] font-extrabold px-1.5 py-0.5 rounded-md ${
              isCurrent
                ? 'bg-white/20 text-white'
                : isCompleted
                ? 'bg-emerald-50 text-emerald-700'
                : isUnlocked
                ? 'bg-slate-100 text-slate-600'
                : 'text-slate-400'
            }`}
          >
            ₹{reward}
          </span>
        </div>
      </button>
    );
  }
);

LevelCard.displayName = 'LevelCard';

export const LevelSelectScreen: React.FC<LevelSelectScreenProps> = ({
  stats,
  onSelectLevel,
  onBack,
}) => {
  // Determine initial tier index based on player's current unlocked level
  const [selectedTierIdx, setSelectedTierIdx] = useState<number>(() => {
    const lvl = stats.unlockedLevel;
    if (lvl <= 50) return 0;
    if (lvl <= 100) return 1;
    if (lvl <= 150) return 2;
    if (lvl <= 200) return 3;
    return 4;
  });

  const currentTier = TIERS[selectedTierIdx];

  // Dynamic max level for the current tier
  // If extreme tier (end === null), it expands dynamically beyond defaultEnd to support unlimited levels
  const currentTierMax = useMemo(() => {
    if (currentTier.end !== null) {
      return currentTier.end;
    }
    // Extreme tier: ensure it covers at least current unlockedLevel + 20, or defaultEnd (250)
    return Math.max(currentTier.defaultEnd, Math.ceil((stats.unlockedLevel + 20) / 20) * 20);
  }, [currentTier, stats.unlockedLevel]);

  // Track how many levels of the current tier are rendered to avoid rendering thousands of DOM nodes at once
  const [visibleCount, setVisibleCount] = useState<number>(() => {
    const totalLevelsInTier = currentTierMax - currentTier.start + 1;
    // If active level is in this tier, ensure visible count covers it
    if (stats.unlockedLevel >= currentTier.start && stats.unlockedLevel <= currentTierMax) {
      const neededForCurrent = stats.unlockedLevel - currentTier.start + 8;
      return Math.min(totalLevelsInTier, Math.max(INITIAL_CHUNK_SIZE, neededForCurrent));
    }
    return Math.min(totalLevelsInTier, INITIAL_CHUNK_SIZE);
  });

  // Extreme infinite expansion counter (for loading more beyond currentTierMax)
  const [extremeExtraCap, setExtremeExtraCap] = useState<number>(0);

  const [lockedNotice, setLockedNotice] = useState<string | null>(null);
  const gridContainerRef = useRef<HTMLDivElement>(null);
  const sentinelRef = useRef<HTMLDivElement>(null);

  // Total ceiling for this tier taking extremeExtraCap into account
  const activeTierCeiling = useMemo(() => {
    if (currentTier.end !== null) {
      return currentTier.end;
    }
    return currentTierMax + extremeExtraCap;
  }, [currentTier, currentTierMax, extremeExtraCap]);

  const totalLevelsInActiveTier = activeTierCeiling - currentTier.start + 1;

  // Reset or adjust visible count whenever tier changes
  useEffect(() => {
    const totalInTier = activeTierCeiling - currentTier.start + 1;
    if (stats.unlockedLevel >= currentTier.start && stats.unlockedLevel <= activeTierCeiling) {
      const neededForCurrent = stats.unlockedLevel - currentTier.start + 8;
      setVisibleCount(Math.min(totalInTier, Math.max(INITIAL_CHUNK_SIZE, neededForCurrent)));
    } else {
      setVisibleCount(Math.min(totalInTier, INITIAL_CHUNK_SIZE));
    }
  }, [selectedTierIdx, activeTierCeiling, currentTier.start, stats.unlockedLevel]);

  // Auto-scroll to active current level when entering tier
  useEffect(() => {
    const currentBtn = document.getElementById(`level-btn-${stats.unlockedLevel}`);
    if (currentBtn && gridContainerRef.current) {
      currentBtn.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
  }, [selectedTierIdx, stats.unlockedLevel]);

  // Load more levels when scrolling near the bottom (efficient chunk rendering)
  const handleLoadMore = useCallback(() => {
    setVisibleCount(prev => {
      if (prev < totalLevelsInActiveTier) {
        return Math.min(totalLevelsInActiveTier, prev + LOAD_MORE_CHUNK_SIZE);
      }
      // If Extreme tier and reached ceiling, expand the ceiling dynamically!
      if (currentTier.end === null) {
        setExtremeExtraCap(extra => extra + 40);
        return prev + LOAD_MORE_CHUNK_SIZE;
      }
      return prev;
    });
  }, [totalLevelsInActiveTier, currentTier.end]);

  // Scroll listener / Intersection Observer on sentinel for smooth infinite scrolling
  useEffect(() => {
    const sentinel = sentinelRef.current;
    if (!sentinel) return;

    const observer = new IntersectionObserver(
      entries => {
        if (entries[0]?.isIntersecting) {
          handleLoadMore();
        }
      },
      { root: gridContainerRef.current, rootMargin: '120px' }
    );

    observer.observe(sentinel);
    return () => observer.disconnect();
  }, [handleLoadMore]);

  // Generate rendered level IDs slice efficiently
  const renderedLevelIds = useMemo(() => {
    const count = Math.min(visibleCount, totalLevelsInActiveTier);
    return Array.from({ length: count }, (_, i) => currentTier.start + i);
  }, [currentTier.start, visibleCount, totalLevelsInActiveTier]);

  const handleLevelClick = useCallback(
    (levelId: number) => {
      if (levelId <= stats.unlockedLevel) {
        sounds.playTap();
        onSelectLevel(levelId);
      } else {
        sounds.playBlocked();
        setLockedNotice(`Complete Level ${levelId - 1} to unlock!`);
        setTimeout(() => setLockedNotice(null), 2500);
      }
    },
    [stats.unlockedLevel, onSelectLevel]
  );

  const jumpToCurrentLevel = useCallback(() => {
    sounds.playTap();
    const lvl = stats.unlockedLevel;
    let targetIdx = 0;
    if (lvl <= 50) targetIdx = 0;
    else if (lvl <= 100) targetIdx = 1;
    else if (lvl <= 150) targetIdx = 2;
    else if (lvl <= 200) targetIdx = 3;
    else targetIdx = 4;

    setSelectedTierIdx(targetIdx);
    onSelectLevel(lvl);
  }, [stats.unlockedLevel, onSelectLevel]);

  // Count completions per tier
  const getTierCompletedCount = (tier: TierInfo) => {
    const endVal = tier.end ?? activeTierCeiling;
    return stats.completedLevels.filter(lvl => lvl >= tier.start && lvl <= endVal).length;
  };

  const currentTierCompleted = getTierCompletedCount(currentTier);

  return (
    <div
      id="level-select-screen"
      className="flex flex-col h-full w-full bg-slate-50 text-slate-900 select-none overflow-hidden"
    >
      {/* 1. Header Bar */}
      <div className="bg-white px-4 py-3 border-b border-slate-200 shadow-2xs shrink-0 flex items-center justify-between z-10">
        <button
          id="level-select-back-btn"
          onClick={() => {
            sounds.playTap();
            onBack();
          }}
          className="w-9 h-9 rounded-xl bg-slate-100 hover:bg-slate-200 active:scale-95 flex items-center justify-center text-slate-700 transition-all border border-slate-200/80"
          title="Back to Home"
        >
          <ArrowLeft className="w-4 h-4" />
        </button>

        <div className="text-center">
          <h1 className="text-base font-black tracking-tight text-slate-900">
            Select Level
          </h1>
          <p className="text-[11px] font-bold text-slate-500">
            <span className="text-blue-600 font-extrabold">{stats.completedLevels.length}</span> Solved • Current: Level {stats.unlockedLevel}
          </p>
        </div>

        <div className="flex items-center space-x-1.5 px-3 py-1.5 bg-emerald-50 border border-emerald-200 rounded-xl text-xs font-black text-emerald-700">
          <Award className="w-3.5 h-3.5 text-emerald-600" />
          <span>₹{stats.walletBalance.toFixed(2)}</span>
        </div>
      </div>

      {/* 2. Tier / Difficulty Category Tabs */}
      <div className="bg-white border-b border-slate-200 px-3 py-2 shrink-0">
        <div className="flex items-center space-x-2 overflow-x-auto no-scrollbar py-0.5">
          {TIERS.map((tier, idx) => {
            const isSelected = selectedTierIdx === idx;
            const isTierUnlocked = stats.unlockedLevel >= tier.start;

            return (
              <button
                key={tier.id}
                id={`tier-tab-${tier.id.toLowerCase().replace(' ', '-')}`}
                onClick={() => {
                  sounds.playTap();
                  setSelectedTierIdx(idx);
                }}
                className={`flex flex-col items-start px-3.5 py-2 rounded-2xl transition-all shrink-0 border text-left ${
                  isSelected
                    ? `${tier.activeBg} text-white shadow-sm border-transparent`
                    : isTierUnlocked
                    ? 'bg-slate-50 hover:bg-slate-100 text-slate-700 border-slate-200'
                    : 'bg-slate-50/60 text-slate-400 border-slate-200 opacity-60'
                }`}
              >
                <div className="flex items-center space-x-1.5 w-full">
                  <span className={isSelected ? 'text-white' : tier.badgeText}>
                    {tier.icon}
                  </span>
                  <span className="text-xs font-black uppercase tracking-tight">
                    {tier.shortLabel}
                  </span>
                </div>
                <div className="flex items-center justify-between w-full mt-1 space-x-3 text-[10px]">
                  <span className={isSelected ? 'text-white/80 font-bold' : 'text-slate-400 font-medium'}>
                    {tier.label}
                  </span>
                  <span
                    className={`font-black px-1.5 py-0.2 rounded-md ${
                      isSelected
                        ? 'bg-white/20 text-white'
                        : 'bg-emerald-50 text-emerald-700 border border-emerald-200/60'
                    }`}
                  >
                    +₹{tier.reward}
                  </span>
                </div>
              </button>
            );
          })}
        </div>
      </div>

      {/* 3. Tier Banner & Progress Header */}
      <div className="bg-slate-100/80 px-4 py-2.5 border-b border-slate-200 flex items-center justify-between shrink-0">
        <div className="flex items-center space-x-2">
          <span
            className={`px-2.5 py-0.5 rounded-full font-black text-xs border ${currentTier.badgeBg} ${currentTier.badgeBorder} ${currentTier.badgeText}`}
          >
            {currentTier.id.toUpperCase()} • ₹{currentTier.reward} Reward / Level
          </span>
        </div>
        <span className="text-xs font-bold text-slate-600">
          {currentTierCompleted} Cleared {currentTier.end ? `/ ${totalLevelsInActiveTier}` : ''}
        </span>
      </div>

      {/* 4. Locked Notification Toast */}
      {lockedNotice && (
        <div className="bg-amber-500 text-white text-xs font-black px-4 py-2 text-center shadow-md animate-in slide-in-from-top-2 duration-200">
          {lockedNotice}
        </div>
      )}

      {/* 5. Scrollable Level Grid with Chunk Loading */}
      <div
        ref={gridContainerRef}
        className="flex-1 overflow-y-auto p-4 max-w-md mx-auto w-full"
      >
        <div className="grid grid-cols-4 gap-3">
          {renderedLevelIds.map(levelId => {
            const isUnlocked = levelId <= stats.unlockedLevel;
            const isCompleted = stats.completedLevels.includes(levelId);
            const isCurrent = levelId === stats.unlockedLevel;
            const reward = calculateLevelReward(levelId);

            return (
              <LevelCard
                key={levelId}
                levelId={levelId}
                isUnlocked={isUnlocked}
                isCompleted={isCompleted}
                isCurrent={isCurrent}
                reward={reward}
                onSelect={handleLevelClick}
              />
            );
          })}
        </div>

        {/* Sentinel & Load More Indicator */}
        <div ref={sentinelRef} className="py-4 flex flex-col items-center justify-center">
          {visibleCount < totalLevelsInActiveTier || currentTier.end === null ? (
            <button
              onClick={handleLoadMore}
              className="py-2 px-4 bg-white hover:bg-slate-100 text-slate-600 text-xs font-bold rounded-xl border border-slate-200 shadow-2xs flex items-center space-x-1.5 active:scale-95 transition-all"
            >
              <span>Load More Levels</span>
              <ChevronsRight className="w-3.5 h-3.5 text-slate-400" />
            </button>
          ) : (
            <span className="text-[11px] font-bold text-slate-400">
              ✓ All {totalLevelsInActiveTier} {currentTier.shortLabel} levels loaded
            </span>
          )}
        </div>
      </div>

      {/* 6. Bottom Sticky Action Bar: Jump to Current Level */}
      <div className="bg-white p-3 border-t border-slate-200 shadow-lg shrink-0 flex items-center justify-between max-w-md mx-auto w-full">
        <div className="flex items-center space-x-2">
          <div className="w-8 h-8 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center font-black text-xs border border-blue-200">
            #{stats.unlockedLevel}
          </div>
          <div>
            <p className="text-[10px] font-extrabold text-slate-400 uppercase tracking-wider">
              Active Progress
            </p>
            <p className="text-xs font-black text-slate-900">
              Level {stats.unlockedLevel} ({getLevelDifficulty(stats.unlockedLevel)})
            </p>
          </div>
        </div>

        <button
          id="jump-to-current-level-btn"
          onClick={jumpToCurrentLevel}
          className="py-2.5 px-4 bg-blue-600 hover:bg-blue-700 active:scale-95 text-white font-black rounded-xl text-xs flex items-center space-x-1.5 shadow-md shadow-blue-500/20 transition-all"
        >
          <span>Play Level {stats.unlockedLevel}</span>
          <ChevronRight className="w-3.5 h-3.5" />
        </button>
      </div>
    </div>
  );
};
