import React, { useState } from 'react';
import {
  X,
  Trophy,
  Wallet,
  TrendingUp,
  Gamepad2,
  Gift,
  Tv,
  Lightbulb,
  ArrowRight,
  Sparkles,
  ChevronRight,
  Flame,
  Zap,
  CheckCircle2,
  Clock,
  PlayCircle,
} from 'lucide-react';
import { UserStats } from '../types/game';
import { sounds } from '../utils/audio';
import { getLevelDifficulty } from '../engine/puzzleEngine';
import { calculateLevelReward } from '../services/earningsService';
import { DAILY_REWARDS } from './DailyRewardModal';

interface RewardsModalProps {
  stats: UserStats;
  onClose: () => void;
  onOpenDaily: () => void;
  onOpenWallet: () => void;
  onWatchRewardAd: () => void;
  onWatchHintAd: () => void;
}

const TWENTY_FOUR_HOURS_MS = 24 * 60 * 60 * 1000;
const FORTY_EIGHT_HOURS_MS = 48 * 60 * 60 * 1000;

export const RewardsModal: React.FC<RewardsModalProps> = ({
  stats,
  onClose,
  onOpenDaily,
  onOpenWallet,
  onWatchRewardAd,
  onWatchHintAd,
}) => {
  const [adProcessing, setAdProcessing] = useState<boolean>(false);

  // 1. Current Level Reward Info
  const currentLevel = stats.unlockedLevel;
  const currentLevelReward = calculateLevelReward(currentLevel);
  const currentLevelDiff = getLevelDifficulty(currentLevel);

  // 2. Daily Reward Info
  const now = Date.now();
  const lastTimestamp = stats.lastDailyRewardTimestamp;
  const isWithin24Hours = Boolean(
    lastTimestamp && now - lastTimestamp < TWENTY_FOUR_HOURS_MS
  );
  const isStreakBroken = Boolean(
    lastTimestamp && now - lastTimestamp > FORTY_EIGHT_HOURS_MS
  );
  const effectiveStreak = isStreakBroken ? 0 : stats.dailyStreak || 0;
  const activeCycleDay = isWithin24Hours
    ? ((stats.dailyStreak - 1) % 7) + 1
    : (effectiveStreak % 7) + 1;
  const todayRewardObj = DAILY_REWARDS[activeCycleDay - 1] || DAILY_REWARDS[0];
  const todayDailyAmount = todayRewardObj.amount;
  const isDailyClaimed = isWithin24Hours;

  const handleAction = (callback: () => void) => {
    if (adProcessing) return;
    setAdProcessing(true);
    sounds.playTap();
    callback();
    setTimeout(() => setAdProcessing(false), 800);
  };

  return (
    <div
      id="rewards-screen-backdrop"
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs animate-in fade-in duration-200"
    >
      <div
        id="rewards-screen-modal"
        className="w-full max-w-sm bg-white rounded-3xl p-5 shadow-2xl border border-slate-200 flex flex-col space-y-4 text-slate-900 animate-in zoom-in-95 duration-200 select-none max-h-[92vh] overflow-y-auto"
      >
        {/* 1. Header Bar */}
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2.5">
            <div className="w-10 h-10 bg-amber-50 rounded-2xl border border-amber-200 flex items-center justify-center text-amber-600 shadow-2xs">
              <Trophy className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-lg font-black text-slate-900 tracking-tight flex items-center space-x-1.5">
                <span>🏆 Rewards</span>
              </h2>
              <p className="text-xs text-slate-400 font-semibold">
                Earn real cash rewards & powerups
              </p>
            </div>
          </div>
          <button
            id="rewards-modal-close-btn"
            onClick={() => {
              sounds.playTap();
              onClose();
            }}
            className="w-8 h-8 bg-slate-100 hover:bg-slate-200 active:scale-95 rounded-full flex items-center justify-center text-slate-600 transition-all"
            title="Close"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* 2. Player Balance Summary: Wallet Balance & Total Earnings */}
        <div className="grid grid-cols-2 gap-2.5">
          {/* 💰 Wallet Balance */}
          <button
            id="rewards-wallet-card-btn"
            onClick={() => {
              sounds.playTap();
              onOpenWallet();
            }}
            className="bg-emerald-50 hover:bg-emerald-100/80 active:scale-98 transition-all border border-emerald-200/90 rounded-2xl p-3 flex flex-col items-start justify-between text-left shadow-2xs group"
          >
            <div className="flex items-center justify-between w-full">
              <div className="flex items-center space-x-1 text-emerald-800 text-[10px] font-extrabold uppercase tracking-wide">
                <Wallet className="w-3.5 h-3.5 text-emerald-600 shrink-0" />
                <span>Wallet Balance</span>
              </div>
              <ChevronRight className="w-3.5 h-3.5 text-emerald-500 group-hover:translate-x-0.5 transition-transform" />
            </div>
            <div className="mt-2 text-xl font-black text-emerald-700 tracking-tight">
              ₹{stats.walletBalance.toFixed(2)}
            </div>
            <span className="text-[9px] font-bold text-emerald-600/90 mt-0.5">
              Tap to View Wallet →
            </span>
          </button>

          {/* 📈 Total Earnings */}
          <div className="bg-blue-50 border border-blue-200/90 rounded-2xl p-3 flex flex-col items-start justify-between text-left shadow-2xs">
            <div className="flex items-center space-x-1 text-blue-800 text-[10px] font-extrabold uppercase tracking-wide">
              <TrendingUp className="w-3.5 h-3.5 text-blue-600 shrink-0" />
              <span>Total Earnings</span>
            </div>
            <div className="mt-2 text-xl font-black text-blue-700 tracking-tight">
              ₹{stats.totalEarnings.toFixed(2)}
            </div>
            <span className="text-[9px] font-semibold text-blue-600/90 mt-0.5">
              Lifetime Earned
            </span>
          </div>
        </div>

        {/* 3. Reward Cards */}
        <div className="space-y-2.5 pt-1">
          {/* Card 1: 🎮 LEVEL REWARD */}
          <div
            id="reward-card-level"
            className="p-3.5 rounded-2xl border border-slate-200 bg-slate-50 flex flex-col space-y-2"
          >
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-2">
                <div className="w-8 h-8 rounded-xl bg-blue-100 text-blue-600 flex items-center justify-center">
                  <Gamepad2 className="w-4 h-4" />
                </div>
                <div>
                  <h3 className="text-xs font-black text-slate-900 uppercase tracking-tight">
                    🎮 Level Reward
                  </h3>
                  <p className="text-[10px] font-bold text-slate-500">
                    Solve puzzles to earn cash
                  </p>
                </div>
              </div>
              <span className="text-xs font-black px-2 py-0.5 rounded-lg bg-emerald-100 text-emerald-800 border border-emerald-200">
                +₹{currentLevelReward.toFixed(2)} / level
              </span>
            </div>

            <div className="bg-white p-2.5 rounded-xl border border-slate-200/80 text-[11px] flex items-center justify-between">
              <div className="flex items-center space-x-1.5">
                <span className="font-bold text-slate-600">Current:</span>
                <span className="font-black text-slate-900 bg-slate-100 px-2 py-0.5 rounded-md">
                  Level {currentLevel}
                </span>
                <span className="text-[10px] font-bold text-blue-600">
                  ({currentLevelDiff})
                </span>
              </div>
              <span className="font-bold text-slate-500">
                ₹{currentLevelReward.toFixed(2)} reward
              </span>
            </div>

            <div className="text-[10px] text-slate-400 font-medium px-0.5 leading-relaxed">
              • 1-50: <span className="font-bold text-slate-600">₹1</span> • 51-125: <span className="font-bold text-slate-600">₹2</span> • 126-250: <span className="font-bold text-slate-600">₹3</span> • 251-400: <span className="font-bold text-slate-600">₹5</span> • 401-550: <span className="font-bold text-slate-600">₹10</span> • 551-700: <span className="font-bold text-slate-600">₹15</span> • 701-850: <span className="font-bold text-slate-600">₹20</span> • 851+: <span className="font-bold text-slate-600">₹25</span>
            </div>
          </div>

          {/* Card 2: 🎁 DAILY REWARD */}
          <div
            id="reward-card-daily"
            className="p-3.5 rounded-2xl border border-amber-200/80 bg-amber-50/50 flex flex-col space-y-2.5"
          >
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-2">
                <div className="w-8 h-8 rounded-xl bg-amber-100 text-amber-600 flex items-center justify-center">
                  <Gift className="w-4 h-4" />
                </div>
                <div>
                  <h3 className="text-xs font-black text-slate-900 uppercase tracking-tight">
                    🎁 Daily Reward
                  </h3>
                  <div className="flex items-center space-x-1 text-[10px] font-extrabold text-amber-700">
                    <Flame className="w-3 h-3 text-amber-500 fill-amber-500" />
                    <span>Streak: {stats.dailyStreak} {stats.dailyStreak === 1 ? 'Day' : 'Days'}</span>
                  </div>
                </div>
              </div>
              <div className="text-right">
                <span className="text-xs font-black text-emerald-700 bg-emerald-100 px-2 py-0.5 rounded-lg border border-emerald-200">
                  Today: ₹{todayDailyAmount.toFixed(2)}
                </span>
              </div>
            </div>

            <div className="flex items-center justify-between bg-white p-2 rounded-xl border border-amber-200/60 text-[11px]">
              <div className="flex items-center space-x-1.5">
                <span className="font-bold text-slate-600">Status:</span>
                {isDailyClaimed ? (
                  <span className="flex items-center space-x-1 text-emerald-700 font-bold">
                    <CheckCircle2 className="w-3.5 h-3.5" />
                    <span>Claimed for Today</span>
                  </span>
                ) : (
                  <span className="text-amber-700 font-black flex items-center space-x-1">
                    <Sparkles className="w-3.5 h-3.5 text-amber-500 fill-amber-400" />
                    <span>Ready to Claim!</span>
                  </span>
                )}
              </div>

              <button
                id="rewards-open-daily-btn"
                onClick={() => {
                  sounds.playTap();
                  onOpenDaily();
                }}
                className="py-1.5 px-3 bg-amber-500 hover:bg-amber-600 active:scale-95 text-white font-black rounded-lg text-xs flex items-center space-x-1 shadow-2xs transition-all"
              >
                <span>Open Daily Reward</span>
                <ChevronRight className="w-3 h-3" />
              </button>
            </div>
          </div>

          {/* Card 3: 📺 REWARD AD */}
          <div
            id="reward-card-ad"
            className="p-3.5 rounded-2xl border border-blue-200/80 bg-blue-50/50 flex flex-col space-y-2.5"
          >
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-2">
                <div className="w-8 h-8 rounded-xl bg-blue-100 text-blue-600 flex items-center justify-center">
                  <Tv className="w-4 h-4" />
                </div>
                <div>
                  <h3 className="text-xs font-black text-slate-900 uppercase tracking-tight">
                    📺 Reward Ad
                  </h3>
                  <p className="text-[10px] font-bold text-slate-500">
                    Watch rewarded video for bonus cash
                  </p>
                </div>
              </div>
              <span className="text-xs font-black text-blue-700 bg-blue-100 px-2 py-0.5 rounded-lg border border-blue-200">
                +₹0.50 Cash
              </span>
            </div>

            <button
              id="rewards-watch-ad-btn"
              disabled={adProcessing}
              onClick={() => handleAction(onWatchRewardAd)}
              className="w-full py-2.5 px-4 bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 active:scale-98 text-white font-black rounded-xl text-xs flex items-center justify-center space-x-1.5 shadow-sm transition-all disabled:opacity-60"
            >
              <PlayCircle className="w-4 h-4 fill-current" />
              <span>Watch Ad for ₹0.50</span>
            </button>
          </div>

          {/* Card 4: 💡 HINT AD */}
          <div
            id="reward-card-hint"
            className="p-3.5 rounded-2xl border border-purple-200/80 bg-purple-50/50 flex flex-col space-y-2.5"
          >
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-2">
                <div className="w-8 h-8 rounded-xl bg-purple-100 text-purple-600 flex items-center justify-center">
                  <Lightbulb className="w-4 h-4" />
                </div>
                <div>
                  <h3 className="text-xs font-black text-slate-900 uppercase tracking-tight">
                    💡 Hint Ad
                  </h3>
                  <p className="text-[10px] font-bold text-slate-500">
                    Free puzzle solve assistance
                  </p>
                </div>
              </div>
              <span className="text-xs font-black text-purple-700 bg-purple-100 px-2 py-0.5 rounded-lg border border-purple-200">
                +1 Free Hint
              </span>
            </div>

            <button
              id="rewards-watch-hint-ad-btn"
              disabled={adProcessing}
              onClick={() => handleAction(onWatchHintAd)}
              className="w-full py-2.5 px-4 bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-700 hover:to-indigo-700 active:scale-98 text-white font-black rounded-xl text-xs flex items-center justify-center space-x-1.5 shadow-sm transition-all disabled:opacity-60"
            >
              <PlayCircle className="w-4 h-4 fill-current" />
              <span>Watch Ad for 1 Free Hint</span>
            </button>
          </div>
        </div>

        {/* 4. Footer Close Button */}
        <button
          id="rewards-screen-close-footer"
          onClick={() => {
            sounds.playTap();
            onClose();
          }}
          className="w-full py-2.5 bg-slate-100 hover:bg-slate-200 active:scale-98 text-slate-600 font-bold rounded-2xl text-xs transition-all border border-slate-200/80"
        >
          Close
        </button>
      </div>
    </div>
  );
};
