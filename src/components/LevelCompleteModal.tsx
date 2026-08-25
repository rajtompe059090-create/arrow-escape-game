import React, { useEffect, useState } from 'react';
import confetti from 'canvas-confetti';
import {
  ArrowRight,
  RotateCcw,
  Home,
  Award,
  Sparkles,
  Star,
  Trophy,
  Tv,
  Wallet,
  CheckCircle2,
  Zap,
  Flame,
  ShieldAlert,
  Crown,
} from 'lucide-react';
import { sounds } from '../utils/audio';
import { getLevelDifficulty } from '../engine/puzzleEngine';
import { Difficulty } from '../types/game';

interface LevelCompleteModalProps {
  levelId: number;
  rewardRupees: number;
  totalEarnings: number;
  walletBalance?: number;
  remainingLives: number;
  onNextLevel: () => void;
  onDoubleRewardAd: () => void;
  onReplay: () => void;
  onHome: () => void;
  isRewardDoubled?: boolean;
  isAlreadyClaimed?: boolean;
}

export const LevelCompleteModal: React.FC<LevelCompleteModalProps> = ({
  levelId,
  rewardRupees,
  totalEarnings,
  walletBalance,
  remainingLives,
  onNextLevel,
  onDoubleRewardAd,
  onReplay,
  onHome,
  isRewardDoubled = false,
  isAlreadyClaimed = false,
}) => {
  const [isActionLocked, setIsActionLocked] = useState<boolean>(false);

  useEffect(() => {
    try {
      confetti({
        particleCount: 90,
        spread: 75,
        origin: { y: 0.55 },
        colors: ['#2563EB', '#38BDF8', '#F59E0B', '#10B981', '#6366F1', '#EC4899'],
      });
    } catch {}
  }, []);

  const handleAction = (callback: () => void) => {
    if (isActionLocked) return;
    setIsActionLocked(true);
    sounds.playTap();
    callback();
    // Unlock after 800ms to debounce accidental multi-taps
    setTimeout(() => setIsActionLocked(false), 800);
  };

  const difficulty: Difficulty = getLevelDifficulty(levelId);

  // Difficulty badge styling helper
  const getDifficultyBadge = (diff: Difficulty) => {
    switch (diff) {
      case 'Easy':
        return {
          bg: 'bg-emerald-50 text-emerald-700 border-emerald-200',
          icon: <Zap className="w-3 h-3" />,
        };
      case 'Normal':
        return {
          bg: 'bg-blue-50 text-blue-700 border-blue-200',
          icon: <Sparkles className="w-3 h-3" />,
        };
      case 'Hard':
        return {
          bg: 'bg-amber-50 text-amber-700 border-amber-200',
          icon: <Flame className="w-3 h-3" />,
        };
      case 'Very Hard':
        return {
          bg: 'bg-orange-50 text-orange-700 border-orange-200',
          icon: <ShieldAlert className="w-3 h-3" />,
        };
      case 'Extreme':
        return {
          bg: 'bg-rose-50 text-rose-700 border-rose-200',
          icon: <Crown className="w-3 h-3" />,
        };
      default:
        return {
          bg: 'bg-blue-50 text-blue-700 border-blue-200',
          icon: <Sparkles className="w-3 h-3" />,
        };
    }
  };

  const diffBadge = getDifficultyBadge(difficulty);
  const finalRewardEarned = isAlreadyClaimed
    ? 0
    : isRewardDoubled
    ? rewardRupees * 2
    : rewardRupees;

  return (
    <div
      id="level-complete-modal"
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs select-none animate-in fade-in duration-200"
    >
      <div className="relative w-full max-w-sm bg-white text-slate-900 rounded-3xl p-6 shadow-2xl flex flex-col items-center text-center space-y-4 border border-slate-200 max-h-[94vh] overflow-y-auto">
        {/* Animated Trophy Banner */}
        <div className="relative flex items-center justify-center w-20 h-20 bg-gradient-to-br from-blue-500 to-indigo-600 rounded-3xl shadow-xl shadow-blue-500/25 text-white">
          <Trophy className="w-10 h-10 animate-bounce" />
          <Sparkles className="absolute -top-2 -right-2 w-6 h-6 text-amber-300 fill-amber-300 animate-spin" />
        </div>

        {/* Title Header */}
        <div className="space-y-1">
          <h2 className="text-2xl font-black text-slate-900 tracking-tight flex items-center justify-center space-x-1.5">
            <span>🎉</span>
            <span>LEVEL COMPLETE!</span>
          </h2>
          <div className="flex items-center justify-center space-x-2 pt-0.5">
            <span className="text-xs font-black text-slate-700 bg-slate-100 px-2.5 py-0.5 rounded-full border border-slate-200">
              Level: {levelId}
            </span>
            <span
              className={`text-xs font-black px-2.5 py-0.5 rounded-full border flex items-center space-x-1 ${diffBadge.bg}`}
            >
              {diffBadge.icon}
              <span>Difficulty: {difficulty}</span>
            </span>
          </div>
        </div>

        {/* 3 Stars Rating */}
        <div className="flex items-center space-x-2 py-0.5">
          {[1, 2, 3].map(starIndex => {
            const hasStar = starIndex <= remainingLives;
            return (
              <Star
                key={starIndex}
                className={`w-7 h-7 transition-all ${
                  hasStar
                    ? 'text-amber-400 fill-amber-400 scale-110 drop-shadow-xs'
                    : 'text-slate-200 fill-slate-200 scale-90'
                }`}
              />
            );
          })}
        </div>

        {/* Reward & Earnings Cards */}
        <div className="w-full grid grid-cols-2 gap-2.5 pt-1">
          {/* 1. 💰 Reward Earned */}
          <div className="bg-emerald-50 border border-emerald-200/90 rounded-2xl p-3 flex flex-col items-start justify-between text-left relative overflow-hidden shadow-2xs">
            <div className="flex items-center space-x-1.5 text-emerald-800 text-[11px] font-bold">
              <Award className="w-4 h-4 text-emerald-600 shrink-0" />
              <span className="truncate">Reward Earned</span>
            </div>
            <div className="mt-2 text-xl font-black text-emerald-700 tracking-tight">
              ₹{finalRewardEarned.toFixed(2)}
            </div>
            {isAlreadyClaimed ? (
              <span className="text-[9px] font-bold text-slate-500 mt-0.5">
                Replayed (Claimed)
              </span>
            ) : isRewardDoubled ? (
              <span className="text-[9px] font-black text-amber-600 bg-amber-100 px-1.5 py-0.2 rounded mt-0.5">
                2X DOUBLED
              </span>
            ) : (
              <span className="text-[9px] font-semibold text-emerald-600 mt-0.5">
                Credited to wallet
              </span>
            )}
          </div>

          {/* 2. 💰 Total Earnings */}
          <div className="bg-blue-50 border border-blue-200/90 rounded-2xl p-3 flex flex-col items-start justify-between text-left relative overflow-hidden shadow-2xs">
            <div className="flex items-center space-x-1.5 text-blue-800 text-[11px] font-bold">
              <Wallet className="w-4 h-4 text-blue-600 shrink-0" />
              <span className="truncate">Total Earnings</span>
            </div>
            <div className="mt-2 text-xl font-black text-blue-700 tracking-tight">
              ₹{totalEarnings.toFixed(2)}
            </div>
            <span className="text-[9px] font-semibold text-blue-600 mt-0.5">
              {walletBalance !== undefined ? `Wallet: ₹${walletBalance.toFixed(2)}` : 'Lifetime earned'}
            </span>
          </div>
        </div>

        {/* Main Action Buttons */}
        <div className="flex flex-col space-y-2.5 w-full pt-2">
          {/* 📺 Continue / Double Reward Ad Button (if not already claimed/doubled) */}
          {!isRewardDoubled && !isAlreadyClaimed && (
            <button
              id="complete-continue-ad-button"
              disabled={isActionLocked}
              onClick={() => handleAction(onDoubleRewardAd)}
              className="w-full py-3 px-4 bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-600 hover:to-amber-700 active:scale-98 text-white font-black rounded-2xl shadow-sm flex items-center justify-center space-x-2 text-xs transition-all disabled:opacity-50"
            >
              <Tv className="w-4 h-4 shrink-0" />
              <span>📺 Continue (+₹{rewardRupees.toFixed(2)} Bonus Ad)</span>
            </button>
          )}

          {/* ➡️ Next Level Button */}
          <button
            id="complete-next-button"
            disabled={isActionLocked}
            onClick={() => handleAction(onNextLevel)}
            className="w-full py-4 px-6 bg-blue-600 hover:bg-blue-700 active:scale-98 text-white font-black rounded-2xl shadow-lg shadow-blue-500/25 flex items-center justify-center space-x-2 text-sm transition-all disabled:opacity-50"
          >
            <span>➡️ Next Level ({levelId + 1})</span>
            <ArrowRight className="w-4 h-4 ml-1" />
          </button>

          {/* Secondary Replay & Home Row */}
          <div className="grid grid-cols-2 gap-2.5 w-full pt-1">
            <button
              id="complete-replay-button"
              disabled={isActionLocked}
              onClick={() => handleAction(onReplay)}
              className="py-2.5 px-4 bg-slate-100 hover:bg-slate-200 active:scale-98 text-slate-700 font-bold rounded-2xl border border-slate-200 flex items-center justify-center space-x-1.5 text-xs transition-all disabled:opacity-50"
            >
              <RotateCcw className="w-3.5 h-3.5" />
              <span>Replay</span>
            </button>

            <button
              id="complete-home-button"
              disabled={isActionLocked}
              onClick={() => handleAction(onHome)}
              className="py-2.5 px-4 bg-slate-100 hover:bg-slate-200 active:scale-98 text-slate-700 font-bold rounded-2xl border border-slate-200 flex items-center justify-center space-x-1.5 text-xs transition-all disabled:opacity-50"
            >
              <Home className="w-3.5 h-3.5" />
              <span>Home</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
