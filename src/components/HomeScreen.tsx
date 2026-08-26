import React from 'react';
import {
  Play,
  Wallet,
  Gift,
  Award,
  Settings,
  Sparkles,
  TrendingUp,
  Volume2,
  VolumeX,
  Lightbulb,
  Coins,
  ArrowRight,
  Flame,
  User,
  ShieldCheck,
} from 'lucide-react';
import { sounds } from '../utils/audio';
import { UserStats } from '../types/game';
import { getLevelDifficulty } from '../engine/puzzleEngine';
import { calculateLevelReward, getTodayEarnings, isDailyRewardClaimable } from '../services/earningsService';

interface HomeScreenProps {
  stats: UserStats;
  onPlayContinue: () => void;
  onOpenDailyReward: () => void;
  onOpenRewards: () => void;
  onOpenWallet: () => void;
  onOpenHints: () => void;
  onOpenSettings: () => void;
  onOpenProfile: () => void;
  onOpenLevels?: () => void;
  onToggleSound?: () => void;
}

export const HomeScreen: React.FC<HomeScreenProps> = ({
  stats,
  onPlayContinue,
  onOpenDailyReward,
  onOpenRewards,
  onOpenWallet,
  onOpenHints,
  onOpenSettings,
  onOpenProfile,
  onOpenLevels,
  onToggleSound,
}) => {
  const handleAction = (cb: () => void) => {
    sounds.playTap();
    cb();
  };

  const currentLevel = stats.unlockedLevel;
  const currentDifficulty = getLevelDifficulty(currentLevel);
  const nextLevelReward = calculateLevelReward(currentLevel);

  // Calculate Today's Earnings from transaction ledger
  const todayEarnings = getTodayEarnings(stats);

  // Check if daily reward is claimable (24-hour cycle)
  const isDailyRewardAvailable = isDailyRewardClaimable(stats);

  const displayName = stats.displayName || 'Player One';
  const username = stats.username || 'player_0590';
  const isRegistered = Boolean(stats.isRegistered);

  const getDifficultyColor = (diff: string) => {
    switch (diff) {
      case 'Easy':
        return 'bg-emerald-50 text-emerald-700 border-emerald-200';
      case 'Normal':
        return 'bg-blue-50 text-blue-700 border-blue-200';
      case 'Medium':
        return 'bg-cyan-50 text-cyan-700 border-cyan-200';
      case 'Hard':
        return 'bg-amber-50 text-amber-700 border-amber-200';
      case 'Very Hard':
        return 'bg-orange-50 text-orange-700 border-orange-200';
      case 'Master':
        return 'bg-purple-50 text-purple-700 border-purple-200';
      case 'Grandmaster':
        return 'bg-indigo-50 text-indigo-700 border-indigo-200';
      case 'Legendary':
        return 'bg-rose-50 text-rose-700 border-rose-200';
      default:
        return 'bg-blue-50 text-blue-700 border-blue-200';
    }
  };

  return (
    <div
      id="home-screen"
      className="flex flex-col justify-between h-full w-full py-4 px-4 sm:px-6 bg-slate-50/50 text-slate-900 select-none overflow-y-auto"
    >
      {/* 1. TOP BAR: Audio, Profile, Title, Settings */}
      <div className="w-full space-y-3">
        {/* Navigation / Header Strip */}
        <div className="w-full flex items-center justify-between">
          {/* Left Actions: Sound & Profile */}
          <div className="flex items-center space-x-1.5">
            {onToggleSound && (
              <button
                id="home-sound-button"
                onClick={() => {
                  sounds.playTap();
                  onToggleSound();
                }}
                className="w-9 h-9 bg-white hover:bg-slate-100 active:scale-95 rounded-xl flex items-center justify-center text-slate-600 transition-all shadow-2xs border border-slate-200 cursor-pointer"
                title={stats.soundEnabled ? 'Mute Sound' : 'Enable Sound'}
              >
                {stats.soundEnabled ? (
                  <Volume2 className="w-4 h-4 text-blue-600" />
                ) : (
                  <VolumeX className="w-4 h-4 text-slate-400" />
                )}
              </button>
            )}

            <button
              id="home-profile-header-button"
              onClick={() => handleAction(onOpenProfile)}
              className="h-9 px-2.5 bg-white hover:bg-slate-100 active:scale-95 rounded-xl flex items-center space-x-1.5 text-slate-700 transition-all shadow-2xs border border-slate-200 cursor-pointer"
              title="Player Profile"
            >
              <div className="w-5 h-5 bg-linear-to-br from-blue-500 to-indigo-600 rounded-full flex items-center justify-center text-white text-[10px] font-bold">
                {displayName.charAt(0).toUpperCase()}
              </div>
              <span className="text-xs font-bold text-slate-800 max-w-[80px] truncate">
                {displayName}
              </span>
              {isRegistered && <ShieldCheck className="w-3 h-3 text-emerald-600" />}
            </button>
          </div>

          {/* Centered Brand Title */}
          <div className="flex items-center space-x-1.5">
            <div className="w-7 h-7 bg-blue-600 text-white rounded-xl flex items-center justify-center shadow-xs">
              <svg
                className="w-4 h-4"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2.5"
                strokeLinecap="round"
                strokeLinejoin="round"
              >
                <path d="M5 12h14" />
                <path d="m12 5 7 7-7 7" />
              </svg>
            </div>
            <h1 className="text-base sm:text-lg font-black tracking-tight text-slate-900">
              Arrow Escape
            </h1>
          </div>

          {/* Right Action: Settings Quick Access */}
          <button
            id="home-settings-header-button"
            onClick={() => handleAction(onOpenSettings)}
            className="w-9 h-9 bg-white hover:bg-slate-100 active:scale-95 rounded-xl flex items-center justify-center text-slate-600 transition-all shadow-2xs border border-slate-200 cursor-pointer"
            title="Settings"
          >
            <Settings className="w-4 h-4 text-slate-600" />
          </button>
        </div>

        {/* Top Earnings & Wallet Stat Header Cards */}
        <div className="w-full grid grid-cols-2 gap-2.5">
          {/* Total Earnings */}
          <div
            id="home-stat-total-earnings"
            onClick={() => handleAction(onOpenWallet)}
            className="bg-white hover:bg-slate-50/90 cursor-pointer p-3 rounded-2xl border border-slate-200 flex flex-col items-center text-center transition-all shadow-xs"
          >
            <div className="flex items-center space-x-1 text-slate-500 mb-0.5">
              <TrendingUp className="w-3.5 h-3.5 text-blue-600" />
              <span className="text-[10px] uppercase font-black tracking-wider text-slate-400">
                Total Earnings
              </span>
            </div>
            <span className="text-base font-black text-slate-900">
              ₹{stats.totalEarnings.toFixed(2)}
            </span>
          </div>

          {/* Wallet Balance */}
          <div
            id="home-stat-wallet-balance"
            onClick={() => handleAction(onOpenWallet)}
            className="bg-emerald-50/80 hover:bg-emerald-100/80 cursor-pointer p-3 rounded-2xl border border-emerald-200 flex flex-col items-center text-center transition-all shadow-xs"
          >
            <div className="flex items-center space-x-1 text-emerald-700 mb-0.5">
              <Wallet className="w-3.5 h-3.5 text-emerald-600" />
              <span className="text-[10px] uppercase font-black tracking-wider text-emerald-600">
                Wallet Balance
              </span>
            </div>
            <span className="text-base font-black text-emerald-700">
              ₹{stats.walletBalance.toFixed(2)}
            </span>
          </div>
        </div>
      </div>

      {/* 2. MAIN SECTION: Level Card, Today's Earnings, & Big Play/Continue Button */}
      <div className="w-full flex flex-col items-center my-3 space-y-3">
        {/* Current Level Card */}
        <div
          id="home-current-level-card"
          onClick={onOpenLevels ? () => handleAction(onOpenLevels) : undefined}
          className={`w-full bg-white rounded-3xl p-4 border border-slate-200/90 shadow-sm space-y-3 ${
            onOpenLevels ? 'cursor-pointer hover:border-blue-300 transition-colors' : ''
          }`}
        >
          <div className="flex items-center justify-between">
            <div>
              <div className="flex items-center space-x-1.5">
                <span className="text-[10px] uppercase font-extrabold text-slate-400 tracking-wider">
                  Current Level
                </span>
                {onOpenLevels && (
                  <span className="text-[9px] font-bold text-blue-600 bg-blue-50 px-1.5 py-0.2 rounded-md">
                    Tap to view all
                  </span>
                )}
              </div>
              <div className="text-2xl font-black text-slate-900 tracking-tight flex items-baseline space-x-2">
                <span>Level {currentLevel}</span>
                <span
                  className={`text-xs px-2 py-0.5 rounded-full border font-bold ${getDifficultyColor(
                    currentDifficulty
                  )}`}
                >
                  {currentDifficulty}
                </span>
              </div>
            </div>

            {/* Next Level Reward Tag */}
            <div className="flex flex-col items-end">
              <span className="text-[10px] uppercase font-extrabold text-emerald-600 tracking-wider">
                Reward
              </span>
              <div className="px-2.5 py-1 bg-emerald-50 border border-emerald-200 rounded-xl flex items-center space-x-1 text-emerald-700 font-black text-sm">
                <Coins className="w-3.5 h-3.5 text-emerald-600" />
                <span>₹{nextLevelReward.toFixed(2)}</span>
              </div>
            </div>
          </div>

          {/* Today's Earnings & Streak Strip */}
          <div className="grid grid-cols-2 gap-2 pt-2 border-t border-slate-100">
            <div className="bg-slate-50 rounded-xl px-3 py-1.5 flex items-center justify-between">
              <span className="text-[11px] font-bold text-slate-500">Today's Earnings</span>
              <span className="text-xs font-black text-emerald-600">₹{todayEarnings.toFixed(2)}</span>
            </div>
            <div className="bg-slate-50 rounded-xl px-3 py-1.5 flex items-center justify-between">
              <span className="text-[11px] font-bold text-slate-500 flex items-center space-x-1">
                <Flame className="w-3 h-3 text-amber-500 fill-amber-500" />
                <span>Streak</span>
              </span>
              <span className="text-xs font-black text-amber-600">{stats.dailyStreak} Days</span>
            </div>
          </div>
        </div>

        {/* Big Primary PLAY / CONTINUE Button */}
        <button
          id="home-play-continue-button"
          onClick={() => handleAction(onPlayContinue)}
          className="w-full py-4 bg-linear-to-r from-blue-600 via-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 active:scale-98 text-white rounded-2xl font-black text-lg flex items-center justify-center space-x-2.5 shadow-lg shadow-blue-500/25 transition-all cursor-pointer group"
        >
          <Play className="w-6 h-6 fill-current transition-transform group-hover:scale-110" />
          <span>{stats.completedLevels.includes(currentLevel) ? `REPLAY LEVEL ${currentLevel}` : `PLAY LEVEL ${currentLevel}`}</span>
          <ArrowRight className="w-5 h-5 ml-1 transition-transform group-hover:translate-x-1" />
        </button>
      </div>

      {/* 3. BOTTOM FEATURES BAR: Profile, Wallet, Daily, Rewards, Hints, Settings */}
      <div className="w-full space-y-2">
        <div className="w-full grid grid-cols-3 sm:grid-cols-6 gap-2">
          {/* 👤 Profile */}
          <button
            id="home-profile-grid-button"
            onClick={() => handleAction(onOpenProfile)}
            className="p-2.5 bg-white hover:bg-slate-50 active:scale-95 text-slate-800 rounded-2xl border border-slate-200 flex flex-col items-center justify-center text-center transition-all shadow-2xs group cursor-pointer"
          >
            <div className="w-9 h-9 rounded-xl bg-indigo-50 group-hover:bg-indigo-100 flex items-center justify-center text-indigo-600 mb-1 transition-colors">
              <User className="w-4 h-4" />
            </div>
            <span className="text-[11px] font-extrabold text-slate-800 leading-tight">
              Profile
            </span>
            <span className="text-[9px] font-bold text-indigo-600 truncate max-w-full">
              Account
            </span>
          </button>

          {/* 💰 Wallet */}
          <button
            id="home-wallet-button"
            onClick={() => handleAction(onOpenWallet)}
            className="p-2.5 bg-white hover:bg-slate-50 active:scale-95 text-slate-800 rounded-2xl border border-slate-200 flex flex-col items-center justify-center text-center transition-all shadow-2xs group cursor-pointer"
          >
            <div className="w-9 h-9 rounded-xl bg-emerald-100/80 group-hover:bg-emerald-200/80 flex items-center justify-center text-emerald-600 mb-1 transition-colors">
              <Wallet className="w-4 h-4" />
            </div>
            <span className="text-[11px] font-extrabold text-slate-800 leading-tight">
              Wallet
            </span>
            <span className="text-[9px] font-bold text-emerald-600 truncate max-w-full">
              Payouts
            </span>
          </button>

          {/* 🎁 Daily Reward */}
          <button
            id="home-daily-reward-button"
            onClick={() => handleAction(onOpenDailyReward)}
            className="relative p-2.5 bg-white hover:bg-slate-50 active:scale-95 text-slate-800 rounded-2xl border border-slate-200 flex flex-col items-center justify-center text-center transition-all shadow-2xs group cursor-pointer"
          >
            <div className="w-9 h-9 rounded-xl bg-amber-100/80 group-hover:bg-amber-200/80 flex items-center justify-center text-amber-600 mb-1 transition-colors">
              <Gift className="w-4 h-4" />
            </div>
            <span className="text-[11px] font-extrabold text-slate-800 leading-tight">
              Daily
            </span>
            <span className="text-[9px] font-bold text-amber-600 truncate max-w-full">
              Free ₹
            </span>
            {isDailyRewardAvailable && (
              <span className="absolute -top-1 -right-1 flex h-3.5 w-3.5">
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-amber-400 opacity-75"></span>
                <span className="relative inline-flex rounded-full h-3.5 w-3.5 bg-amber-500 text-white font-black text-[9px] items-center justify-center shadow-xs">
                  !
                </span>
              </span>
            )}
          </button>

          {/* 🏆 Rewards */}
          <button
            id="home-rewards-button"
            onClick={() => handleAction(onOpenRewards)}
            className="p-2.5 bg-white hover:bg-slate-50 active:scale-95 text-slate-800 rounded-2xl border border-slate-200 flex flex-col items-center justify-center text-center transition-all shadow-2xs group cursor-pointer"
          >
            <div className="w-9 h-9 rounded-xl bg-blue-100/80 group-hover:bg-blue-200/80 flex items-center justify-center text-blue-600 mb-1 transition-colors">
              <Award className="w-4 h-4" />
            </div>
            <span className="text-[11px] font-extrabold text-slate-800 leading-tight">
              Rewards
            </span>
            <span className="text-[9px] font-bold text-blue-600 truncate max-w-full">
              Tiers
            </span>
          </button>

          {/* 💡 Hints */}
          <button
            id="home-hints-button"
            onClick={() => handleAction(onOpenHints)}
            className="p-2.5 bg-white hover:bg-slate-50 active:scale-95 text-slate-800 rounded-2xl border border-slate-200 flex flex-col items-center justify-center text-center transition-all shadow-2xs group cursor-pointer"
          >
            <div className="w-9 h-9 rounded-xl bg-amber-50 group-hover:bg-amber-100 flex items-center justify-center text-amber-500 mb-1 transition-colors">
              <Lightbulb className="w-4 h-4 fill-current" />
            </div>
            <span className="text-[11px] font-extrabold text-slate-800 leading-tight">
              Hints
            </span>
            <span className="text-[9px] font-bold text-amber-600 truncate max-w-full">
              {stats.hintsRemaining} left
            </span>
          </button>

          {/* ⚙️ Settings */}
          <button
            id="home-settings-button"
            onClick={() => handleAction(onOpenSettings)}
            className="p-2.5 bg-white hover:bg-slate-50 active:scale-95 text-slate-800 rounded-2xl border border-slate-200 flex flex-col items-center justify-center text-center transition-all shadow-2xs group cursor-pointer"
          >
            <div className="w-9 h-9 rounded-xl bg-slate-100 group-hover:bg-slate-200 flex items-center justify-center text-slate-600 mb-1 transition-colors">
              <Settings className="w-4 h-4" />
            </div>
            <span className="text-[11px] font-extrabold text-slate-800 leading-tight">
              Settings
            </span>
            <span className="text-[9px] font-bold text-slate-500 truncate max-w-full">
              Audio
            </span>
          </button>
        </div>
      </div>
    </div>
  );
};
