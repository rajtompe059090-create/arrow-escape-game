import React from 'react';
import { Play, Grid, Wallet, User, Volume2, VolumeX, Sparkles, Trophy, Award } from 'lucide-react';
import { sounds } from '../utils/audio';
import { UserStats } from '../types/game';

interface HomeScreenProps {
  stats: UserStats;
  onContinue: () => void;
  onOpenLevels: () => void;
  onOpenWallet: () => void;
  onOpenProfile: () => void;
  onToggleSound: () => void;
}

export const HomeScreen: React.FC<HomeScreenProps> = ({
  stats,
  onContinue,
  onOpenLevels,
  onOpenWallet,
  onOpenProfile,
  onToggleSound,
}) => {
  const handleAction = (cb: () => void) => {
    sounds.playTap();
    cb();
  };

  const progressPercent = Math.min(100, Math.round((stats.completedLevels.length / 20) * 100));

  return (
    <div
      id="home-screen"
      className="flex flex-col items-center justify-between h-full w-full py-8 px-6 bg-white text-slate-900 select-none"
    >
      {/* Top Bar with Sound and Profile Placeholder */}
      <div className="w-full flex items-center justify-between max-w-sm">
        <button
          id="sound-toggle-button"
          onClick={() => {
            sounds.playTap();
            onToggleSound();
          }}
          className="w-10 h-10 bg-slate-100 hover:bg-slate-200 active:scale-95 rounded-2xl flex items-center justify-center text-slate-600 transition-all shadow-2xs"
          title={stats.soundEnabled ? 'Mute Sound' : 'Enable Sound'}
        >
          {stats.soundEnabled ? (
            <Volume2 className="w-5 h-5 text-blue-600" />
          ) : (
            <VolumeX className="w-5 h-5 text-slate-400" />
          )}
        </button>

        {/* Reward balance pill (Wallet quick access) */}
        <button
          id="quick-wallet-pill"
          onClick={() => handleAction(onOpenWallet)}
          className="flex items-center space-x-1.5 px-4 py-1.5 bg-green-50 hover:bg-green-100 active:scale-95 border border-green-200 rounded-full transition-all text-green-700 font-bold text-xs shadow-2xs"
        >
          <Award className="w-4 h-4 text-green-600" />
          <span>₹{stats.earnedRupees}.00</span>
        </button>

        <button
          id="profile-button-placeholder"
          onClick={() => handleAction(onOpenProfile)}
          className="w-10 h-10 bg-slate-100 hover:bg-slate-200 active:scale-95 rounded-2xl flex items-center justify-center text-slate-600 transition-all shadow-2xs"
          title="Player Profile"
        >
          <User className="w-5 h-5 text-slate-600" />
        </button>
      </div>

      {/* Main Logo & Title */}
      <div className="flex flex-col items-center text-center space-y-4 my-auto">
        <div className="relative flex items-center justify-center w-24 h-24 bg-blue-600 text-white rounded-3xl shadow-xl shadow-blue-200">
          <svg className="w-14 h-14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
            <path d="M5 12h14" />
            <path d="m12 5 7 7-7 7" />
          </svg>
          <Sparkles className="absolute -top-1.5 -right-1.5 w-6 h-6 text-amber-300 fill-amber-300 animate-pulse" />
        </div>

        <div>
          <h1 className="text-3xl font-black tracking-tight text-slate-900">
            Arrow Escape
          </h1>
          <p className="text-xs font-bold tracking-widest text-blue-600 mt-1 uppercase">
            Tap • Solve • Escape
          </p>
        </div>

        {/* Progress bar card */}
        <div className="w-full max-w-xs bg-slate-50 rounded-2xl p-4 border border-slate-200/80 shadow-2xs flex flex-col space-y-2">
          <div className="flex justify-between text-xs font-bold text-slate-600">
            <span className="flex items-center gap-1.5">
              <Trophy className="w-4 h-4 text-blue-500" /> Level {stats.unlockedLevel} / 20
            </span>
            <span className="text-blue-600 font-extrabold">{progressPercent}%</span>
          </div>
          <div className="w-full h-2 bg-slate-200/70 rounded-full overflow-hidden">
            <div
              className="h-full bg-blue-500 rounded-full transition-all duration-500"
              style={{ width: `${Math.max(5, progressPercent)}%` }}
            />
          </div>
        </div>
      </div>

      {/* Action Buttons */}
      <div className="flex flex-col items-center space-y-3 w-full max-w-xs pb-2">
        {/* Continue Button */}
        <button
          id="home-continue-button"
          onClick={() => handleAction(onContinue)}
          className="w-full py-4 px-6 bg-blue-600 hover:bg-blue-700 active:scale-98 text-white font-bold rounded-2xl shadow-lg shadow-blue-200 flex items-center justify-center space-x-2.5 transition-all text-base"
        >
          <Play className="w-5 h-5 fill-current" />
          <span>Continue (Level {stats.unlockedLevel})</span>
        </button>

        {/* Levels Grid Button */}
        <button
          id="home-levels-button"
          onClick={() => handleAction(onOpenLevels)}
          className="w-full py-3.5 px-6 bg-white hover:bg-slate-50 active:scale-98 text-slate-700 font-bold rounded-2xl shadow-2xs border border-slate-200 flex items-center justify-center space-x-2.5 transition-all text-sm"
        >
          <Grid className="w-4 h-4 text-blue-600" />
          <span>Select Levels</span>
        </button>

        {/* Secondary Buttons Row */}
        <div className="grid grid-cols-2 gap-3 w-full">
          <button
            id="home-wallet-button"
            onClick={() => handleAction(onOpenWallet)}
            className="py-3 px-4 bg-slate-50 hover:bg-slate-100 active:scale-98 text-slate-700 font-bold rounded-2xl border border-slate-200 flex items-center justify-center space-x-2 text-xs transition-all"
          >
            <Wallet className="w-4 h-4 text-slate-600" />
            <span>Rewards</span>
          </button>

          <button
            id="home-profile-button"
            onClick={() => handleAction(onOpenProfile)}
            className="py-3 px-4 bg-slate-50 hover:bg-slate-100 active:scale-98 text-slate-700 font-bold rounded-2xl border border-slate-200 flex items-center justify-center space-x-2 text-xs transition-all"
          >
            <User className="w-4 h-4 text-slate-600" />
            <span>Profile</span>
          </button>
        </div>
      </div>
    </div>
  );
};
